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

cmd_debug() {
   
  DOCKER_CONNECT="${DOCKER_MOUNT}"
  if [[ "${DOCKER_MOUNT}" = "not set" ]]; then
    DOCKER_CONNECT=$(docker inspect $(get_this_container_id) | grep DOCKER_HOST)
    DOCKER_CONNECT=${DOCKER_CONNECT//\"}
    DOCKER_CONNECT=${DOCKER_CONNECT//\,}
    DOCKER_CONNECT=${DOCKER_CONNECT#*=}
  fi
  text "\n"
  text "CLI:\n"
  text " TTY: ${TTY_ACTIVATED}\n"
  text " Daemon: ${DOCKER_CONNECT}\n"
  text " Image: ${CHE_IMAGE_FULLNAME}\n"
  text " Version: ${CHE_IMAGE_VERSION}\n"
  text " Command: $(echo $COMMAND | cut -f2 -d'_')\n"
  text " Parameters: ${ORIGINAL_PARAMETERS}\n"
  text "Mounts:\n"
  text " $CHE_CONTAINER_ROOT: $DATA_MOUNT\n"
  text " $CHE_CONTAINER_ROOT/instance: $INSTANCE_MOUNT\n"
  text " $CHE_CONTAINER_ROOT/backup: $BACKUP_MOUNT\n"
  text " /repo: $REPO_MOUNT\n"
  text " /assembly: $ASSEMBLY_MOUNT\n"
  text " /sync: $SYNC_MOUNT\n"
  text " /unison: $UNISON_PROFILE_MOUNT\n"
  text " /chedir: $CHEDIR_MOUNT\n"
  text "System:\n"
  text " Docker: $(get_docker_install_type)\n"
  if [[ ${HTTP_PROXY} = "" ]] &&
     [[ ${HTTPS_PROXY} = "" ]] &&
     [[ ${NO_PROXY} = "" ]]; then
     text " Proxy: not set\n"
  else
    text " Proxy: HTTP_PROXY=${HTTP_PROXY}, HTTPS_PROXY=${HTTPS_PROXY}, NO_PROXY=${NO_PROXY}\n"
  fi
  if is_initialized; then
    text " Initialized: true\n"
  else
    text " Initialized: false\n"
  fi    
  text "Internal:\n"
  text " ${CHE_PRODUCT_NAME}_VERSION: ${CHE_VERSION}\n"
  local CHE_HOST_LOCAL=${CHE_HOST}
  if is_initialized; then
    CHE_HOST_LOCAL=$(get_value_of_var_from_env_file CHE_HOST)
  fi
  text " ${CHE_PRODUCT_NAME}_HOST: ${CHE_HOST_LOCAL}\n"
  text " ${CHE_PRODUCT_NAME}_INSTANCE: ${CHE_HOST_INSTANCE}\n"
  text " ${CHE_PRODUCT_NAME}_CONFIG: ${CHE_HOST_CONFIG}\n"
  text " ${CHE_PRODUCT_NAME}_BACKUP: ${CHE_HOST_BACKUP}\n"
  text " ${CHE_PRODUCT_NAME}_REGISTRY: ${CHE_MANIFEST_DIR}\n"
  text " ${CHE_PRODUCT_NAME}_DEBUG: ${CHE_DEBUG}\n"
  if local_repo; then
    text " ${CHE_PRODUCT_NAME}_DEVELOPMENT_REPO: ${CHE_HOST_DEVELOPMENT_REPO}\n"
  fi
  
  text "Image Registry:\n"
  for SINGLE_IMAGE in $IMAGE_LIST; do
    text " $SINGLE_IMAGE\n"
  done
  for SINGLE_IMAGE in $BOOTSTRAP_IMAGE_LIST; do
    text " $SINGLE_IMAGE\n"
  done
  for SINGLE_IMAGE in $UTILITY_IMAGE_LIST; do
    text " $SINGLE_IMAGE\n"
  done

  # This seems like overkill for the output
  #readarray STACK_IMAGE_LIST < /version/$CHE_VERSION/images-stacks
  #for SINGLE_IMAGE in "${STACK_IMAGE_LIST[@]}"; do
    #text " $SINGLE_IMAGE"
  #done

  if ! is_initialized; then
    text "Configuration: not initialized\n"
  else
    text "Configuration:\n"
    # Implement loop of all lines that are uncommented.
    CONFIGURATION_ARRAY=($(grep "^[^#]" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"))
    for SINGLE_CONFIGURATION in "${CONFIGURATION_ARRAY[@]}"; do
      text " $SINGLE_CONFIGURATION\n"
    done
  fi
}
