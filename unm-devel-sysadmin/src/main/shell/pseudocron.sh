#!/bin/sh

# Run a script regularly.
#
# This is intended for the "macos_job-xcodebuild_test.sh" script, which
# runs well as a standalone script, but fails when run from cron. authd.log says:
# Failed to authorize right 'system.privilege.taskport.debug' by client
# '/usr/libexec/taskgated' [13] for authorization created by '/usr/libexec/taskgated'

# Parameters:
#   "${1}" The interval to wait before the next run, in sec. e.g. 300
#   "${2}" The script to run, e.g. /path/to/my/script.sh
#   "${@}" Arguments to pass to the script

WAIT="${1}"

shift

while true; do

   $@

   sleep "${WAIT}"

done
