#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
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
Usage:
  docker run --rm -it --cap-add SYS_ADMIN --device /dev/fuse 
             -v <local-mount>/:/mnthost codenvy/che-mount <ip> <port>
     <local-mount>    Host directory to sync files, must end with a slash '/'
     <ip>             IP address of Che server
     <port>           Port of workspace SSH server - retrieve inside workspace
"
}

parse_command_line () {
  if [ $# -eq 0 ]; then
    usage
    exit
  fi
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
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
}

error_exit() {
  echo  "---------------------------------------"
  error "!!!"
  error "!!! ${1}"
  error "!!!"
  echo  "---------------------------------------"
  exit 1
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

init_logging
init_global_variables
parse_command_line "$@"

sshfs user@$1:/projects /mntssh -p $2
unison /mntssh /mnthost -batch -fat -silent -auto -prefer=newer > /dev/null 2>&1

info "INFO: ECLIPSE CHE: Successfully mounted user@$1:/projects"
 
while :
do
    unison /mntssh /mnthost -batch -fat -silent -auto -prefer=newer > /dev/null 2>&1
    sleep 1
done
