#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

init_offline_or_network_mode(){
  # If you are using ${CHE_FORMAL_PRODUCT_NAME} in offline mode, images must be loaded here
  # This is the point where we know that docker is working, but before we run any utilities
  # that require docker.
  if is_offline; then
    info "init" "Importing ${CHE_MINI_PRODUCT_NAME} Docker images from tars..."

    if [ ! -d ${CHE_CONTAINER_OFFLINE_FOLDER} ]; then
      warning "Skipping offline image loading - '${CHE_CONTAINER_OFFLINE_FOLDER}' not found"
    else
      IFS=$'\n'
      for file in "${CHE_CONTAINER_OFFLINE_FOLDER}"/*.tar
      do
        if ! $(docker load < "${CHE_CONTAINER_OFFLINE_FOLDER}"/"${file##*/}" > /dev/null); then
          error "Failed to restore ${CHE_MINI_PRODUCT_NAME} Docker images"
          return 2;
        fi
        info "init" "Loading ${file##*/}..."
      done
    fi
  else
    # If we are here, then we want to run in networking mode.
    # If we are in networking mode, we have had some issues where users have failed DNS networking.
    # See: https://github.com/eclipse/che/issues/3266#issuecomment-265464165
    if ! is_fast && ! skip_network; then
      # Removing this info line as it was appearing before initial CLI output

      local HTTP_STATUS_CODE=$(curl -I -k https://hub.docker.com -s -o /dev/null --write-out '%{http_code}')
      if [[ ! $HTTP_STATUS_CODE -eq "301" ]] && [[ ! $HTTP_STATUS_CODE -eq "200" ]]; then
        info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
        info ""
        info "We could not resolve DockerHub using DNS."
        info "Either we cannot reach the Internet or Docker's DNS resolver needs a modification."
        info ""
        info "You can:"
        info "  1. Modify Docker's DNS settings." 
        info "     a. Docker for Windows & Mac have GUIs for this."
        info "     b. Typically setting DNS to 8.8.8.8 fixes resolver issues."
        info "  2. Does your network require Docker to use a proxy?"
        info "     a. Docker for Windows & Mac have GUIs to set proxies."
        info "  3. Verify that you have access to DockerHub."
        info "     a. Try 'curl --head hub.docker.com'"
        info "  4. Skip networking checks."
        info "     a. Add '--fast' to any command"
        return 2;
      fi
    fi
  fi
}

init_initial_images() {
  # get list of images
  get_image_manifest ${CHE_VERSION}

  # grab all bootstrap images
  IFS=$'\n'
  for BOOTSTRAP_IMAGE_LINE in ${BOOTSTRAP_IMAGE_LIST}; do
    local BOOTSTRAP_IMAGE=$(echo ${BOOTSTRAP_IMAGE_LINE} | cut -d'=' -f2)
    if [ "$(docker images -q ${BOOTSTRAP_IMAGE} 2> /dev/null)" = "" ]; then
        info "cli" "Pulling image ${BOOTSTRAP_IMAGE}"
        log "docker pull ${BOOTSTRAP_IMAGE} >> \"${LOGS}\" 2>&1"
        TEST=""
        docker pull ${BOOTSTRAP_IMAGE} >> "${LOGS}" > /dev/null 2>&1 || TEST=$?
        if [ "$TEST" = "1" ]; then
          error "Image ${BOOTSTRAP_IMAGE} unavailable. Not on dockerhub or built locally."
          return 2;
        fi
      fi
  done

}

### Returns the list of ${CHE_FORMAL_PRODUCT_NAME} images for a particular version of ${CHE_FORMAL_PRODUCT_NAME}
### Sets the images as environment variables after loading from file
get_image_manifest() {
  log "Checking registry for version '$1' images"
  if ! has_version_registry $1; then
    version_error $1
    return 1;
  fi

  # Load images from file
  BOOTSTRAP_IMAGE_LIST=$(cat /version/$1/images-bootstrap)
  IMAGE_LIST=$(cat /version/$1/images)
  if [ -z "${CHE_SINGLE_PORT:-}" ]; then
    IMAGE_LIST=$(echo "${IMAGE_LIST}" | sed '/IMAGE_TRAEFIK/d')
  fi
  if [ -z "${CHE_MULTIUSER:-}" ]; then
     IMAGE_LIST=$(echo "${IMAGE_LIST}" | sed '/IMAGE_KEY*/d; /IMAGE_POSTGRES/d')
  fi
  UTILITY_IMAGE_LIST=$(cat /version/$1/images-utilities)

  # set variables
  set_variables_images_list "${BOOTSTRAP_IMAGE_LIST}"
  set_variables_images_list "${IMAGE_LIST}"
  set_variables_images_list "${UTILITY_IMAGE_LIST}"
}

has_version_registry() {
  if [ -d /version/$1 ]; then
    return 0;
  else
    return 1;
  fi
}

version_error(){
  text "\nWe could not find version '$1'. Available versions:\n"
  list_versions
  text "\nSet CHE_VERSION=<version> and rerun.\n\n"
}

list_versions(){
  # List all subdirectories and then print only the file name
  for version in /version/* ; do
    text " ${version##*/}\n"
  done
}

### define variables for all image name in the given list
set_variables_images_list() {
  IFS=$'\n'
  REPLACEMENT='s/\(.*\)=\(.*\)/\1=${\1:-\2}/g'
  for SINGLE_IMAGE in $1; do
    INSTRUCTION="$(echo "${SINGLE_IMAGE}" | sed -e "${REPLACEMENT}")"
    log "eval ${INSTRUCTION}"
    eval "${INSTRUCTION}"
  done

}

get_docker_install_type() {
#  debug $FUNCNAME
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}

has_docker_for_windows_client(){
#  debug $FUNCNAME
  if [[ "${GLOBAL_HOST_IP}" = "10.0.75.2" ]]; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
#  debug $FUNCNAME
  if $(echo ${UNAME_R} | grep -q 'linuxkit') && has_docker_for_windows_client; then
    return 0
  elif $(echo ${UNAME_R} | grep -q 'moby') && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
#  debug $FUNCNAME
  if $(echo ${UNAME_R} | grep -q 'linuxkit') && ! has_docker_for_windows_client; then
    return 0
  elif $(echo ${UNAME_R} | grep -q 'moby') && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
#  debug $FUNCNAME
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}


docker_run() {
#  debug $FUNCNAME
  # Setup options for connecting to docker host
  if [ -z "${DOCKER_HOST+x}" ]; then
      DOCKER_HOST="/var/run/docker.sock"
  fi

  echo "" > /tmp/docker_run_vars
  # Add environment variables for CHE
  while IFS='=' read -r -d '' key value; do
    if [[ ${key} == "CHE_"* ]]; then
      echo ${key}=${value} >> /tmp/docker_run_vars
    fi
  done < <(env -0)

  # Add scripts global variables for CHE
  while read key; do
    if [[ ${key} == "CHE_"* ]]; then
      local ENV_VAL="${!key}"
      echo ${key}=${ENV_VAL} >> /tmp/docker_run_vars
    fi
  done < <(compgen -v)


  if [ -S "$DOCKER_HOST" ]; then
    docker run --rm --env-file /tmp/docker_run_vars \
                    -v $DOCKER_HOST:$DOCKER_HOST \
                    -v $HOME:$HOME \
                    -w "$(pwd)" "$@"
  else
    docker run --rm --env-file /tmp/docker_run_vars \
                    -e DOCKER_HOST -e DOCKER_TLS_VERIFY -e DOCKER_CERT_PATH \
                    -v $HOME:$HOME \
                    -w "$(pwd)" "$@"
  fi
}

has_curl() {
  hash curl 2>/dev/null && return 0 || return 1
}

curl() {

 # In situations where we are performing internal pings using curl, then
 # we should append the CHE_HOST as a no proxy. It seems that curl does
 # not respect the NO_PROXY environment variable set on the system.
 local NO_PROXY_CONFIG_FOR_CURL=("")
 if [[ ! "${HTTP_PROXY}" = "" ]] ||
    [[ ! "${HTTPS_PROXY}" = "" ]]; then
      if is_var_defined "${CHE_PRODUCT_NAME}_HOST"; then
        NO_PROXY_CONFIG_FOR_CURL=("--noproxy" $(eval "echo \$${CHE_PRODUCT_NAME}_HOST"))
      fi
 fi

 if ! has_curl; then
   log "docker run --rm --net=host appropriate/curl \"$@\""
   docker run --rm --net=host appropriate/curl ${NO_PROXY_CONFIG_FOR_CURL[@]} "$@"
 else
   log "$(which curl) ${NO_PROXY_CONFIG_FOR_CURL[@]} \"$@\""
   $(which curl) ${NO_PROXY_CONFIG_FOR_CURL[@]} "$@"
 fi
}

is_var_defined() {
    if [ $# -ne 1 ]
    then
        echo "Expected exactly one argument: variable name as string, e.g., 'my_var'"
        exit 1
    fi
    eval "[ ! -z \${$1:-} ]"
    return $?
}
