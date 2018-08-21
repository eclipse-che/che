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

help_cmd_config() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} config [PARAMETERS]\n"
  text "\n"
  text "Generate a ${CHE_MINI_PRODUCT_NAME} runtime configuration into /instance. The configurator uses 
values from your host, ${CHE_MINI_PRODUCT_NAME}.env, and optionally your local repository to generate
a runtime configuration used to start and stop ${CHE_MINI_PRODUCT_NAME}. A configuration is generated 
before every execution of ${CHE_MINI_PRODUCT_NAME}. The configuration phase will download all Docker
images required to start or stop ${CHE_MINI_PRODUCT_NAME} to guarantee that the right images are cached
before execution. If you have mounted a local repository or assembly, the ${CHE_MINI_PRODUCT_NAME} Docker
images will use those binaries instead of their embedded ones.\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --force                           Uses 'docker rmi' and 'docker pull' to forcibly retrieve latest images\n"
  text "  --no-force                        Updates images if matching tag not found in local cache\n"
  text "  --pull                            Uses 'docker pull' to check for new remote versions of images\n"
  text "  --skip:config                     Skip re-generation of config files placed into /instance\n"
  text "\n"
}

pre_cmd_config() {
  CHE_SKIP_CONFIG=false
  FORCE_UPDATE="--no-force"

  while [ $# -gt 0 ]; do
    case $1 in
      --skip:config)
        CHE_SKIP_CONFIG=true
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
      *) error "Unknown parameter: $1" return 2 ;;
    esac
  done
}

post_cmd_config() {
  :
}

cmd_config() {
  # If the system is not initialized, initalize it.
  # If the system is already initialized, but a user wants to update images, then re-download.
  if ! is_initialized; then
    cmd_lifecycle init $FORCE_UPDATE
  elif [[ "${FORCE_UPDATE}" == "--pull" ]] || \
       [[ "${FORCE_UPDATE}" == "--force" ]]; then
    cmd_lifecycle download $FORCE_UPDATE
  elif is_nightly && ! is_fast && ! skip_pull; then
    cmd_lifecycle download --pull
  fi

  # If using a local repository, then we need to always perform an updated init with those files
  if local_repo; then
    # if user has mounted local repo, use configuration files from the repo.
    # please note that in production mode update of configuration sources must be only on update.
    docker_run -v "${CHE_HOST_CONFIG}":/copy \
               -v "${CHE_HOST_DEVELOPMENT_REPO}"/dockerfiles/init:/files \
                  $IMAGE_INIT

  fi

  # Run the docker configurator
  if ! skip_config; then
    generate_configuration_with_puppet
  fi

  # Replace certain environment file lines with their container counterparts
  info "config" "Customizing docker-compose for running in a container"
 
  if local_repo || local_assembly; then
    # in development mode to avoid permissions issues we copy tomcat assembly to ${CHE_INSTANCE}
    # if ${CHE_FORMAL_PRODUCT_NAME} development tomcat exist we remove it
    if [[ -d "${CHE_CONTAINER_INSTANCE}/dev" ]]; then
        log "docker_run -v \"${CHE_HOST_INSTANCE}/dev\":/root/dev ${BOOTSTRAP_IMAGE_ALPINE} sh -c \"rm -rf /root/dev/*\""

        # Super weird bug - sometimes, the RM command doesn't wipe everything, so we have to repeat it a couple times
        until config_directory_is_empty; do
          docker_run -v "${CHE_HOST_INSTANCE}/dev":/root/dev ${BOOTSTRAP_IMAGE_ALPINE} sh -c "rm -rf /root/dev/${CHE_MINI_PRODUCT_NAME}-tomcat" > /dev/null 2>&1  || true
        done

        log "rm -rf \"${CHE_HOST_INSTANCE}/dev\" >> \"${LOGS}\""
        rm -rf "${CHE_CONTAINER_INSTANCE}/dev"
    fi

    if [[ ! -d $(echo ${CHE_CONTAINER_ASSEMBLY_FULL_PATH}) ]]; then
      warning "You mounted ':/repo' or ':/assembly', but we did not find an assembly."
      warning "Have you built the assembly with 'mvn clean install'?"
      warning "CHE_ASSEMBLY=${CHE_CONTAINER_ASSEMBLY_FULL_PATH}"
      return 2
    fi

    # copy ${CHE_FORMAL_PRODUCT_NAME} development tomcat to ${CHE_INSTANCE} folder
    info "config" "Copying local binaries to ${CHE_HOST_INSTANCE}/dev..."
    mkdir -p "${CHE_CONTAINER_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat"
    cp -r "$(echo ${CHE_CONTAINER_ASSEMBLY_FULL_PATH})/." \
        "${CHE_CONTAINER_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat/"
  fi
}


# Runs puppet image to generate che configuration
generate_configuration_with_puppet() {
  info "config" "Generating $CHE_MINI_PRODUCT_NAME configuration..."

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

  CHE_REPO="off"
  WRITE_PARAMETERS=""

  if local_repo || local_assembly; then
    CHE_REPO="on"
    WRITE_PARAMETERS=" -e \"CHE_ASSEMBLY=${CHE_ASSEMBLY}\""
  fi

  if local_repo; then
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
      if [ -d "/repo/dockerfiles/init/addon/modules" ]; then
        WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/addon/modules/\":/etc/puppet/addon/:ro"
      fi
    fi
  fi

  for element in "${CLI_ENV_ARRAY[@]}"
  do
    var1=$(echo $element | cut -f1 -d=)
    var2=$(echo $element | cut -f2 -d=)

    if [[ $var1 == CHE_* ]] ||
       [[ $var1 == IMAGE_* ]]  ||
       [[ $var1 == *_IMAGE_* ]]  ||
       [[ $var1 == ${CHE_PRODUCT_NAME}_* ]]; then
      WRITE_PARAMETERS+=" -e $var1='$var2'"
    fi
  done

  GENERATE_CONFIG_COMMAND="docker_run \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  ${WRITE_PARAMETERS} \
                  -e \"CHE_ENV_FILE=${CHE_ENV_FILE}\" \
                  -e \"CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT}\" \
                  -e \"CHE_CONTAINER_NAME=${CHE_CONTAINER_NAME}\" \
                  -e \"CHE_ENVIRONMENT=${CHE_ENVIRONMENT}\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_USER=${CHE_USER}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_REPO=${CHE_REPO}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/:/etc/puppet/addon/ \
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
