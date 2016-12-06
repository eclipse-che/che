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

start_che_server() {
  if che_container_exist_by_name ${CHE_SERVER_CONTAINER_NAME}; then
    error_exit "A container running ${CHE_PRODUCT_NAME} named \"${CHE_SERVER_CONTAINER_NAME}\" already exists.
             1. Use \"info\" to find it's URL.
             2. Use \"restart\" to stop it and start anew.
             3. Stop it with \"stop\".
             4. Remove it manually (docker rm -f ${CHE_SERVER_CONTAINER_NAME}) and try again. Or:
             5. Set CHE_SERVER_CONTAINER_NAME to a different value and try again."
  fi

  CURRENT_IMAGE=$(docker images -q "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}")

  if [ "${CURRENT_IMAGE}" != "" ]; then
    info "${CHE_PRODUCT_NAME}: Found image ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}"
  else
    update_che_server
  fi

  info "${CHE_PRODUCT_NAME}: Starting container..."
  docker_run_with_debug "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}" > /dev/null
  CURRENT_CHE_SERVER_CONTAINER_ID=$(get_che_server_container_id ${CHE_SERVER_CONTAINER_NAME})
  wait_until_container_is_running 10 ${CURRENT_CHE_SERVER_CONTAINER_ID}
  if ! che_container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    error_exit "${CHE_PRODUCT_NAME}: Timeout waiting for ${CHE_PRODUCT_NAME} container to start."
  fi

  info "${CHE_PRODUCT_NAME}: Server logs at \"docker logs -f ${CHE_SERVER_CONTAINER_NAME}\""
  info "${CHE_PRODUCT_NAME}: Server booting..."
  wait_until_server_is_booted 60 ${CURRENT_CHE_SERVER_CONTAINER_ID}

  if server_is_booted ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    info "${CHE_PRODUCT_NAME}: Booted and reachable"
    info "${CHE_PRODUCT_NAME}: Ver: $(get_server_version ${CURRENT_CHE_SERVER_CONTAINER_ID})"
    if ! is_docker_for_mac; then
      info "${CHE_PRODUCT_NAME}: Use: http://${CHE_HOST_IP}:${CHE_PORT}"
      info "${CHE_PRODUCT_NAME}: API: http://${CHE_HOST_IP}:${CHE_PORT}/swagger"
    else
      info "${CHE_PRODUCT_NAME}: Use: http://localhost:${CHE_PORT}"
      info "${CHE_PRODUCT_NAME}: API: http://localhost:${CHE_PORT}/swagger"
    fi

    if has_debug; then
      info "${CHE_PRODUCT_NAME}: JPDA Debug - http://${CHE_HOST_IP}:${CHE_DEBUG_SERVER_PORT}"
    fi
  else
    error_exit "${CHE_PRODUCT_NAME}: Timeout waiting for server. Run \"docker logs ${CHE_SERVER_CONTAINER_NAME}\" to inspect the issue."
  fi
}

stop_che_server() {
  if [ $# -gt 0 ]; then
    CURRENT_CHE_SERVER_CONTAINER_ID=$1
  else
    CURRENT_CHE_SERVER_CONTAINER_ID=$(get_che_server_container_id ${CHE_SERVER_CONTAINER_NAME})
  fi

  if ! che_container_is_running $CURRENT_CHE_SERVER_CONTAINER_ID; then
    info "${CHE_PRODUCT_NAME}: Container $CURRENT_CHE_SERVER_CONTAINER_ID is not running. Nothing to do."
  else
    info "${CHE_PRODUCT_NAME}: Stopping server..."
    docker stop -t 300 ${CURRENT_CHE_SERVER_CONTAINER_ID} > /dev/null
    if che_container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
      error_exit "${CHE_PRODUCT_NAME}: Timeout waiting for the ${CHE_PRODUCT_NAME} container to stop."
    fi

    info "${CHE_PRODUCT_NAME}: Removing container"
    docker rm ${CURRENT_CHE_SERVER_CONTAINER_ID} > /dev/null
    info "${CHE_PRODUCT_NAME}: Stopped"
  fi
}

restart_che_server() {
  if [ $# -gt 0 ]; then
    CURRENT_CHE_SERVER_CONTAINER_ID=$1
  else
    CURRENT_CHE_SERVER_CONTAINER_ID=$(get_che_server_container_id ${CHE_SERVER_CONTAINER_NAME})
  fi

  if che_container_is_running $CURRENT_CHE_SERVER_CONTAINER_ID; then
    stop_che_server $CURRENT_CHE_SERVER_CONTAINER_ID
  fi

  start_che_server
}

update_che_server() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  info "${CHE_PRODUCT_NAME}: Pulling image ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}"
  execute_command_with_progress extended docker pull ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}
}

print_debug_info() {
  info "---------------------------------------"
  info "---------   LAUNCHER INFO  ------------"
  info "---------------------------------------"
  info ""
  info "---------  PLATFORM INFO  -------------"
  info "DOCKER_INSTALL_TYPE       = ${DOCKER_INSTALL_TYPE}"
  info "DOCKER_HOST_OS            = $(get_docker_host_os)"
  info "DOCKER_HOST_IP            = ${DEFAULT_DOCKER_HOST_IP}"
  info "DOCKER_HOST_EXTERNAL_IP   = ${DEFAULT_CHE_DOCKER_MACHINE_HOST_EXTERNAL:-not set}"
  info "DOCKER_DAEMON_VERSION     = $(get_docker_daemon_version)"
  info ""
  info ""
  info "--------- CHE INSTANCE LIST  ----------"
  CURRENT_CHE_INSTANCES=$(docker ps -aq --filter "ancestor=${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}")
  IFS=$'\n'
  for CHE_SERVER_CONTAINER_ID in $CURRENT_CHE_INSTANCES; do
    info ""
    info "--------- CHE INSTANCE: $CHE_SERVER_CONTAINER_ID" 
    CURRENT_CHE_SERVER_CONTAINER_NAME=$(docker inspect --format='{{.Name}}' ${CHE_SERVER_CONTAINER_ID} | cut -f2 -d"/") 
    info "CHE SERVER CONTAINER NANE = $CURRENT_CHE_SERVER_CONTAINER_NAME"
    info "CHE CONTAINER EXISTS      = $(che_container_exist $CHE_SERVER_CONTAINER_ID && echo "YES" || echo "NO")"
    info "CHE CONTAINER STATUS      = $(che_container_is_running $CHE_SERVER_CONTAINER_ID && echo "running" || echo "stopped")"
    if che_container_is_running $CHE_SERVER_CONTAINER_ID; then
      info "CHE SERVER STATUS         = $(server_is_booted $CHE_SERVER_CONTAINER_ID && echo "running & api reachable" || echo "stopped")"
      info "CHE VERSION               = $(get_server_version $CHE_SERVER_CONTAINER_ID)"
      info "CHE IMAGE                 = $(get_che_container_image_name $CHE_SERVER_CONTAINER_ID)"
      info "CHE CONF                  = $(get_che_container_conf_folder $CHE_SERVER_CONTAINER_ID)"
      info "CHE DATA                  = $(get_che_container_data_folder $CHE_SERVER_CONTAINER_ID)"
      CURRENT_CHE_HOST_IP=$(get_che_container_host_ip_from_container $CHE_SERVER_CONTAINER_ID)
      CURRENT_CHE_PORT=$(docker inspect --format='{{ (index (index .NetworkSettings.Ports "8080/tcp") 0).HostPort }}' ${CHE_SERVER_CONTAINER_ID})
      info "CHE USE URL               = http://${CURRENT_CHE_HOST_IP}:${CURRENT_CHE_PORT}"  
      info "CHE API URL               = http://${CURRENT_CHE_HOST_IP}:${CURRENT_CHE_PORT}/swagger"
      if has_container_debug $CHE_SERVER_CONTAINER_ID; then
        CURRENT_CHE_DEBUG_PORT=$(docker inspect --format='{{ (index (index .NetworkSettings.Ports "8000/tcp") 0).HostPort }}' ${CHE_SERVER_CONTAINER_ID})        
        info "CHE JPDA DEBUG URL        = http://${CURRENT_CHE_HOST_IP}:${CURRENT_CHE_DEBUG_PORT}"  
      fi
      info 'CHE LOGS                  = run `docker logs -f '${CURRENT_CHE_SERVER_CONTAINER_NAME}'`'
    fi
  done
  info ""
  info ""
  info "----  CURRENT COMMAND LINE OPTIONS  ---" 
  info "CHE_VERSION               = ${CHE_VERSION}"
  info "CHE_DATA                  = ${CHE_DATA}"
  info "CHE_CONF                  = ${CHE_CONF:-not set}"
  info "CHE_ASSEMBLY              = ${CHE_ASSEMBLY:-not set}"
  info "CHE_PORT                  = ${CHE_PORT}"
  info "CHE_HOST_IP               = ${CHE_HOST_IP}"
  info "CHE_RESTART_POLICY        = ${CHE_RESTART_POLICY}"
  info "CHE_USER                  = ${CHE_USER}"
  info "CHE_LOG_LEVEL             = ${CHE_LOG_LEVEL}"
  info "CHE_DEBUG_SERVER          = ${CHE_DEBUG_SERVER}"
  info "CHE_DEBUG_SERVER_PORT     = ${CHE_DEBUG_SERVER_PORT}"
  info "CHE_HOSTNAME              = ${CHE_HOSTNAME}"
  info "CHE_SERVER_CONTAINER_NAME = ${CHE_SERVER_CONTAINER_NAME}"
  info "CHE_SERVER_IMAGE_NAME     = ${CHE_SERVER_IMAGE_NAME}"
}