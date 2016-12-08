#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial implementation
#
init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {

  USAGE="
Usage on Linux 
  docker run --rm -it --cap-add SYS_ADMIN --device /dev/fuse
            --name che-mount
            -v \${HOME}/.ssh:\${HOME}/.ssh
            -v /etc/group:/etc/group:ro 
            -v /etc/passwd:/etc/passwd:ro 
            -v <path-to-sync-profile>:/profile
            -u \$(id -u \${USER})
            -v <local-mount>/:/mnthost eclipse/che-mount <workspace-id|workspace-name>

     <workspace-id|workspace-name> ID or Name of the workspace or namespace:workspace-name

Usage on Mac or Windows:
  docker run --rm -it --cap-add SYS_ADMIN --device /dev/fuse
            --name che-mount 
            -v <path-to-sync-profile>:/profile
            -v <local-mount>/:/mnthost eclipse/che-mount <workspace-id|workspace-name>

     <workspace-id|workspace-name> ID or Name of the workspace or namespace:workspace-name
"
 UNISON_REPEAT_DELAY_IN_SEC=2
 WORKSPACE_NAME=
 COMMAND_EXTRA_ARGS=
}

parse_command_line () {
  if [ $# -eq 0 ]; then
    usage
    return 1
  fi

  # See if profile document was provided
  mkdir -p $HOME/.unison
  cp -rf /profile/default.prf $HOME/.unison/default.prf

  WORKSPACE_NAME=$1
  shift
  COMMAND_EXTRA_ARGS="$*"
}

usage () {
  printf "%s" "${USAGE}"
}

info() {
  printf  "${GREEN}INFO:${NC} %s\n" "${1}"
}

debug() {
  printf  "${BLUE}DEBUG:${NC} %s\n" "${1}"
}

error() {
  echo  "---------------------------------------"
  echo "!!!"
  echo "!!! ${1}"
  echo "!!!"
  echo  "---------------------------------------"
  return 1
}

stop_sync() {
  echo "Received interrupt signal. Exiting."
  exit 1
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -u

# on callback, kill the last background process, which is `tail -f /dev/null` and execute the specified handler
trap 'stop_sync' SIGHUP SIGTERM SIGINT

init_logging
init_global_variables
parse_command_line "$@"
status=$?
if [ $status -ne 0 ]; then
    exit 1
fi

docker run --rm  -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action:${CHE_VERSION} get-ssh-data ${WORKSPACE_NAME} ${COMMAND_EXTRA_ARGS} > $HOME/env
if [ $? -ne 0 ]; then
    error "ERROR: Error when trying to get workspace data for workspace named ${WORKSPACE_NAME}"
    echo "List of workspaces are:"
    docker run --rm  -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action:${CHE_VERSION} list-workspaces
    return 1
fi

source $HOME/env

# store private key
mkdir $HOME/.ssh
echo "${SSH_PRIVATE_KEY}" > $HOME/.ssh/id_rsa
chmod 600 $HOME/.ssh/id_rsa

info "INFO: (che mount): Mounting ${SSH_USER}@${SSH_IP}:/projects with SSHFS"
sshfs ${SSH_USER}@${SSH_IP}:/projects /mntssh -p ${SSH_PORT} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no

status=$?
if [ $status -ne 0 ]; then
    error "ERROR: Fatal error occurred ($status)"
    exit 1
fi
info "INFO: (che mount): Successfully mounted ${SSH_USER}@${SSH_IP}:/projects (${SSH_PORT})"
info "INFO: (che mount): Initial sync...Please wait."
unison /mntssh /mnthost -batch -fat -silent -auto -prefer=newer -log=false > /dev/null 2>&1
status=$?
if [ $status -ne 0 ]; then
    error "ERROR: Fatal error occurred ($status)"
    exit 1
fi
info "INFO: (che mount): Background sync continues every ${UNISON_REPEAT_DELAY_IN_SEC} seconds."
info "INFO: (che mount): This terminal will block while the synchronization continues."
info "INFO: (che mount): To stop, issue a SIGTERM or SIGINT, usually CTRL-C."

# run application
unison /mntssh /mnthost -batch -retry 10 -fat -silent -copyonconflict -auto -prefer=newer -repeat=${UNISON_REPEAT_DELAY_IN_SEC} -log=false > /dev/null 2>&1
