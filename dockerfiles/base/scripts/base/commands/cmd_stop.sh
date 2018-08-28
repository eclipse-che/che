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

help_cmd_stop() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} restart [PARAMETERS]\n"
  text "\n"
  text "Stops ${CHE_MINI_PRODUCT_NAME} workspaces gracefully and then the server\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --skip:graceful                   Do not wait for confirmation that workspaces have stopped\n"
  text "  --user <username>                 Admin user name for authenticated Che systems\n"
  text "  --password <password>             Admin password for authenticated Che systems\n"
  text "\n"
}

pre_cmd_stop() {
  :
}

post_cmd_stop() {
  :
}

cmd_stop() {
  debug $FUNCNAME
  FORCE_STOP=false
  if [[ "$@" == *"--skip:graceful"* ]]; then
  	FORCE_STOP=true
  elif local_repo || local_assembly; then
    FORCE_STOP=true
  fi

  if server_is_booted $(get_server_container_id $CHE_CONTAINER_NAME); then 
    if [[ ${FORCE_STOP} = "false" ]]; then
      info "stop" "Stopping workspaces..."
      local GRACEFUL_STATUS_RESULT=0
      cmd_lifecycle action "graceful-stop" "$@" >> "${LOGS}" 2>&1 || GRACEFUL_STATUS_RESULT=$?
      # error on authentication (401 modulo 256 = 145)
      if [[ ${GRACEFUL_STATUS_RESULT} -eq 145 ]]; then
        error "Authentication failed (hint: --user/--password for auth, --skip:graceful bypasses workspace stop)"
        return 2;
      elif [[ ${GRACEFUL_STATUS_RESULT} -ne 0 ]]; then
        error "Error during graceful stop - see $CHE_HOST_CONFIG/cli.log. (hint: --skip:graceful bypasses workspace stop)"
        return 2;
      fi
    fi
    # stop containers booted by docker compose
    stop_containers
  else
    info "stop" "Server $CHE_CONTAINER_NAME on port $CHE_PORT not running..."
  fi
}

# stop containers booted by docker compose and remove them
stop_containers() {
  info "stop" "Stopping containers..."
  if is_initialized; then
    log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_COMPOSE_PROJECT_NAME stop -t ${CHE_COMPOSE_STOP_TIMEOUT} >> \"${LOGS}\" 2>&1 || true"
    docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                   -p=$CHE_COMPOSE_PROJECT_NAME stop -t ${CHE_COMPOSE_STOP_TIMEOUT} >> "${LOGS}" 2>&1 || true
    info "stop" "Removing containers..."
    log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_COMPOSE_PROJECT_NAME rm -v --force >> \"${LOGS}\" 2>&1 || true"
    docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                   -p=$CHE_COMPOSE_PROJECT_NAME rm -v --force >> "${LOGS}" 2>&1 || true
  fi
}
