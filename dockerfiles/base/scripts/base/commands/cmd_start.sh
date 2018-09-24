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
#   Tyler Jewell - Initial Implementation
#

help_cmd_start() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} start [PARAMETERS]\n"
  text "\n"
  text "Starts ${CHE_MINI_PRODUCT_NAME} and verifies its operation\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --follow                          Displays server logs to console and blocks until user interrupts\n"
  text "  --force                           Uses 'docker rmi' and 'docker pull' to forcibly retrieve latest images\n"
  text "  --no-force                        Updates images if matching tag not found in local cache\n"
  text "  --pull                            Uses 'docker pull' to check for new remote versions of images\n"
  text "  --skip:config                     Skip re-generation of config files placed into /instance\n"
  text "  --skip:preflight                  Skip preflight checks\n"
  text "  --skip:postflight                 Skip postflight checks\n"
  text "\n"  
}

pre_cmd_start() {
  CHE_SKIP_CONFIG=false
  CHE_SKIP_PREFLIGHT=false
  CHE_SKIP_POSTFLIGHT=false
  CHE_FOLLOW_LOGS=false
  FORCE_UPDATE="--no-force"

  while [ $# -gt 0 ]; do
    case $1 in
      --skip:config)
        CHE_SKIP_CONFIG=true
        shift ;;
      --skip:preflight)
        CHE_SKIP_PREFLIGHT=true
        shift ;;
      --skip:postflight)
        CHE_SKIP_POSTFLIGHT=true
        shift ;;
      --follow)
        CHE_FOLLOW_LOGS=true
        shift ;;
      --force)
        FORCE_UPDATE="--force"
        shift ;;
      --no-force)
        FORCE_UPDATE="--no-force"
        shift ;;
      --pull)
        FORCE_UPDATE="--pull"
        shift ;;
      *)
        shift ;;
    esac
  done
}

post_cmd_start() {
  :
}


cmd_start() {
  # If already running, just display output again
  check_if_booted

  if server_is_booted $(get_server_container_id $CHE_CONTAINER_NAME); then 
    return 1
  fi

  # Always regenerate puppet configuration from environment variable source, whether changed or not.
  # If the current directory is not configured with an .env file, it will initialize
  if skip_config; then
    cmd_lifecycle config $FORCE_UPDATE --skip:config
  else
    cmd_lifecycle config $FORCE_UPDATE
  fi

  # Preflight checks
  #   a) Check for open ports
  #   b) Test simulated connections for failures
  if ! is_fast && ! skip_preflight; then
    info "start" "Preflight checks"
    cmd_start_check_preflight
    text "\n"
  fi

  # Start ${CHE_FORMAL_PRODUCT_NAME}
  # Note bug in docker requires relative path, not absolute path to compose file
  info "start" "Starting containers..."
  COMPOSE_UP_COMMAND="docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=\"${CHE_COMPOSE_PROJECT_NAME}\" up -d"

  ## validate the compose file (quiet mode)
  if local_repo; then
    docker_compose --file=${REFERENCE_CONTAINER_COMPOSE_FILE} config -q || (error "Invalid docker compose file content at ${REFERENCE_CONTAINER_COMPOSE_FILE}" && return 2)
  fi

  if ! debug_server; then
    COMPOSE_UP_COMMAND+=" >> \"${LOGS}\" 2>&1"
  fi

  log ${COMPOSE_UP_COMMAND}
  eval ${COMPOSE_UP_COMMAND} || (error "Error during 'compose up' - printing 30 line tail of ${CHE_HOST_CONFIG}/cli.log:" && tail -30 ${LOGS} && return 2)

  wait_until_booted

  if ! server_is_booted $(get_server_container_id $CHE_CONTAINER_NAME); then 
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CHE_CONTAINER_NAME}\" to inspect."
    return 2
  fi

  if ! is_fast && ! skip_postflight; then
    cmd_start_check_postflight
  fi

  check_if_booted
}

cmd_start_check_host_resources() {
  HOST_RAM=$(docker info | grep "Total Memory:")
  HOST_RAM=$(echo ${HOST_RAM#*:} | xargs)
  HOST_RAM=${HOST_RAM% *}

  PREFLIGHT=""
  if less_than_numerically $CHE_MIN_RAM $HOST_RAM; then
    text "         mem ($CHE_MIN_RAM GiB):           ${GREEN}[OK]${NC}\n"
  else
    text "         mem ($CHE_MIN_RAM GiB):           ${RED}[NOT OK]${NC}\n"
    PREFLIGHT="fail"
  fi

  HOST_DISK=$(df "${CHE_CONTAINER_ROOT}" | grep "${CHE_CONTAINER_ROOT}" | awk '{ print $4}')

  if less_than_numerically "$CHE_MIN_DISK"000 $HOST_DISK; then
    text "         disk ($CHE_MIN_DISK MB):           ${GREEN}[OK]${NC}\n"
  else
    text "         disk ($CHE_MIN_DISK MB):           ${RED}[NOT OK]${NC}\n"
    PREFLIGHT="fail"
  fi

  if [[ "${PREFLIGHT}" = "fail" ]]; then
    text "\n"
    error "${CHE_MINI_PRODUCT_NAME} requires more RAM or disk to guarantee workspaces can start."
    return 2;
  fi
}

cmd_start_check_ports() {
  # Develop array of port #, description.
  # Format of array is "<port>;<port_string>" where the <port_string> is the text to appear in console
  local PORT_ARRAY=(
     "${CHE_PORT};port ${CHE_PORT} (http):       "
    )

  # If dev mode is on, then we also need to check the debug port set by the user for availability
  if debug_server; then
    USER_DEBUG_PORT=$(get_value_of_var_from_env_file ${CHE_PRODUCT_NAME}_DEBUG_PORT)

    if [[ "$USER_DEBUG_PORT" = "" ]]; then
      # If the user has not set a debug port, then use the default
      CHE_LOCAL_DEBUG_PORT=8000
    else 
      # Otherwise, this is the value set by the user
      CHE_LOCAL_DEBUG_PORT=$USER_DEBUG_PORT
    fi

    PORT_ARRAY+=("$CHE_LOCAL_DEBUG_PORT;port ${CHE_LOCAL_DEBUG_PORT} (debug):      ")
  fi

  if check_all_ports "${PORT_ARRAY[@]}"; then
    print_ports_as_ok "${PORT_ARRAY[@]}"
  else
    find_and_print_ports_as_notok "${PORT_ARRAY[@]}"
  fi
}

# See cmd_network.sh for utilities for unning these tests
cmd_start_check_agent_network() {
  start_test_server

  PREFLIGHT="success"
  if test1 || test2; then
    text "         conn (browser => ws):    ${GREEN}[OK]${NC}\n"
  else
    text "         conn (browser => ws):    ${RED}[NOT OK]${NC}\n"
    PREFLIGHT="fail"
  fi

  if test3 && test4; then
    text "         conn (server => ws):     ${GREEN}[OK]${NC}\n"
  else
    text "         conn (server => ws):     ${RED}[NOT OK]${NC}\n\n"
    PREFLIGHT="fail"
  fi

  stop_test_server

  if [[ "${PREFLIGHT}" = "fail" ]]; then
    text "\n"
    error "Try 'docker run <options> ${CHE_IMAGE_FULLNAME} info --network' for more tests."
    return 2;
  fi
}

cmd_start_check_preflight() {
  cmd_start_check_host_resources
  cmd_start_check_ports
  cmd_start_check_agent_network
}

cmd_start_check_postflight() {
  true
}

wait_until_booted() {
  CURRENT_CHE_SERVER_CONTAINER_ID=$(get_server_container_id $CHE_CONTAINER_NAME)

  wait_until_container_is_running 20 ${CURRENT_CHE_SERVER_CONTAINER_ID}
  if ! container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for ${CHE_MINI_PRODUCT_NAME} container to start."
    return 2
  fi

  info "start" "Services booting..."

  # CHE-3546 - if in development mode, then display the che server logs to STDOUT
  #            automatically kill the streaming of the log output when the server is booted
  if debug_server || follow_logs; then
    DOCKER_LOGS_COMMAND="docker logs -f ${CHE_CONTAINER_NAME}"

    if debug_server; then 
      DOCKER_LOGS_COMMAND+=" &"
    fi

    eval $DOCKER_LOGS_COMMAND
    LOG_PID=$!
  else
    info "start" "Server logs at \"docker logs -f ${CHE_CONTAINER_NAME}\""
  fi

  check_containers_are_running
  wait_until_server_is_booted 60 ${CURRENT_CHE_SERVER_CONTAINER_ID}
  check_containers_are_running

  if debug_server; then
    kill $LOG_PID > /dev/null 2>&1
    info ""
  fi
}

check_if_booted() {
  if container_exist_by_name $CHE_CONTAINER_NAME; then
    local CURRENT_CHE_SERVER_CONTAINER_ID=$(get_server_container_id $CHE_CONTAINER_NAME)
    if server_is_booted $CURRENT_CHE_SERVER_CONTAINER_ID; then 
      DISPLAY_URL=$(get_display_url)
      info "start" "Booted and reachable"
      info "start" "Ver: $(get_installed_version)"
      info "start" "Use: ${DISPLAY_URL}"
      info "start" "API: ${DISPLAY_URL}/swagger"
      if debug_server; then
        DISPLAY_DEBUG_URL=$(get_debug_display_url)
        info "start" "Debug: ${DISPLAY_DEBUG_URL}"
      fi
    fi
  fi
}

check_containers_are_running() {
  # get list of docker compose services started by this docker compose
  local LIST_OF_COMPOSE_CONTAINERS=$(docker_compose --file=${REFERENCE_CONTAINER_COMPOSE_FILE} -p=$CHE_COMPOSE_PROJECT_NAME config --services)

  # For each service of docker-compose file, get container and then check it is running
  while IFS= read -r DOCKER_COMPOSE_SERVICE_NAME ; do
    local CONTAINER_ID_MATCHING_SERVICE_NAMES=$(docker ps -q --filter label=com.docker.compose.service=${DOCKER_COMPOSE_SERVICE_NAME})
    if [[ -z "${CONTAINER_ID_MATCHING_SERVICE_NAMES}" ]]; then
      error "Unable to find a matching container for the docker compose service named ${DOCKER_COMPOSE_SERVICE_NAME}. Check logs at ${CHE_HOST_CONFIG}/cli.log"
      return 2
    fi

    while IFS='\n' read -r CONTAINER_ID_MATCHING_SERVICE_NAME ; do
      debug "Container with id ${CONTAINER_ID_MATCHING_SERVICE_NAME} is matching ${DOCKER_COMPOSE_SERVICE_NAME} service"
      local IS_RUNNING_CONTAINER=$(docker inspect -f {{.State.Running}} ${CONTAINER_ID_MATCHING_SERVICE_NAME})
      debug "Running state of container ${CONTAINER_ID_MATCHING_SERVICE_NAME} is ${IS_RUNNING_CONTAINER}"

      if [[ ${IS_RUNNING_CONTAINER} != "true" ]]; then
        error "The container with ID ${CONTAINER_ID_MATCHING_SERVICE_NAME} of docker-compose service ${DOCKER_COMPOSE_SERVICE_NAME} is not running, aborting."
        docker inspect ${CONTAINER_ID_MATCHING_SERVICE_NAME}
        return 2
      fi
    done <<< "${CONTAINER_ID_MATCHING_SERVICE_NAMES}"
  done <<< "${LIST_OF_COMPOSE_CONTAINERS}"
}

skip_preflight() {
  if [ "${CHE_SKIP_PREFLIGHT}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

skip_postflight() {
  if [ "${CHE_SKIP_POSTFLIGHT}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

follow_logs() {
  if [ "${CHE_FOLLOW_LOGS}" = "true" ]; then
    return 0
  else
    return 1
  fi
}
