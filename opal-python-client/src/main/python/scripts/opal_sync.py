#!/usr/bin/python

from __future__ import print_function, division
from sortedcontainers import SortedSet

import json
import time

import opal.protobuf.Magma_pb2
from opal_tools_lib import *
import pycurl

verbose = '-v' in sys.argv
quiet = '-q' in sys.argv

CONFIGFILE = '~/.opal/datasync.conf'

def setup():
    global skipped_tables, logfile, projects, login_info

    with setup_loader(CONFIGFILE) as config:
        login_info = get_login_info()
        skipped_tables = [p.strip() for p in config.get('main', 'skipped_tables').split(',')]
        logfile = expanduser(config.get('main', 'logfile'))
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
        self.remote_project = config.get(section, 'remote_project')
        self.local_project = config.get(section, 'local_project')

    def process(self):
        logging.info("Processing remote project '{0}' at {1}".format(self.remote_project, self.section))
        #@TODO: check/create local project

        remote_tables = get_tables(self.remote_project, login=self.login_info)
        local_tables = get_tables(self.local_project)

        for table in remote_tables.keys():
            if table not in skipped_tables:
                logging.info('Synchronizing table {0}'.format(table))
                if not local_tables.has_key(table):
                    create_table(self.local_project, table, remote_tables[table])

                self.check_removals(table)

    def check_removals(self, table):
        local_ids = get_entity_ids(self.local_project, table)
        remote_ids = get_entity_ids(self.remote_project, table, login=self.login_info)
        local_ids.difference_update(remote_ids) #keep only what should be deleted
        if len(local_ids) > 0:
            print('ids to remove: {0}'.format(local_ids))
            #@TODO: create a file with ids and put it in the automated data import input folder

def create_table(project, table, entity_type):
    """Creates a new table in the project"""

    dto = opal.protobuf.Magma_pb2.TableDto()
    dto.name = table
    dto.entityType = entity_type
    rest_post('/datasource/{0}/tables'.format(project), dto.SerializeToString())
    print('Created table {0}.{1}, with entity type {1}'.format(project, table, entity_type))

def get_tables(project, login=login_info):
    """Returns a dict of table name -> entity type for the given project and auth settings"""

    try:
        res = json.loads(rest_call('/datasource/{0}/tables'.format(project), login=login))
    except pycurl.error as e:
        if error_code(e) == 404:
            raise KnownError("Project '{0}' does not exist on the server".format(project))

    return {t['name']: t['entityType'] for t in res}

def get_entity_ids(project, table, login=login_info):
    """Returns a sortedcontainers SortedSet with all the ids in the given project/table"""

    try:
        res = json.loads(rest_call('/datasource/{0}/table/{1}/entities'.format(project, table), login=login))
    except pycurl.error as e:
        if error_code(e) == 404:
            raise KnownError("Project '{0}' or table '{1}' does not exist on the server".format(
                project, table))

    result = SortedSet()
    for d in res:
        result.add(d['identifier'])
    return result

def main():
    #print(projects)
    for proj in projects:
        proj.process()


if __name__ == '__main__':
    setup()
    main()

