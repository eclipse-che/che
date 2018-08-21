#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

convert_windows_to_posix() {
#  debug $FUNCNAME
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

convert_posix_to_windows() {
#  debug $FUNCNAME
  # Remove leading slash
  VALUE="${1:1}"

  # Get first character (drive letter)
  VALUE2="${VALUE:0:1}"

  # Replace / with \
  VALUE3=$(echo ${VALUE} | tr '/' '\\' | sed 's/\\/\\\\/g')

  # Replace c\ with c:\ for drive letter
  echo "$VALUE3" | sed "s/./$VALUE2:/1"
}

get_boot_url() {
  echo "$CHE_HOST:$CHE_PORT/api/system/state"
}

get_display_url() {
  # If the user has modified che.env with a custom CHE_HOST, we need to detect that here
  # and not use the in-memory one which is always set with eclipse/che-ip.
  local CHE_HOST_LOCAL=$${CHE_PRODUCT_NAME}_HOST

  if is_initialized; then 
    CHE_HOST_LOCAL=$(get_value_of_var_from_env_file ${CHE_PRODUCT_NAME}_HOST)
  fi

  if ! is_docker_for_mac && ! is_docker_for_windows; then
    echo "http://${CHE_HOST_LOCAL}:${CHE_PORT}"
  else
    echo "http://localhost:${CHE_PORT}"
  fi
}

get_debug_display_url() {
  local CHE_DEBUG_PORT_LOCAL=8000

  if is_initialized; then 
    DEBUG_PORT_FROM_CONFIG=$(get_value_of_var_from_env_file ${CHE_PRODUCT_NAME}_DEBUG_PORT)
    if [[ "${DEBUG_PORT_FROM_CONFIG}" != "" ]]; then
      CHE_DEBUG_PORT_LOCAL=$DEBUG_PORT_FROM_CONFIG
    fi
  fi

  if ! is_docker_for_mac && ! is_docker_for_windows; then
    echo "http://${CHE_HOST}:${CHE_DEBUG_PORT_LOCAL}"
  else
    echo "http://localhost:${CHE_DEBUG_PORT_LOCAL}"
  fi
}

server_is_booted() {
  PING_URL=$(get_boot_url)
  HTTP_STATUS_CODE=$(curl -I -k ${PING_URL} -s -o /dev/null --write-out '%{http_code}')
  log "${HTTP_STATUS_CODE}"
  if [[ "${HTTP_STATUS_CODE}" = "200" ]] || [[ "${HTTP_STATUS_CODE}" = "302" ]]; then
    return 0
  else
    return 1
  fi
}

has_env_variables() {
  PROPERTIES=$(env | grep "${CHE_PRODUCT_NAME}_")

  if [ "$PROPERTIES" = "" ]; then
    return 1
  else
    return 0
  fi
}


### check if all utilities images are loaded and update them if not found
load_utilities_images_if_not_done() {
  IFS=$'\n'
  for UTILITY_IMAGE_LINE in ${UTILITY_IMAGE_LIST}; do
    local UTILITY_IMAGE=$(echo ${UTILITY_IMAGE_LINE} | cut -d'=' -f2)
    update_image_if_not_found ${UTILITY_IMAGE}
  done

}

update_image_if_not_found() {
  local CHECKING_TEXT="${GREEN}INFO:${NC} (${CHE_MINI_PRODUCT_NAME} download): Checking for image '$1'..."
  CURRENT_IMAGE=$(docker images -q "$1")
  if [ "${CURRENT_IMAGE}" == "" ]; then
    text "${CHECKING_TEXT} not found\n"
    update_image $1
  else
    log "${CHECKING_TEXT} found"
  fi
}

# Usage:
#   confirm_operation <Warning message> [--force|--no-force]
confirm_operation() {
  FORCE_OPERATION=${2:-"--no-force"}

  if [ ! "${FORCE_OPERATION}" == "--quiet" ]; then
    # Warn user with passed message
    info "${1}"
    text "\n"
    read -p "      Are you sure? [N/y] " -n 1 -r
    text "\n\n"
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      return 1;
    else
      return 0;
    fi
  fi
}

port_open() {
  docker run -d -p $1:$1 --name fake ${BOOTSTRAP_IMAGE_ALPINE} httpd -f -p $1 -h /etc/ > /dev/null 2>&1
  NETSTAT_EXIT=$?
  docker rm -f fake > /dev/null 2>&1

  if [ $NETSTAT_EXIT = 125 ]; then
    return 1
  else
    return 0
  fi
}

server_is_booted_extra_check() {
 true
}

wait_until_server_is_booted() {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted ${2} || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    log "sleep 2"
    sleep 2
    server_is_booted_extra_check
    ELAPSED=$((ELAPSED+1))
  done
}

less_than_numerically() {
  COMPARE=$(awk "BEGIN { print ($1 < $2) ? 0 : 1}")
  return $COMPARE
}

# Input - an array of ports and port descriptions to check
# Output - true if all ports are open, false if any of them are already bound
check_all_ports(){

  declare -a PORT_INTERNAL_ARRAY=("${@}")

  DOCKER_PORT_STRING=""
  HTTPD_PORT_STRING=""
  for index in "${!PORT_INTERNAL_ARRAY[@]}"; do 
    PORT=${PORT_INTERNAL_ARRAY[$index]%;*}
    PORT_STRING=${PORT_INTERNAL_ARRAY[$index]#*;}

    DOCKER_PORT_STRING+=" -p $PORT:$PORT"
    HTTPD_PORT_STRING+=" -p $PORT"
  done

  EXECUTION_STRING="docker run --rm ${DOCKER_PORT_STRING} ${BOOTSTRAP_IMAGE_ALPINE} \
                         sh -c \"echo hi\" > /dev/null 2>&1"
  eval ${EXECUTION_STRING}
  NETSTAT_EXIT=$?

  if [[ $NETSTAT_EXIT = 125 ]]; then
    return 1
  else
    return 0
  fi
}

print_ports_as_ok() {
  declare -a PORT_INTERNAL_ARRAY=("${@}")  

  for index in "${!PORT_INTERNAL_ARRAY[@]}"; do 
    PORT_STRING=${PORT_INTERNAL_ARRAY[$index]#*;}
    text "         $PORT_STRING ${GREEN}[AVAILABLE]${NC}\n"
  done
}

find_and_print_ports_as_notok() {
  declare -a PORT_INTERNAL_ARRAY=("${@}")  

  for index in "${!PORT_INTERNAL_ARRAY[@]}"; do 
    PORT=${PORT_INTERNAL_ARRAY[$index]%;*}
    PORT_STRING=${PORT_INTERNAL_ARRAY[$index]#*;}
    text   "         ${PORT_STRING} $(port_open ${PORT} && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  done

  echo ""
  error "Ports required to run $CHE_MINI_PRODUCT_NAME are used by another program."
  return 2;
}

container_exist_by_name(){
  docker inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_container_id() {
  log "docker inspect -f '{{.Id}}' ${1}"
  docker inspect -f '{{.Id}}' ${1} 2>&1 || false
}

container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until container_is_running ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    log "sleep 1"
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

has_compose() {
  hash docker-compose 2>/dev/null && return 0 || return 1
}

docker_compose() {
#  debug $FUNCNAME

  if has_compose; then
    docker-compose "$@"
  else
    docker_run -v "${CHE_HOST_INSTANCE}":"${CHE_CONTAINER_INSTANCE}" \
                  $IMAGE_COMPOSE "$@"
  fi
}

start_test_server() {
  export AGENT_INTERNAL_PORT=80
  export AGENT_EXTERNAL_PORT=32768
  export AGENT_CONTAINER_NAME="fakeagent-$CHE_PORT"

  # Start mini httpd server to run simulated tests
  docker run -d -p $AGENT_EXTERNAL_PORT:$AGENT_INTERNAL_PORT --name $AGENT_CONTAINER_NAME \
             ${BOOTSTRAP_IMAGE_ALPINE} httpd -f -p $AGENT_INTERNAL_PORT -h /etc/ >> "${LOGS}"

  export AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' $AGENT_CONTAINER_NAME)
  export AGENT_EXTERNAL_IP=$CHE_HOST
}

stop_test_server() {
  # Remove httpd server
  docker rm -f $AGENT_CONTAINER_NAME >> "${LOGS}"  
}

test1() {
  HTTP_CODE=$(curl -I localhost:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o /dev/null --connect-timeout 5 \
                          --write-out '%{http_code}') || echo "28" >> "${LOGS}"
  
  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

test2() {
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o /dev/null --connect-timeout 5 \
                          --write-out '%{http_code}') || echo "28" >> "${LOGS}"

  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

test3() {
   HTTP_CODE=$(docker_run --entrypoint=curl \
                          $(eval "echo \${IMAGE_${CHE_PRODUCT_NAME}}") \
                            -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                            -s -o /dev/null \
                            --write-out '%{http_code}')

  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

test4() {
  HTTP_CODE=$(docker_run --entrypoint=curl \
                         $(eval "echo \${IMAGE_${CHE_PRODUCT_NAME}}") \
                           -I ${AGENT_INTERNAL_IP}:${AGENT_INTERNAL_PORT}/alpine-release \
                           -s -o /dev/null \
                           --write-out '%{http_code}')  

  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

check_http_code() {
  if [ "${1}" = "200" ]; then
    return 0
  else
    return 1
  fi
}

# return options for docker run used by end-user when calling cli
get_docker_run_terminal_options() {
  local DOCKER_RUN_OPTIONS=""
  # if TTY is there, need to use -ti
  if [[ ${TTY_ACTIVATED} == "true" ]]; then
    DOCKER_RUN_OPTIONS="-t"
  fi
  if [[ ${CHE_CLI_IS_INTERACTIVE} == "true" ]]; then
    DOCKER_RUN_OPTIONS+="i"
  fi
  echo ${DOCKER_RUN_OPTIONS}
}
