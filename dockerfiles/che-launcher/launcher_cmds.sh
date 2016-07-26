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
    "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}" \
                --remote:"${CHE_HOST_IP}" \
                -s:uid \
                -s:client \
                ${CHE_DEBUG_OPTION} \
                run > /dev/null

  wait_until_container_is_running 10
  if ! che_container_is_running; then
    error_exit "ECLIPSE CHE: Timeout waiting Che container to start."
  fi

  info "ECLIPSE CHE: SERVER LOGS AT \"docker logs -f ${CHE_SERVER_CONTAINER_NAME}\""
  info "ECLIPSE CHE: SERVER BOOTING..."
  wait_until_server_is_booted 20

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
  info "ECLIPSE CHE: IMAGE ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION} INSTALLED"
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
