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
  fi

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  # Development mode
  if [ "${CHE_DEVELOPMENT_MODE}" = "on" ]; then
    # if dev mode is on, pick configuration sources from repo.
    # please note that in production mode update of configuration sources must be only on update.
    docker_run -v "${CHE_HOST_CONFIG}":/copy \
               -v "${CHE_HOST_DEVELOPMENT_REPO}"/dockerfiles/init:/files \
                  $IMAGE_INIT

    # in development mode to avoid permissions issues we copy tomcat assembly to ${CHE_INSTANCE}
    # if ${CHE_FORMAL_PRODUCT_NAME} development tomcat exist we remove it
    if [[ -d "${CHE_CONTAINER_INSTANCE}/dev" ]]; then
        log "docker_run -v \"${CHE_HOST_INSTANCE}/dev\":/root/dev alpine:3.4 sh -c \"rm -rf /root/dev/*\""
        docker_run -v "${CHE_HOST_INSTANCE}/dev":/root/dev alpine:3.4 sh -c "rm -rf /root/dev/*"
        log "rm -rf \"${CHE_HOST_INSTANCE}/dev\" >> \"${LOGS}\""
        rm -rf "${CHE_CONTAINER_INSTANCE}/dev"
    fi
    # copy ${CHE_FORMAL_PRODUCT_NAME} development tomcat to ${CHE_INSTANCE} folder
    cp -r "$(echo $CHE_CONTAINER_DEVELOPMENT_REPO/$CHE_ASSEMBLY_IN_REPO)" \
        "${CHE_CONTAINER_INSTANCE}/dev"
  fi

  info "config" "Generating $CHE_MINI_PRODUCT_NAME configuration..."
  # Run the docker configurator
  generate_configuration_with_puppet

  # Replace certain environment file lines with their container counterparts
  info "config" "Customizing docker-compose for running in a container"

  # Write the installed version to the *.ver file into the instance folder
  echo "$CHE_VERSION" > "${CHE_CONTAINER_INSTANCE}/${CHE_VERSION_FILE}"

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

  if [ "${CHE_DEVELOPMENT_MODE}" = "on" ]; then
    # Note - bug in docker requires relative path for env, not absolute
    GENERATE_CONFIG_COMMAND="docker_run \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/manifests\":/etc/puppet/manifests:ro \
                  -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/modules\":/etc/puppet/modules:ro \
                  -e \"CHE_ENV_FILE=${CHE_ENV_FILE}\" \
                  -e \"CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT}\" \
                  -e \"CHE_ENVIRONMENT=development\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_DEVELOPMENT_REPO=${CHE_HOST_DEVELOPMENT_REPO}\" \
                  -e \"CHE_ASSEMBLY=${CHE_ASSEMBLY}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.pp --show_diff"
  else
    GENERATE_CONFIG_COMMAND="docker_run \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  -e \"CHE_ENV_FILE=${CHE_ENV_FILE}\" \
                  -e \"CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT}\" \
                  -e \"CHE_ENVIRONMENT=production\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.pp --show_diff >> \"${LOGS}\""
  fi

  log ${GENERATE_CONFIG_COMMAND}
  eval ${GENERATE_CONFIG_COMMAND}
}
