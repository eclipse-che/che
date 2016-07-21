#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Mario Loriedo - Initial implementation
#

launcher_dir="$(dirname "$0")"
source "$launcher_dir/launcher_funcs.sh"
source "$launcher_dir/launcher_cmds.sh"

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {

  CHE_SERVER_CONTAINER_NAME="che-server"
  CHE_SERVER_IMAGE_NAME="codenvy/che-server"
  CHE_LAUNCHER_IMAGE_NAME="codenvy/che-launcher"

  # Possible Docker install types are:
  #     native, boot2docker or moby
  DOCKER_INSTALL_TYPE=$(get_docker_install_type)

  # User configurable variables
  DEFAULT_DOCKER_HOST_IP=$(get_docker_host_ip)
  DEFAULT_CHE_HOSTNAME=$(get_che_hostname)
  DEFAULT_CHE_PORT="8080"
  DEFAULT_CHE_VERSION=$(get_che_launcher_version)
  DEFAULT_CHE_RESTART_POLICY="no"
  DEFAULT_CHE_USER="root"
  DEFAULT_CHE_LOG_LEVEL="info"
  DEFAULT_CHE_DATA_FOLDER="/home/user/che"

  # Clean eventual user provided paths
  CHE_CONF_FOLDER=${CHE_CONF_FOLDER:+$(get_clean_path ${CHE_CONF_FOLDER})}
  CHE_DATA_FOLDER=${CHE_DATA_FOLDER:+$(get_clean_path ${CHE_DATA_FOLDER})}

  CHE_HOSTNAME=${CHE_HOSTNAME:-${DEFAULT_CHE_HOSTNAME}}
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}
  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_RESTART_POLICY=${CHE_RESTART_POLICY:-${DEFAULT_CHE_RESTART_POLICY}}
  CHE_USER=${CHE_USER:-${DEFAULT_CHE_USER}}
  CHE_HOST_IP=${CHE_HOST_IP:-${DEFAULT_DOCKER_HOST_IP}}
  CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}
  CHE_DATA_FOLDER=${CHE_DATA_FOLDER:-${DEFAULT_CHE_DATA_FOLDER}}

  # CHE_CONF_ARGS are the Docker run options that need to be used if users set CHE_CONF_FOLDER:
  #   - empty if CHE_CONF_FOLDER is not set
  #   - -v ${CHE_CONF_FOLDER}:/conf -e "CHE_LOCAL_CONF_DIR=/conf" if CHE_CONF_FOLDER is set
  CHE_CONF_ARGS=${CHE_CONF_FOLDER:+-v "${CHE_CONF_FOLDER}":/conf -e "CHE_LOCAL_CONF_DIR=/conf"}
  CHE_LOCAL_BINARY_ARGS=${CHE_LOCAL_BINARY:+-v ${CHE_LOCAL_BINARY}:/home/user/che}

  if is_docker_for_mac || is_docker_for_windows; then
    CHE_STORAGE_ARGS=${CHE_DATA_FOLDER:+-v "${CHE_DATA_FOLDER}/storage":/home/user/che/storage \
                                        -e "CHE_WORKSPACE_STORAGE=${CHE_DATA_FOLDER}/workspaces" \
                                        -e "CHE_WORKSPACE_STORAGE_CREATE_FOLDERS=false"}
  else
    CHE_STORAGE_ARGS=${CHE_DATA_FOLDER:+-v "${CHE_DATA_FOLDER}/storage":/home/user/che/storage \
                                        -v "${CHE_DATA_FOLDER}/workspaces":/home/user/che/workspaces}
  fi

  if [ "${CHE_LOG_LEVEL}" = "debug" ]; then
    CHE_DEBUG_OPTION="--debug --log_level:debug"
  else
    CHE_DEBUG_OPTION=""
  fi

  USAGE="
Usage:
  docker run -v /var/run/docker.sock:/var/run/docker.sock ${CHE_LAUNCHER_IMAGE_NAME} [COMMAND]
     start                              Starts Che server
     stop                               Stops Che server
     restart                            Restart Che server
     update                             Pull latest version of ${CHE_SERVER_IMAGE_NAME}
     info                               Print some debugging information

Docs: http://eclipse.org/che/getting-started.
"
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

print_debug_info() {
  debug "---------------------------------------"
  debug "---------  CHE DEBUG INFO   -----------"
  debug "---------------------------------------"
  debug ""
  debug "DOCKER_INSTALL_TYPE       = ${DOCKER_INSTALL_TYPE}"
  debug ""
  debug "CHE_SERVER_CONTAINER_NAME = ${CHE_SERVER_CONTAINER_NAME}"
  debug "CHE_SERVER_IMAGE_NAME     = ${CHE_SERVER_IMAGE_NAME}"
  debug ""
  VAL=$(if che_container_exist;then echo "YES"; else echo "NO"; fi)
  debug "CHE CONTAINER EXISTS?     ${VAL}"
  VAL=$(if che_container_is_running;then echo "YES"; else echo "NO"; fi)
  debug "CHE CONTAINER IS RUNNING? ${VAL}"
  VAL=$(if che_container_is_stopped;then echo "YES"; else echo "NO"; fi)
  debug "CHE CONTAINER IS STOPPED? ${VAL}"
  VAL=$(if server_is_booted;then echo "YES"; else echo "NO"; fi)
  debug "CHE SERVER IS BOOTED?     ${VAL}"
  debug ""
  debug "CHE_PORT                  = ${CHE_PORT}"
  debug "CHE_VERSION               = ${CHE_VERSION}"
  debug "CHE_RESTART_POLICY        = ${CHE_RESTART_POLICY}"
  debug "CHE_USER                  = ${CHE_USER}"
  debug "CHE_HOST_IP               = ${CHE_HOST_IP}"
  debug "CHE_LOG_LEVEL             = ${CHE_LOG_LEVEL}"
  debug "CHE_HOSTNAME              = ${CHE_HOSTNAME}"
  debug "CHE_DATA_FOLDER           = ${CHE_DATA_FOLDER}"
  debug "CHE_CONF_FOLDER           = ${CHE_CONF_FOLDER:-not set}"
  debug "CHE_LOCAL_BINARY          = ${CHE_LOCAL_BINARY:-not set}"
  debug ""
  debug "---------------------------------------"
  debug "---------------------------------------"
  debug "---------------------------------------"
}

get_che_launcher_container_id() {
  hostname
}

get_che_launcher_version() {
  LAUNCHER_CONTAINER_ID=$(get_che_launcher_container_id)
  LAUNCHER_IMAGE_NAME=$(docker inspect --format='{{.Config.Image}}' "${LAUNCHER_CONTAINER_ID}")
  echo "${LAUNCHER_IMAGE_NAME}" | cut -d : -f2
}

is_boot2docker() {
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_ip() {
  DOCKER_HOST_IP=$(get_docker_host_ip)
  if [ "${DOCKER_HOST_IP}" = "10.0.75.2" ]; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  if uname -r | grep -q 'moby' && ! has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  if uname -r | grep -q 'moby' && has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

get_docker_install_type() {
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}

get_docker_host_ip() {
  NETWORK_IF="eth0"
  if is_boot2docker; then
    NETWORK_IF="eth1"
  fi

  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
}

get_che_hostname() {
  INSTALL_TYPE=$(get_docker_install_type)
  if [ "${INSTALL_TYPE}" = "boot2docker" ] ||
     [ "${INSTALL_TYPE}" = "docker4windows" ]; then
    get_docker_host_ip
  else
    echo "localhost"
  fi
}

check_docker() {
  if [ ! -S /var/run/docker.sock ]; then
    error_exit "Docker socket (/var/run/docker.sock) hasn't be mounted \
inside the container. Verify the syntax of the \"docker run\" command."
  fi

  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    error_exit "Error when running \"docker ps\": ${output}"
  fi
}

che_container_exist() {
  if [ "$(docker ps -aq  -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_stopped() {
  if [ "$(docker ps -qa -f "status=exited" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_running || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  HTTP_STATUS_CODE=$(curl -I http://"${CHE_HOST_IP}":"${CHE_PORT}"/api/  \
                     -s -o /dev/null --write-out "%{http_code}")
  if [ "${HTTP_STATUS_CODE}" = "200" ]; then
    return 0
  else
    return 1
  fi
}

parse_command_line () {
  if [ $# -eq 0 ]; then
    usage
    container_self_destruction
    exit
  fi

  for command_line_option in "$@"; do
    case ${command_line_option} in
      start|stop|restart|update|info)
        CHE_SERVER_ACTION=${command_line_option}
      ;;
      -h|--help)
        usage
        container_self_destruction
        exit
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  done
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

init_logging
check_docker
init_global_variables
parse_command_line "$@"

case ${CHE_SERVER_ACTION} in
  start)
    start_che_server
  ;;
  stop)
    stop_che_server
  ;;
  restart)
    restart_che_server
  ;;
  update)
    update_che_server
  ;;
  info)
    print_debug_info
  ;;
esac

# This container will self destruct after execution
container_self_destruction
