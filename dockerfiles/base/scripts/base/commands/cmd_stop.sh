#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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
  text "\n"
}

pre_cmd_stop() {
  true
}

cmd_stop() {
  debug $FUNCNAME
  FORCE_STOP=false
  if [[ "$@" == *"--skip:graceful"* ]]; then
  	FORCE_STOP=true
  fi

  if server_is_booted $(get_server_container_id $CHE_CONTAINER_NAME); then 
    if [[ ${FORCE_STOP} = "false" ]]; then
      info "stop" "Stopping workspaces..."
      if ! $(cmd_lifecycle action "graceful-stop" "$@" >> "${LOGS}" 2>&1 || false); then
        error "We encountered an error -- see cli.log"
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
    log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_COMPOSE_PROJECT_NAME rm >> \"${LOGS}\" 2>&1 || true"
    docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                   -p=$CHE_COMPOSE_PROJECT_NAME rm --force >> "${LOGS}" 2>&1 || true
  fi
}
