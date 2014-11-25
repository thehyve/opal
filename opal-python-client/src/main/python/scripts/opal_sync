#!/usr/bin/python

from __future__ import print_function, division
from sortedcontainers import SortedSet

import time
from os import path

import opal.protobuf.Magma_pb2
import opal.protobuf.Commands_pb2
import opal.protobuf.Projects_pb2
from opal_tools_lib import *
import pycurl

verbose = '-v' in sys.argv
quiet = '-q' in sys.argv

CONFIGFILE = '~/.opal/datasync.conf'

def setup():
    global skipped_tables, logfile, projects, login_info, fileupload_src_folder

    with setup_loader(CONFIGFILE) as config:
        login_info = get_login_info()
        skipped_tables = [p.strip() for p in config.get('main', 'skipped_tables').split(',')]
        logfile = expanduser(config.get('main', 'logfile'))
        fileupload_src_folder = expanduser(config.get('main', 'fileupload_src_folder'))

        if not path.isdir(fileupload_src_folder):
            raise KnownError("fileupload_src_folder '{0}' does not exist".format(fileupload_src_folder))

        sections = config.sections()
        sections.remove('main')
        projects = []
        local_projects = [] #keeping track of local projects, making sure its not mapped more than once
        for section in sections:
            proj = RemoteProject(section, config)
            if proj.local_project in local_projects:
                raise KnownError("Local project '{0}' mapped to multiple remote projects".format(proj.local_project))
            local_projects.append(proj.local_project)
            projects.append(proj)

class RemoteProject:

    def __init__(self, section, config):
        self.section = section
        self.login_info = parse_login_info(config, section)
        if self.login_info.isSsl():
            raise KnownError("Authentication on remote Opals must be done with user/password")
        self.remote_project = config.get(section, 'remote_project')
        self.local_project = config.get(section, 'local_project')
        self.has_deletes = False

    def info(self, msg):
        logging.info("{0}: {1}".format(self.section, msg))

    def transient_table(self, t):
        return table_fullname(self.transient_ds, t)

    def process(self):
        self.info("processing remote project '{0}', mapped on local project '{1}'".format(
            self.remote_project, self.local_project))

        self.tables = get_tables(self.remote_project, login=self.login_info)
        self.create_missing_project_and_tables()

        #creates a transient datasource in the local Opal
        self.transient_ds = self.create_transient_datasource()

        self.import_tables()

        for t in self.tables:
            self.check_removals(t)
            fullname = table_fullname(self.local_project, t)
            self.info('starting reindex for table {0}'.format(fullname))
            rest_call_with_params('/indexes', ['table={0}'.format(u(fullname))], 'PUT')


    def create_missing_project_and_tables(self):
        """Creates locally the missing tables that exist remotelly"""

        try:
            local_tables = get_tables(self.local_project)
        except pycurl.error as e:
            if error_code(e) == 404:
                #project does not exist
                self.info("Local project {0} doesn't exist. Will attempt to create it".format(self.local_project))
                create_project(self.local_project)
                self.info("Created local project '{0}'".format(self.local_project))
                local_tables = dict()

        for table in self.tables.keys():
            if not local_tables.has_key(table):
                entity_type = self.tables[table]
                dto = opal.protobuf.Magma_pb2.TableDto()
                dto.name = table
                dto.entityType = entity_type
                rest_post('/datasource/{0}/tables'.format(self.local_project), dto.SerializeToString())
                self.info("created table '{0}.{1}', with entity type '{2}'".format(
                    self.local_project, table, entity_type))

    def import_tables(self):
        """ Synchronizes variables/data in all the tables in the local project """

        self.info("Synchronizing tables {0}".format(map(str, self.tables.keys())))
        options = opal.protobuf.Commands_pb2.ImportCommandOptionsDto()
        options.destination = self.local_project #local datasource
        options.createVariables = True
        options.tables.extend(map(self.transient_table,self.tables.keys()))

        job_id = parse_job_id(rest_post("/project/{0}/commands/_import".format(
            self.local_project), options.SerializeToString()))

        time.sleep(2) #a small head start
        finished = self.check_import_job_status(job_id)

        while not finished:
            time.sleep(5) #poll every 5 seconds
            finished = self.check_import_job_status(job_id)

    def check_import_job_status(self, job_id):
        res = json_loads(rest_call("/shell/command/{0}".format(job_id)))
        status = res['status']
        if status == 'SUCCEEDED':
            self.info("importing finished")
            return True
        elif status == 'IN_PROGRESS':
            if (res.has_key('progress')):
                prog = res['progress']
                self.info("{0}: {1}% done, imported {2} out of {3} rows".format(
                    prog['message'], prog['percent'], prog['current'], prog['end']))
            return False
        else:
            raise KnownError("Problem running job: {0}".format(res))

    def create_transient_datasource(self):
        """Creates a transient datasource in the local Opal, copied from the remote project"""

        factory = opal.protobuf.Magma_pb2.DatasourceFactoryDto()
        factory.incrementalConfig.incremental = True
        factory.incrementalConfig.incrementalDestinationName = self.local_project

        rest_factory = factory.Extensions[opal.protobuf.Magma_pb2.RestDatasourceFactoryDto.params]
        rest_factory.url = self.login_info.data['server']
        rest_factory.username = self.login_info.data['user']
        rest_factory.password = self.login_info.data['password']
        rest_factory.remoteDatasource = self.remote_project

        res = json_loads(rest_post('/project/{0}/transient-datasources'.format(
            self.local_project), factory.SerializeToString()))
        return res['name']

    def check_removals(self, table):
        """
        Checks if the local table has ecords that no longer exist in the source,
        and if so creates a data deletion file for automated data upload to pickup
        :param table:
        :return:
        """
        local_ids = get_entity_ids(self.local_project, table)
        remote_ids = get_entity_ids(self.remote_project, table, login=self.login_info)
        local_ids.difference_update(remote_ids) #keep only what should be deleted
        if len(local_ids) > 0:
            self.has_deletes = True
            self.create_data_delete_file(table, local_ids)

    def create_data_delete_file(self, table, ids_set):
        """
        :param table: table to delete data from
        :param ids_set: sortedcontainers SortedSet with ids of the ValueSets to delete
        :return:
        """
        tm = time.strftime('%Y%m%d%H%M%S')
        filename = "{0}.{1}.{2}.delete.csv".format(tm, self.local_project, table)

        file_path = path.join(fileupload_src_folder, filename)
        self.info('creating file {0}, for deleting {1} row(s)'.format(file_path, len(ids_set)))

        f = open(file_path, 'w')
        print('id', file=f)
        for e in iter(ids_set):
            print(e, file=f)
        f.close()

def get_default_storage_database():
    res = json_loads(rest_call('/system/databases?usage=storage'))
    for e in res:
        if e['usage'] == 'STORAGE' and e['defaultStorage'] == True:
            return e['name']
    return None

def create_project(project):

    database = get_default_storage_database()
    if database is None:
        raise KnownError("No default storage database found in local Opal. Cannot create project")

    dto = opal.protobuf.Projects_pb2.ProjectFactoryDto()
    dto.name = project
    dto.title = project
    dto.database = database
    rest_post('/projects', dto.SerializeToString())

def get_tables(project, login=login_info):
    """Returns a dict of table name -> entity type for the given project and auth settings"""

    #try:
    res = json_loads(rest_call('/datasource/{0}/tables'.format(project), login=login))
    #except pycurl.error as e:
    #    if error_code(e) == 404:
    #        raise KnownError("Project '{0}' does not exist on the server".format(project))

    result = {t['name']: t['entityType'] for t in res}
    for st in skipped_tables:
        if (result.has_key(st)):
            result.pop(st)
    return result

def get_entity_ids(project, table, login=login_info):
    """Returns a sortedcontainers SortedSet with all the ids in the given project/table"""

    try:
        res = json_loads(rest_call('/datasource/{0}/table/{1}/entities'.format(project, table), login=login))
    except pycurl.error as e:
        if error_code(e) == 404:
            raise KnownError("Project '{0}' or table '{1}' does not exist on the server".format(
                project, table))

    result = SortedSet()
    for d in res:
        result.add(d['identifier'])
    return result

def table_fullname(project, table):
    return "{0}.{1}".format(project, table)

def main():
    has_deletes = False
    for proj in projects:
        proj.process()
        has_deletes = has_deletes or proj.has_deletes

    if has_deletes:
        cmd = 'opal_upload'
        logging.info("Data deletion files have been created. Now launching {0} process...".format(cmd))
        run_command([cmd])

if __name__ == '__main__':
    setup()
    main()
