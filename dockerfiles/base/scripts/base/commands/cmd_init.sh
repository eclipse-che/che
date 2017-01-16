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

cmd_init() {

  # set an initial value for the flag
  if is_nightly && ! is_fast; then 
    FORCE_UPDATE="--pull"
  else
    FORCE_UPDATE="--no-force"
  fi

  AUTO_ACCEPT_LICENSE="false"
  REINIT="false"

  while [ $# -gt 0 ]; do
    case $1 in
      --no-force|--force|--pull|--offline)
        FORCE_UPDATE=$1
        shift ;;
      --accept-license)
        AUTO_ACCEPT_LICENSE="true"
        shift ;;
      --reinit)
        REINIT="true"
        shift ;;
      *) error "Unknown parameter: $1" ; return 2 ;;
    esac
  done

  if [ "${FORCE_UPDATE}" == "--no-force" ]; then
    # If ${CHE_FORMAL_PRODUCT_NAME}.environment file exists, then fail
    if is_initialized; then
      if [[ "${REINIT}" = "false" ]]; then
        info "init" "Already initialized."
        return 2
      fi
    fi
  fi

  if [[ "${CHE_IMAGE_VERSION}" = "nightly" ]]; then
    warning "($CHE_MINI_PRODUCT_NAME init): 'nightly' installations cannot be upgraded to non-nightly versions"
  fi

  cmd_download $FORCE_UPDATE

  if [ -z ${IMAGE_INIT+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  if require_license; then
    if [[ "${AUTO_ACCEPT_LICENSE}" = "false" ]]; then
      info ""
      info "init" "Do you accept the ${CHE_FORMAL_PRODUCT_NAME} license? (${CHE_LICENSE_URL})"
      text "\n"
      read -p "      I accept the license: [Y/n] " -r || { error "Shell is not in interactive mode. Add -i flag to the docker run command"; return 2; }
      if [[ $REPLY =~ ^[Nn]$ ]]; then
        return 2;
      fi
      text "\n"
    fi
  fi

  info "init" "Installing configuration and bootstrap variables:"
  log "mkdir -p \"${CHE_CONTAINER_CONFIG}\""
  mkdir -p "${CHE_CONTAINER_CONFIG}"
  log "mkdir -p \"${CHE_CONTAINER_INSTANCE}\""
  mkdir -p "${CHE_CONTAINER_INSTANCE}"

  if [ ! -w "${CHE_CONTAINER_CONFIG}" ]; then
    error "CHE_CONTAINER_CONFIG is not writable. Aborting."
    return 1;
  fi

  if [ ! -w "${CHE_CONTAINER_INSTANCE}" ]; then
    error "CHE_CONTAINER_INSTANCE is not writable. Aborting."
    return 1;
  fi

  # in development mode we use init files from repo otherwise we use it from docker image
  INIT_RUN_PARAMETERS=""
  if local_repo; then
    if [ -d "/repo/dockerfiles/init/manifests" ]; then
      INIT_RUN_PARAMETERS=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init\":/files"
      INIT_RUN_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/manifests/${CHE_MINI_PRODUCT_NAME}.env\":/etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.env"
    fi
  fi
  GENERATE_INIT_COMMAND="docker_run -v ${CHE_HOST_CONFIG}:/copy ${INIT_RUN_PARAMETERS} $IMAGE_INIT"
  log $GENERATE_INIT_COMMAND
  eval $GENERATE_INIT_COMMAND

  # If this is is a reinit, we should not overwrite these core template files.
  # If this is an initial init, then we have to override some values
  if [[ "${REINIT}" = "false" ]]; then
    # Otherwise, we are using the templated version and making some modifications.
    cmd_init_reinit_pre_action
    rm -rf "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}".bak > /dev/null 2>&1

    info "init" "  ${CHE_PRODUCT_NAME}_HOST=${CHE_HOST}"
    info "init" "  ${CHE_PRODUCT_NAME}_VERSION=${CHE_VERSION}"
    info "init" "  ${CHE_PRODUCT_NAME}_CONFIG=${CHE_HOST_CONFIG}"
    info "init" "  ${CHE_PRODUCT_NAME}_INSTANCE=${CHE_HOST_INSTANCE}"
    if local_repo; then
      info "init" "  ${CHE_PRODUCT_NAME}_DEVELOPMENT_REPO=${CHE_HOST_DEVELOPMENT_REPO}"
      info "init" "  ${CHE_PRODUCT_NAME}_ASSEMBLY=${CHE_ASSEMBLY}"
    fi
  fi

  # Encode the version that we initialized into the version file
  echo "$CHE_VERSION" > "${CHE_CONTAINER_INSTANCE}/${CHE_VERSION_FILE}"
}

cmd_init_reinit_pre_action() {
  
  # One time only, set the value of CHE_HOST within the environment file.
  sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HOST=.*|${CHE_PRODUCT_NAME}_HOST=${CHE_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"

  if [[ ! ${HTTP_PROXY} = "" ]]; then
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HTTP_PROXY=.*|${CHE_PRODUCT_NAME}_HTTP_PROXY=${HTTP_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_WORKSPACE_HTTP__PROXY=.*|${CHE_PRODUCT_NAME}_WORKSPACE_HTTP__PROXY=${HTTP_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  fi
  if [[ ! ${HTTPS_PROXY} = "" ]]; then
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HTTPS_PROXY=.*|${CHE_PRODUCT_NAME}_HTTPS_PROXY=${HTTPS_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_WORKSPACE_HTTPS__PROXY=.*|${CHE_PRODUCT_NAME}_WORKSPACE_HTTPS__PROXY=${HTTPS_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  fi
  if [[ ! ${HTTP_PROXY} = "" ]] ||
     [[ ! ${HTTPS_PROXY} = "" ]]; then
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_NO_PROXY=.*|${CHE_PRODUCT_NAME}_NO_PROXY=${NO_PROXY},${CHE_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_WORKSPACE_NO__PROXY=.*|${CHE_PRODUCT_NAME}_WORKSPACE_NO__PROXY=che-host,${NO_PROXY},${CHE_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  fi
}

require_license() {
  if [[ "${CHE_LICENSE}" = "true" ]]; then
    return 0
  else
    return 1
  fi
}
