# This file contains code that is shared between opal_upload and opal_permission_revoke. There is no real reason for
# it being here except to prevent code duplication.

from __future__ import print_function, division

import sys
import logging
import subprocess
from ConfigParser import ConfigParser, NoSectionError, NoOptionError
from urllib import quote_plus
from StringIO import StringIO
from os.path import expanduser, isfile
import contextlib

verbose = '-v' in sys.argv
quiet = '-q' in sys.argv


auth_params = []

@contextlib.contextmanager
def setup_loader(configfile):
    """This context manager opens the config file, reads shared options and does error handling. Tool specific options
    can be specified in the code block."""
    global url, auth_params, logfile

    config = ConfigParser()
    configfile = expanduser(configfile)

    try:
        with open(configfile) as conf:
            config.readfp(conf)

            logfile = expanduser(config.get('main', 'logfile'))
            url = config.get('main', 'url')
            if not url.startswith('https'):
                raise KnownError("Opal url must be a secure (https) url. Found '{}'".format(url))
            if config.has_option('main', 'use_certificate') and \
                    config.getboolean('main', 'use_certificate'):
                auth_params += ['-o', url, '-sc', config.get('main', 'ssl_cert'), '-sk', config.get('main', 'ssl_key')]
            else:
                auth_params += ['-o', url, '-u', config.get('main', 'user'), '-p', config.get('main', 'password')]

            configure_logging()

            yield config

    except IOError as e:
        if e.errno == 2:
            handle_exception(KnownError("Configuration file not found: "+configfile))
            sys.exit(1)
        raise
    except (NoSectionError, NoOptionError) as e:
        handle_exception(KnownError("Error reading configuration file: "+str(e)))
        sys.exit(1)
    except KnownError as e:
        handle_exception(e)
        sys.exit(1)


## Configure logging ##

# define an intermediate level for info that should by default be logged to the logfile but not to the command line
LOGINFO = logging.INFO - 5
logging.addLevelName(LOGINFO, 'LOGINFO')
def configure_logging():
    logging.getLogger().setLevel(logging.DEBUG)

    logfilehandler = logging.FileHandler(logfile)
    logfilehandler.setLevel(LOGINFO)
    logfilehandler.setFormatter(logging.Formatter('%(levelname)-7s %(asctime)s: %(message)s'))
    logging.getLogger().addHandler(logfilehandler)

    stderrhandler = logging.StreamHandler()
    stderrhandler.setLevel(logging.INFO)
    if verbose:
        stderrhandler.setLevel(logging.DEBUG)
        logfilehandler.setLevel(logging.DEBUG)
    if quiet:
        stderrhandler.setLevel(logging.WARN)
    logging.getLogger().addHandler(stderrhandler)


MAX_URL_SIZE = 2000


def u(it):
    """url-encode string/unicode"""
    return quote_plus(str(it))

# This exception indicates that this is a known error condition handled by the code, so no need to print stacktraces
# and such.
class KnownError (Exception):
    pass


def run_rest_command_params(url, params, method, progresscallback=lambda: None):
    """
    Call a rest url with the list of parameters. If there are too many parameters to fit in a single request,
    split them up into multiple requests.

    If the list of parameters is empty, no request will be made.
    """
    # Sending a request with too many parameters results in an error 413 FULL HEAD,

    url += '?'
    urlwriter = StringIO()
    urlwriter.write(url)

    def send():
        # if no parameters, return
        if urlwriter.len == len(url): return
        # strip off last '&'
        run_rest_command(urlwriter.getvalue()[:-1], method=method)
        urlwriter.seek(len(url)); urlwriter.truncate()
        progresscallback()

    for param in params:
        if urlwriter.len + len(param) > MAX_URL_SIZE:
            send()
        urlwriter.write(param+'&')
    send()


def run_rest_command(url, method=None):
    m = []
    if method != None:
        m = ['-m', method]
    return run_command(['opal', 'rest'] + auth_params + ['-v', url] + m)

def run_command(cmd):
    logging.log(LOGINFO, 'executing ' + ' '.join(hide_password(cmd)))
    process = None
    try:
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = process.communicate()
        retcode = process.wait()
        output = err+'\n'+out
        logging.debug(output)
        if retcode != 0:
            logging.log(LOGINFO, 'subcommand exited with return code {0}: {1}'.format(
                retcode, limit_length(' '.join(hide_password(cmd)))))
            if '401 Unauthorized' in output:
                raise KnownError("Authorization failed, make sure your username and password are correct")
            if 'Could not resolve host' in output:
                raise KnownError("Could not resolve hostname: "+url)
            error = subprocess.CalledProcessError(retcode, hide_password(cmd))
            error.output = output
            raise error
        return out
    except:
        if process:
            if process.poll() is None:
                process.kill()
            process.wait()
        raise

def hide_password(cmd):
    try:
        password_idx = cmd.index('-p')
        return cmd[:password_idx+1] + ['{password_not_logged}'] + cmd[password_idx+2:]
    except ValueError:
        return cmd

def limit_length(msg, maxlen=1000, end=".....rest of message not shown...."):
    if len(msg) > maxlen:
        msg = msg[:1000-len(end)] + end
    return msg

def handle_exception(e):
    if isinstance(e, KnownError):
        logging.error('Error: ' + str(e))
        return

    msg = "Error: {0}: {1}".format(type(e).__name__, e)
    if isinstance(e, subprocess.CalledProcessError):
        msg += '\nCommand output: ' + e.output.rstrip()

    logging.error(msg)
    if not verbose:
        print("Run '{0} -v' for more information".format(sys.argv[0]), file=sys.stderr)
    logging.debug(e, exc_info=True)
