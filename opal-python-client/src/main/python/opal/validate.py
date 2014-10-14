#! /usr/bin/env python
#

import argparse

import opal.core
import opal.protobuf.Commands_pb2



# TO BE REMOVED - START
parser = argparse.ArgumentParser(description='Opal command line.')
subparsers = parser.add_subparsers(title='sub-commands',
                                   help='Available sub-commands. Use --help option on the sub-command ')

def add_opal_arguments(parser):
    """
    Add Opal access arguments
    """
    parser.add_argument('--opal', '-o', required=False, help='Opal server base url')
    parser.add_argument('--user', '-u', required=False, help='User name')
    parser.add_argument('--password', '-p', required=False, help='User password')
    parser.add_argument('--ssl-cert', '-sc', required=False, help='Certificate (public key) file')
    parser.add_argument('--ssl-key', '-sk', required=False, help='Private key file')
    parser.add_argument('--verbose', '-v', action='store_true', help='Verbose output')


def add_subcommand(name, help, add_args_func, default_func):
    """
    Make a sub-parser, add default arguments to it, add sub-command arguments and set the sub-command callback function.
    """
    subparser = subparsers.add_parser(name, help=help)
    add_opal_arguments(subparser)
    add_args_func(subparser)
    subparser.set_defaults(func=default_func)

# TO BE REMOVED - END



class Validator:

    def __init__(self, client, datasource, table, variable=None):
        self.client = client
        self.datasource = datasource
        self.table = table
        self.variable = variable

    def submit(self):
        options = opal.protobuf.Commands_pb2.ValidateCommandOptionsDto()
        options.project = self.datasource
        options.table = self.table
        if self.variable is not None:
            options.variable = self.variable

        # submit validate job
        request = self.client.new_request()
        request.fail_on_error().accept_json().content_type_protobuf()

        #print options
        uri = opal.core.UriBuilder(['project', self.datasource, 'commands', '_validate']).build()
        response = request.post().resource(uri).content(options.SerializeToString()).send()

        print response


def add_arguments(parser):
    parser.add_argument('--datasource', '-d', required=True, help='Datasource name')
    parser.add_argument('--table', '-t', required=True, help='Table to be validated')
    parser.add_argument('--variable', '-va', required=False, help='Variable to be validated (defaults to all)')


def do_command(args):
    try:
        client = opal.core.OpalClient.build(opal.core.OpalClient.LoginInfo.parse(args))
        validator = Validator(client=client, datasource=args.datasource, table=args.table, variable=args.variable)
        validator.submit()

    except Exception as e:
        print e



# TO BE REMOVED - START
add_subcommand('validate', "Validate a table/view or variable", add_arguments,
               do_command)

args = parser.parse_args()
args.func(args)
# TO BE REMOVED - END

