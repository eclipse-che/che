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

convert_windows_to_posix() {
  # "/some/path" => /some/path
  OUTPUT_PATH=${1//\"}
  echo "/"$(echo "$OUTPUT_PATH" | sed 's/\\/\//g' | sed 's/://')
}

get_clean_path() {
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_converted_and_clean_path() {
  CONVERTED_PATH=$(convert_windows_to_posix "${1}")
  CLEAN_PATH=$(get_clean_path "${CONVERTED_PATH}")
  echo $CLEAN_PATH
}

get_che_launcher_container_id() {
  hostname
}

get_che_launcher_version() {
  if [ -n "${LAUNCHER_IMAGE_VERSION}" ]; then
    echo "${LAUNCHER_IMAGE_VERSION}"
  else
    echo "latest"
  fi
}

is_boot2docker() {
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_ip() {
  if [ "${ETH0_ADDRESS}" = "10.0.75.2" ]; then
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

get_list_of_che_system_environment_variables() {
  # See: http://stackoverflow.com/questions/4128235/what-is-the-exact-meaning-of-ifs-n
  IFS=$'\n'
  
  DOCKER_ENV=$(mktemp)

  # First grab all known CHE_ variables
  CHE_VARIABLES=$(env | grep CHE_)
  for SINGLE_VARIABLE in "${CHE_VARIABLES}"; do
    echo "${SINGLE_VARIABLE}" >> $DOCKER_ENV
  done

  # Add in known proxy variables
  if [ ! -z ${http_proxy+x} ]; then 
    echo "http_proxy=${http_proxy}" >> $DOCKER_ENV
  fi

  if [ ! -z ${https_proxy+x} ]; then 
    echo "https_proxy=${https_proxy}" >> $DOCKER_ENV
  fi

  if [ ! -z ${no_proxy+x} ]; then 
    echo "no_proxy=${no_proxy}" >> $DOCKER_ENV
  fi

  echo $DOCKER_ENV
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
  case $(get_docker_install_type) in
   boot2docker)
     echo $ETH1_ADDRESS
   ;;
   native)
     echo $DOCKER0_ADDRESS
   ;;
   *)
     echo $ETH0_ADDRESS
   ;;
  esac
}

get_docker_host_os() {
  docker info | grep "Operating System:" | sed "s/^Operating System: //"
}

get_docker_daemon_version() {
  docker version | grep -i "server version:" | sed "s/^server version: //I"
}

get_che_hostname() {
  INSTALL_TYPE=$(get_docker_install_type)
  if [ "${INSTALL_TYPE}" = "boot2docker" ]; then
    echo $DEFAULT_DOCKER_HOST_IP
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
  if [ "$(docker ps -aq  -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_stopped() {
  if [ "$(docker ps -qa -f "status=exited" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}


get_che_container_host_bind_folder() {
  BINDS=$(docker inspect --format="{{.HostConfig.Binds}}" "${CHE_SERVER_CONTAINER_NAME}" | cut -d '[' -f 2 | cut -d ']' -f 1)

  for SINGLE_BIND in $BINDS; do
    case $SINGLE_BIND in
      *$1*)
        echo $SINGLE_BIND | cut -f1 -d":"
      ;;
      *)
      ;;
    esac
  done
}

get_che_container_conf_folder() {
  FOLDER=$(get_che_container_host_bind_folder "/conf")
  echo "${FOLDER:=not set}"
}

get_che_container_data_folder() {
  FOLDER=$(get_che_container_host_bind_folder "/home/user/che/workspaces")
  echo "${FOLDER:=not set}"
}

get_che_container_image_name() {
  docker inspect --format="{{.Config.Image}}" "${CHE_SERVER_CONTAINER_NAME}"
}

get_che_server_container_id() {
  docker ps -q -a -f "name=${CHE_SERVER_CONTAINER_NAME}"
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_running || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

wait_until_container_is_stopped() {
  CONTAINER_STOP_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_stopped || [ ${ELAPSED} -eq "${CONTAINER_STOP_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  HTTP_STATUS_CODE=$(curl -I http://$(docker inspect -f '{{.NetworkSettings.IPAddress}}' "${CHE_SERVER_CONTAINER_NAME}"):8080/api/ \
                     -s -o /dev/null --write-out "%{http_code}")
  if [ "${HTTP_STATUS_CODE}" = "200" ]; then
    return 0
  else
    return 1
  fi
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

execute_command_with_progress() {
  progress=$1
  command=$2
  shift 2

  pid=""

  case "$progress" in
    extended)
      $command "$@"
      ;;
    basic|*)
      $command "$@" &>/dev/null &
      pid=$!
      while kill -0 "$pid" >/dev/null 2>&1; do
        printf "#"
        sleep 10
      done
      wait $pid # return pid's exit code
      printf "\n"
    ;;
  esac
  printf "\n"
}
