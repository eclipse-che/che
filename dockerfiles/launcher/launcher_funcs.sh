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
  error "     ${1}"
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
  get_che_image_version ${LAUNCHER_IMAGE_VERSION}
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

docker_run() {
   ENV_FILE=$(get_list_of_che_system_environment_variables)
   docker run -d --name "${CHE_SERVER_CONTAINER_NAME}" \
    -v /var/run/docker.sock:/var/run/docker.sock:Z \
    -v "$CHE_DATA_LOCATION" \
    -p "${CHE_PORT}":"${CHE_PORT}" \
    --restart="${CHE_RESTART_POLICY}" \
    -e "CHE_LOG_LEVEL=${CHE_LOG_LEVEL}" \
    -e "CHE_IP=$CHE_HOST_IP" \
    --env-file=$ENV_FILE \
    "$@"
   rm -rf $ENV_FILE > /dev/null
}

get_user_id() {
   CHE_USER_UID=$(docker run -t \
     -v /etc/passwd:/etc/passwd:ro,Z \
     -v /etc/group:/etc/group:ro,Z \
     alpine id -u ${CHE_USER})
   CHE_USER_GID=$(docker run -t \
     -v /etc/passwd:/etc/passwd:ro,Z \
     -v /etc/group:/etc/group:ro,Z \
      alpine getent group docker | cut -d: -f3)
   echo -n "${CHE_USER_UID}" | tr '\r' ':'; echo -n ${CHE_USER_GID}
}

docker_run_with_che_user() {
   if [ "${CHE_USER}" != "root" ]; then
     docker_run -e CHE_USER=${CHE_USER} \
      -v /etc/group:/etc/group:ro,Z \
      -v /etc/passwd:/etc/passwd:ro,Z \
      --user=$(get_user_id) \
      "$@"
   else
     docker_run --user="${CHE_USER}" "$@"
   fi
}

docker_run_if_in_vm() {
  # If the container will run inside of a VM, additional parameters must be set.
  # Setting CHE_IN_VM=true will have the che-server container set the values.
  if is_docker_for_mac || is_docker_for_windows || is_boot2docker; then
    docker_run_with_che_user -e "CHE_IN_VM=true" "$@"
  else
    docker_run_with_che_user "$@"
  fi
}

docker_run_with_assembly() {
  if has_assembly; then
    docker_run_if_in_vm -v "$CHE_ASSEMBLY_LOCATION" -e "CHE_ASSEMBLY=${CHE_ASSEMBLY}" "$@"
  else
    docker_run_if_in_vm "$@"
  fi
}

docker_run_with_conf() {
  if has_che_conf_path; then
    docker_run_with_assembly -v "$CHE_CONF_LOCATION" -e "CHE_LOCAL_CONF_DIR=${CHE_CONF}" "$@"
  else
    docker_run_with_assembly "$@"
  fi
}

docker_run_with_external_hostname() {
  if has_external_hostname; then
    docker_run_with_conf -e "CHE_DOCKER_MACHINE_HOST_EXTERNAL=${CHE_DOCKER_MACHINE_HOST_EXTERNAL}" "$@"
  else
    docker_run_with_conf "$@"
  fi
}

docker_run_with_debug() {
  if has_debug && has_debug_suspend; then
    docker_run_with_external_hostname -p "${CHE_DEBUG_SERVER_PORT}":8000 \
                                      -e "CHE_DEBUG_SERVER=true" \
                                      -e "JPDA_SUSPEND=y" "$@"
  elif has_debug; then
    docker_run_with_external_hostname -p "${CHE_DEBUG_SERVER_PORT}":8000 \
                                      -e "CHE_DEBUG_SERVER=true" "$@"
  else
    docker_run_with_external_hostname "$@"
  fi
}

has_debug_suspend() {
  if [ "${CHE_DEBUG_SERVER_SUSPEND}" = "false" ]; then
    return 1
  else
    return 0
  fi
}

has_debug() {
  if [ "${CHE_DEBUG_SERVER}" = "false" ]; then
    return 1
  else
    return 0
  fi
}

has_che_conf_path() {
  if [ "${CHE_CONF}" = "" ]; then
    return 1
  else
    return 0
  fi
}

has_assembly() {
  if [ "${CHE_ASSEMBLY}" = "" ]; then
    return 1
  else
    return 0
  fi
}

has_external_hostname() {
  if [ "${CHE_DOCKER_MACHINE_HOST_EXTERNAL}" = "" ]; then
    return 1
  else
    return 0
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
  docker version --format '{{.Server.Version}}' | grep "1\.[0-9]*\.[0-9]*"
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

che_container_exist_by_name() {
  docker inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else 
    return 1
  fi 
}

che_container_exist() {
  if [ "$(docker ps -aq  -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_stopped() {
  if [ "$(docker ps -qa -f "status=exited" -f "name=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

contains() {
    string="$1"
    substring="$2"
    if test "${string#*$substring}" != "$string"
    then
        return 0    # $substring is in $string
    else
        return 1    # $substring is not in $string
    fi
}

has_container_debug () {
  if $(contains $(get_container_debug $1) "<nil>"); then
    return 1
  else
    return 0
  fi
}

get_container_debug() {
  CURRENT_CHE_DEBUG=$(docker inspect --format='{{.NetworkSettings.Ports}}' ${1})
  IFS=$' '
  for SINGLE_BIND in $CURRENT_CHE_DEBUG; do
    case $SINGLE_BIND in
      *8000/tcp:*)
        echo $SINGLE_BIND | cut -f2 -d":"
      ;;
      *)
      ;;
    esac
  done
}

get_che_container_host_ip_from_container() {
  BINDS=$(docker inspect --format="{{.Config.Env}}" "${1}" | cut -d '[' -f 2 | cut -d ']' -f 1)

  IFS=$' '
  for SINGLE_BIND in $BINDS; do
    case $SINGLE_BIND in
      *CHE_IP*)
        echo $SINGLE_BIND | cut -f2 -d=
      ;;
      *)
      ;;
    esac
  done
}

get_che_container_host_bind_folder() {
  BINDS=$(docker inspect --format="{{.HostConfig.Binds}}" "${2}" | cut -d '[' -f 2 | cut -d ']' -f 1)
  IFS=$' '
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
  FOLDER=$(get_che_container_host_bind_folder "/conf:Z" $1)
  echo "${FOLDER:=not set}"
}

get_che_container_data_folder() {
  FOLDER=$(get_che_container_host_bind_folder "/home/user/che/workspaces:Z" $1)
  echo "${FOLDER:=not set}"
}

get_che_container_image_name() {
  docker inspect --format="{{.Config.Image}}" "${1}"
}

get_che_image_version() {
  image_version=$(echo ${1} | cut -d : -f2 -s)
  if [ -n "${image_version}" ]; then
    echo "${image_version}"
  else
    echo "latest"
  fi
}

get_che_server_container_id() {
  docker inspect -f '{{.Id}}' ${1}
}

get_docker_external_hostname() {
  if is_docker_for_mac || is_docker_for_windows; then
    echo "localhost"
  else
    echo ""
  fi
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_running ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

wait_until_container_is_stopped() {
  CONTAINER_STOP_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_stopped ${2} || [ ${ELAPSED} -eq "${CONTAINER_STOP_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  HTTP_STATUS_CODE=$(curl -I http://$(docker inspect -f '{{.NetworkSettings.IPAddress}}' "${1}"):$CHE_PORT/api/ \
                     -s -o /dev/null --write-out "%{http_code}")
  if [ "${HTTP_STATUS_CODE}" = "200" ]; then
    return 0
  else
    return 1
  fi
}

get_server_version() {
  HTTP_STATUS_CODE=$(curl -X OPTIONS http://$(docker inspect -f '{{.NetworkSettings.IPAddress}}' \
                          "${1}"):$CHE_PORT/api/ -s)

  FIRST=${HTTP_STATUS_CODE//\ /}
  IFS=','
  for SINGLE_BIND in $FIRST; do
    case $SINGLE_BIND in
      *implementationVersion*)
        echo ${SINGLE_BIND//\"} | cut -f2 -d":" | cut -f1 -d"}"
      ;;
      *)
      ;;
    esac
  done
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted ${2} || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
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
