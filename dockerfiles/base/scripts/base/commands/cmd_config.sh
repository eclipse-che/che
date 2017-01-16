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

cmd_config_post_action() {
 true
}

cmd_config() {

  # If the system is not initialized, initalize it.
  # If the system is already initialized, but a user wants to update images, then re-download.
  FORCE_UPDATE=${1:-"--no-force"}
  if ! is_initialized; then
    cmd_init $FORCE_UPDATE
  elif [[ "${FORCE_UPDATE}" == "--pull" ]] || \
       [[ "${FORCE_UPDATE}" == "--force" ]]; then
    cmd_download $FORCE_UPDATE
  elif is_nightly && ! is_fast; then
    cmd_download --pull
  fi

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  # If using a local repository, then we need to always perform an updated init with those files
  if local_repo; then
    # if user has mounted local repo, use configuration files from the repo.
    # please note that in production mode update of configuration sources must be only on update.
    docker_run -v "${CHE_HOST_CONFIG}":/copy \
               -v "${CHE_HOST_DEVELOPMENT_REPO}"/dockerfiles/init:/files \
                  $IMAGE_INIT

  fi

  info "config" "Generating $CHE_MINI_PRODUCT_NAME configuration..."

  # Run the docker configurator
  generate_configuration_with_puppet

  # Replace certain environment file lines with their container counterparts
  info "config" "Customizing docker-compose for running in a container"

 
  if local_repo; then
    # in development mode to avoid permissions issues we copy tomcat assembly to ${CHE_INSTANCE}
    # if ${CHE_FORMAL_PRODUCT_NAME} development tomcat exist we remove it
    if [[ -d "${CHE_CONTAINER_INSTANCE}/dev" ]]; then
        log "docker_run -v \"${CHE_HOST_INSTANCE}/dev\":/root/dev ${UTILITY_IMAGE_ALPINE} sh -c \"rm -rf /root/dev/*\""

        # Super weird bug - sometimes, the RM command doesn't wipe everything, so we have to repeat it a couple times
        until config_directory_is_empty; do
          docker_run -v "${CHE_HOST_INSTANCE}/dev":/root/dev ${UTILITY_IMAGE_ALPINE} sh -c "rm -rf /root/dev/${CHE_MINI_PRODUCT_NAME}-tomcat" > /dev/null 2>&1  || true
        done

        log "rm -rf \"${CHE_HOST_INSTANCE}/dev\" >> \"${LOGS}\""
        rm -rf "${CHE_CONTAINER_INSTANCE}/dev"
    fi

    if [[ ! -d $(echo ${CHE_CONTAINER_DEVELOPMENT_REPO}/${CHE_ASSEMBLY_IN_REPO}) ]]; then
      warning "You volume mounted a valid $CHE_FORMAL_PRODUCT_NAME repo to ':/repo', but we could not find a ${CHE_FORMAL_PRODUCT_NAME} assembly."
      warning "Have you built ${CHE_ASSEMBLY_IN_REPO_MODULE_NAME} with 'mvn clean install'?"
      return 2
    fi

    # copy ${CHE_FORMAL_PRODUCT_NAME} development tomcat to ${CHE_INSTANCE} folder
    info "config" "Copying local binaries to ${CHE_HOST_INSTANCE}/dev..."
    mkdir -p "${CHE_CONTAINER_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat"
    cp -r "$(echo $CHE_CONTAINER_DEVELOPMENT_REPO/$CHE_ASSEMBLY_IN_REPO)/." \
        "${CHE_CONTAINER_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat/"
  fi

  cmd_config_post_action
}


# Runs puppet image to generate che configuration
generate_configuration_with_puppet() {
  debug $FUNCNAME

  if is_docker_for_windows; then
    CHE_ENV_FILE=$(convert_posix_to_windows "${CHE_HOST_INSTANCE}/config/$CHE_MINI_PRODUCT_NAME.env")
  else
    CHE_ENV_FILE="${CHE_HOST_INSTANCE}/config/$CHE_MINI_PRODUCT_NAME.env"
  fi

  if debug_server; then
    CHE_ENVIRONMENT="development"
    WRITE_LOGS=""
  else
    CHE_ENVIRONMENT="production"
    WRITE_LOGS=">> \"${LOGS}\""
  fi

  if local_repo; then
    CHE_REPO="on"
    WRITE_PARAMETERS=" -e \"CHE_ASSEMBLY=${CHE_ASSEMBLY}\""
    # add local mounts only if they are present
    if [ -d "/repo/dockerfiles/init/manifests" ]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/manifests\":/etc/puppet/manifests:ro"
    fi
    if [ -d "/repo/dockerfiles/init/modules" ]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/modules\":/etc/puppet/modules:ro"
    fi
    # Handle override/addon
    if [ -d "/repo/dockerfiles/init/addon" ]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/addon/addon.pp\":/etc/puppet/manifests/addon.pp:ro"
    fi

  else
    CHE_REPO="off"
    WRITE_PARAMETERS=""
  fi

  GENERATE_CONFIG_COMMAND="docker_run \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  ${WRITE_PARAMETERS} \
                  -e \"CHE_ENV_FILE=${CHE_ENV_FILE}\" \
                  -e \"CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT}\" \
                  -e \"CHE_ENVIRONMENT=${CHE_ENVIRONMENT}\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_REPO=${CHE_REPO}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/ --show_diff ${WRITE_LOGS}"

  log ${GENERATE_CONFIG_COMMAND}
  eval ${GENERATE_CONFIG_COMMAND}
}

config_directory_is_empty() {
  if [[ -d "${CHE_CONTAINER_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat" ]]; then
    return 1
  else
    return 0
  fi
}
