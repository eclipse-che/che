#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Marian Labuda - Initial Implementation

#export CLI_IMAGE=$IMAGE_NAME
source /dockerfiles/base/scripts/base/*.sh
export SCRIPTS_DIR="${BATS_BASE_DIR}"/base/scripts/base
export TESTS_DIR="${BATS_BASE_DIR}"/cli/tests
export TESTRUN_DIR="${TESTS_DIR}"/testrun
export CONTAINER_TESTRUN_DIR=/dockerfiles/cli/tests/testrun

if [ -d "${CONTAINER_TESTRUN_DIR}" ]; then
 rm -rf "${CONTAINER_TESTRUN_DIR}"
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

check_che_state() {
  local CHE_CONTAINER_NAME="che"
  local CHE_PORT="8080"
  local IS_RUNNING="true"
  for i in "${@}"
  do
      case $i in
         --che-container-name=*)
             CHE_CONTAINER_NAME="${i#*=}"
             shift
         ;;
         --che-port=*)
             CHE_PORT="${i#*=}"
             shift
         ;;
         --is-running=*)
             IS_RUNNING="${i#*=}"
             shift
         ;;
         *)
              echo "You've passed unknown option"
              exit 2
          ;;
      esac
  done
  [[ "$(docker inspect --format='{{.State.Running}}' $CHE_CONTAINER_NAME)" == "$IS_RUNNING" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} $CHE_CONTAINER_NAME)
  curl -fsS http://${ip_address}:$CHE_PORT  > /dev/null
}

execute_cli_command() {
  local CHE_CONTAINER_NAME="che"
  local CHE_DATA_PATH=""
  local CHE_CLI_COMMAND=""
  local CHE_CLI_EXTRA_OPTIONS=""
  local CHE_PORT="8080"
  local DATA_VOLUME=""
  local USE_DOCKER_SOCK="true"
  local MOUNT_SCRIPTS="true"

  for i in "${@}"
  do
      case $i in
         --che-container-name=*)
             CHE_CONTAINER_NAME="${i#*=}"
             shift
         ;;
         --che-port=*)
             CHE_PORT="${i#*=}"
             shift
         ;;
         --che-data-path=*)
             CHE_DATA_PATH="${i#*=}"
             shift
         ;;
         --che-cli-command=*)
             CHE_CLI_COMMAND="${i#*=}"
             shift
         ;;
         --che-cli-extra-options=*)
             CHE_CLI_EXTRA_OPTIONS="${i#*=}"
             shift
         ;;
         --che-cli-use-docker-sock=*)
             USE_DOCKER_SOCK="${i#*=}"
             shift
         ;;
         --che-cli-mount-scripts=*)
             MOUNT_SCRIPTS="${i#*=}"
             shift
         ;;
         *)
              echo "You've passed unknown option"
              exit 2
          ;;
      esac
  done

  if [ ! -z $CHE_DATA_PATH ]; then
    DATA_VOLUME="-v ${CHE_DATA_PATH}:/data"
  fi
  if [ $USE_DOCKER_SOCK == "true" ]; then
    DOCKER_SOCK_VOLUME="-v /var/run/docker.sock:/var/run/docker.sock"
  fi
  if [ $MOUNT_SCRIPTS == "true" ]; then
    SCRIPTS_VOLUME="-v ${SCRIPTS_DIR}:/scripts/base"
  fi
  if [ $CHE_PORT -ne 8080 ]; then
     CLI_CUSTOM_PORT="-e CHE_PORT=${CHE_PORT}"
  fi
  if [ $CHE_CONTAINER_NAME != "che" ]; then
     CLI_CUSTOM_CHE_CONTAINER_NAME="-e CHE_CONTAINER=${CHE_CONTAINER_NAME}"
  fi

  docker run --rm ${CLI_CUSTOM_PORT} ${SCRIPTS_VOLUME} ${DOCKER_SOCK_VOLUME} ${DATA_VOLUME} ${CLI_CUSTOM_CHE_CONTAINER_NAME} $CLI_IMAGE ${CHE_CLI_COMMAND} ${CHE_CLI_EXTRA_OPTIONS}
}
