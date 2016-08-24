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
  if che_container_exist; then
    error_exit "A container named \"${CHE_SERVER_CONTAINER_NAME}\" already exists. Please remove it manually (docker rm -f ${CHE_SERVER_CONTAINER_NAME}) and try again."
  fi

  CURRENT_IMAGE=$(docker images -q "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}")

  if [ "${CURRENT_IMAGE}" != "" ]; then
    info "ECLIPSE CHE: ALREADY HAVE IMAGE ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}"
  else
    update_che_server
  fi

  ENV_FILE=$(get_list_of_che_system_environment_variables)

  info "ECLIPSE CHE: CONTAINER STARTING"
  docker run -d --name "${CHE_SERVER_CONTAINER_NAME}" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v /home/user/che/lib:/home/user/che/lib-copy \
    ${CHE_LOCAL_BINARY_ARGS} \
    -p "${CHE_PORT}":8080 \
    --restart="${CHE_RESTART_POLICY}" \
    --user="${CHE_USER}" \
    ${CHE_CONF_ARGS} \
    ${CHE_STORAGE_ARGS} \
    --env-file=$ENV_FILE \
    "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}" \
                --remote:"${CHE_HOST_IP}" \
                -s:uid \
                -s:client \
                ${CHE_DEBUG_OPTION} \
                run > /dev/null

  rm $ENV_FILE
  
  wait_until_container_is_running 10
  if ! che_container_is_running; then
    error_exit "ECLIPSE CHE: Timeout waiting Che container to start."
  fi

  info "ECLIPSE CHE: SERVER LOGS AT \"docker logs -f ${CHE_SERVER_CONTAINER_NAME}\""
  info "ECLIPSE CHE: SERVER BOOTING..."
  wait_until_server_is_booted 60

  if server_is_booted; then
    info "ECLIPSE CHE: BOOTED AND REACHABLE"
    info "ECLIPSE CHE: http://${CHE_HOSTNAME}:${CHE_PORT}"
  else
    error_exit "ECLIPSE CHE: Timeout waiting Che server to boot. Run \"docker logs ${CHE_SERVER_CONTAINER_NAME}\" to see the logs."
  fi
}

stop_che_server() {
  if ! che_container_is_running; then
    info "-------------------------------------------------------"
    info "ECLIPSE CHE: CONTAINER IS NOT RUNNING. NOTHING TO DO."
    info "-------------------------------------------------------"
  else
    info "ECLIPSE CHE: STOPPING SERVER..."
    docker exec ${CHE_SERVER_CONTAINER_NAME} /home/user/che/bin/che.sh -c -s:uid stop > /dev/null
    wait_until_container_is_stopped 60
    if che_container_is_running; then
      error_exit "ECLIPSE CHE: Timeout waiting Che container to stop."
    fi

    info "ECLIPSE CHE: REMOVING CONTAINER"
    docker rm ${CHE_SERVER_CONTAINER_NAME} > /dev/null
    info "ECLIPSE CHE: STOPPED"
  fi
}

restart_che_server() {
  if che_container_is_running; then
    stop_che_server
  fi
  start_che_server
}

update_che_server() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  info "ECLIPSE CHE: PULLING IMAGE ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}"
  execute_command_with_progress extended docker pull ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}
}

print_debug_info() {
  debug "---------------------------------------"
  debug "---------  CHE DEBUG INFO  ------------"
  debug "---------------------------------------"
  debug ""
  debug "---------  PLATFORM INFO  -------------"
  debug "DOCKER_INSTALL_TYPE       = ${DOCKER_INSTALL_TYPE}"
  debug "DOCKER_HOST_OS            = $(get_docker_host_os)"
  debug "DOCKER_HOST_IP            = ${DEFAULT_DOCKER_HOST_IP}"
  debug "DOCKER_DAEMON_VERSION     = $(get_docker_daemon_version)"
  debug ""
  debug ""
  debug "--------- CHE INSTANCE INFO  ----------" 
  debug "CHE CONTAINER EXISTS      = $(che_container_exist && echo "YES" || echo "NO")"
  debug "CHE CONTAINER STATUS      = $(che_container_is_running && echo "running" || echo "stopped")"
  if che_container_is_running; then
    debug "CHE SERVER STATUS         = $(server_is_booted && echo "running" || echo "stopped")"
    debug "CHE IMAGE                 = $(get_che_container_image_name)"
    debug "CHE SERVER CONTAINER ID   = $(get_che_server_container_id)"
    debug "CHE CONF FOLDER           = $(get_che_container_conf_folder)"
    debug "CHE DATA FOLDER           = $(get_che_container_data_folder)"
    CURRENT_CHE_PORT=$(docker inspect --format='{{ (index (index .NetworkSettings.Ports "8080/tcp") 0).HostPort }}' ${CHE_SERVER_CONTAINER_NAME})
    debug "CHE DASHBOARD URL         = http://${DEFAULT_CHE_HOSTNAME}:${CURRENT_CHE_PORT}"  
    debug "CHE API URL               = http://${DEFAULT_CHE_HOSTNAME}:${CURRENT_CHE_PORT}/api"
    debug 'CHE LOGS                  = run `docker logs -f '${CHE_SERVER_CONTAINER_NAME}'`'
  fi
  debug ""
  debug ""
  debug "----  CURRENT COMMAND LINE OPTIONS  ---" 
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
  debug "CHE_SERVER_CONTAINER_NAME = ${CHE_SERVER_CONTAINER_NAME}"
  debug "CHE_SERVER_IMAGE_NAME     = ${CHE_SERVER_IMAGE_NAME}"
  debug ""
  debug "---------------------------------------"
  debug "---------------------------------------"
  debug "---------------------------------------"
}
