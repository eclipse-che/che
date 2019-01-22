#!/bin/bash
#
# Copyright (c) 2018-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

export USER_ID=$(id -u)
export GROUP_ID=$(id -g)

if ! grep -Fq "${USER_ID}" /etc/passwd; then
    # current user is an arbitrary
    # user (its uid is not in the
    # container /etc/passwd). Let's fix that
    cat ${HOME}/passwd.template | \
    sed "s/\${USER_ID}/${USER_ID}/g" | \
    sed "s/\${GROUP_ID}/${GROUP_ID}/g" | \
    sed "s/\${HOME}/\/home\/theia/g" > /etc/passwd

    cat ${HOME}/group.template | \
    sed "s/\${USER_ID}/${USER_ID}/g" | \
    sed "s/\${GROUP_ID}/${GROUP_ID}/g" | \
    sed "s/\${HOME}/\/home\/theia/g" > /etc/group
fi

# Grant access to projects volume in case of non root user with sudo rights
if [ "$(id -u)" -ne 0 ] && command -v sudo >/dev/null 2>&1 && sudo -n true > /dev/null 2>&1; then
    sudo chown ${USER_ID}:${GROUP_ID} /projects
fi

if [ -z "$THEIA_PORT" ]; then
    export THEIA_PORT=3100
else
    # Parse THEIA_PORT env var in case it has weird value, such as tcp://10.108.137.206:3000
    theia_port_number_regexp='^[0-9]+$'
    if ! [[ "$THEIA_PORT" =~ $theia_port_number_regexp ]]; then
        # THEIA_PORT contains something other than a number
        theia_port_tcp_uri_regexp='^tcp://[0-9a-zA-Z.]+:[0-9]+$'
        if [[ "$THEIA_PORT" =~ $theia_port_tcp_uri_regexp ]]; then
            # Remove tcp://....: prefix
            THEIA_PORT=${THEIA_PORT##*:}
        else
            echo  1>&2 "Environment variable THEIA_PORT contains unexpected value:$THEIA_PORT"
        fi
    fi
fi

# SITTERM / SIGINT
responsible_shutdown() {
  echo ""
  echo "Received SIGTERM"
  kill -SIGINT ${PID}
  wait ${PID}
  exit;
}

set -e
set +o posix

# setup handlers
# on callback, kill the last background process, which is `tail -f /dev/null` and execute the specified handler
trap 'responsible_shutdown' SIGHUP SIGTERM SIGINT

cd ${HOME}

# disable CDN based on an environment variable.
shopt -s nocasematch
if [ "${NOCDN}" == "true" ]; then
  sed -i 's/\.buildScripts/.buildScriptsWithoutCdn/' lib/index.html
fi
shopt -u nocasematch

# run che
node src-gen/backend/main.js /projects --hostname=0.0.0.0 --port=${THEIA_PORT} &

PID=$!

# See: http://veithen.github.io/2014/11/16/sigterm-propagation.html
wait ${PID}
wait ${PID}
EXIT_STATUS=$?

# wait forever
while true
do
  tail -f /dev/null & wait ${!}
