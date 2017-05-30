#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Marian Labuda - Initial Implementation

#export CLI_IMAGE=$IMAGE_NAME
source /dockerfiles/base/scripts/base/*.sh
export SCRIPTS_DIR="${BATS_BASE_DIR}"/base/scripts/base
export TESTS_DIR="${BATS_BASE_DIR}"/cli/tests
export TESTRUN_DIR="${TESTS_DIR}"/testrun
export CONTAINER_TESTRUN_DIR=/dockerfiles/cli/tests/testrun
if [ -d "${TESTRUN_DIR}" ]; then
 rm -rf "${TESTRUN_DIR}"
fi
mkdir "${TESTRUN_DIR}" -p

kill_running_named_container() {
  if [[ $(docker ps --format '{{.Names}}' | grep $1 | wc -l) -eq 1 ]]; then
    echo "Stopping named container $1"
    docker kill $1 1>/dev/null
  fi
}

remove_named_container() {
  if [[ $(docker ps -a --format '{{.Names}}' | grep $1 | wc -l) -eq 1 ]]; then
    echo "Removing named container $1"
    docker rm $1 1>/dev/null
  fi
}

# Pass a port as an argument to check whether is free or not
# Returns 0 if port is free (not listening), 1 otherwise
port_is_free() {
  if [[ $(netstat -lnt | awk -v port=$1 '$6 == "LISTEN" && $4 ~ "."port' | wc -l) -gt 0 ]]; then
    return 1
  else 
    return 0
  fi
}

# Get first free port from range of dynamic/private ports
get_free_port() {
  local port=49200
  while [[ $(port_is_free $port) -eq 1 ]]; do
    if [[ $port -eq 65535 ]]; then
       echo ""
       return 1
    fi
    port=$((port+1))
  done
  echo $port
}

