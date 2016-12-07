#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Contains docker scripts

has_docker() {
  hash docker 2>/dev/null && return 0 || return 1
}

has_compose() {
  hash docker-compose 2>/dev/null && return 0 || return 1
}

get_container_folder() {
  THIS_CONTAINER_ID=$(get_this_container_id)
  FOLDER=$(get_container_host_bind_folder "$1" $THIS_CONTAINER_ID)
  echo "${FOLDER:=not set}"
}


get_this_container_id() {
  hostname
}

get_container_host_bind_folder() {
  # BINDS in the format of var/run/docker.sock:/var/run/docker.sock <path>:${CHE_CONTAINER_ROOT}
  BINDS=$(docker inspect --format="{{.HostConfig.Binds}}" "${2}" | cut -d '[' -f 2 | cut -d ']' -f 1)

  # Remove /var/run/docker.sock:/var/run/docker.sock
  VALUE=${BINDS/\/var\/run\/docker\.sock\:\/var\/run\/docker\.sock/}

  # Remove leading and trailing spaces
  VALUE2=$(echo "${VALUE}" | xargs)

  MOUNT=""
  IFS=$' '
  for SINGLE_BIND in $VALUE2; do
    case $SINGLE_BIND in
      *$1)
        MOUNT="${MOUNT} ${SINGLE_BIND}"
        echo "${MOUNT}" | cut -f1 -d":" | xargs
      ;;
      *)
        # Super ugly - since we parse by space, if the next parameter is not a colon, then
        # we know that next parameter is second part of a directory with a space in it.
        if [[ ${SINGLE_BIND} != *":"* ]]; then
          MOUNT="${MOUNT} ${SINGLE_BIND}"
        else
          MOUNT=""
        fi
      ;;
    esac
  done
}


get_docker_install_type() {
  debug $FUNCNAME
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
  debug $FUNCNAME
  if [[ "${GLOBAL_HOST_IP}" = "10.0.75.2" ]]; then
    return 0
  else
    return 1
  fi
}

is_boot2docker() {
  debug $FUNCNAME
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  debug $FUNCNAME
  if uname -r | grep -q 'moby' && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  debug $FUNCNAME
  if uname -r | grep -q 'moby' && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
  debug $FUNCNAME
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}

docker_run() {
  debug $FUNCNAME
  # Setup options for connecting to docker host
  if [ -z "${DOCKER_HOST+x}" ]; then
      DOCKER_HOST="/var/run/docker.sock"
  fi

  if [ -S "$DOCKER_HOST" ]; then
    docker run --rm -v $DOCKER_HOST:$DOCKER_HOST \
                    -v $HOME:$HOME \
                    -w "$(pwd)" "$@"
  else
    docker run --rm -e DOCKER_HOST -e DOCKER_TLS_VERIFY -e DOCKER_CERT_PATH \
                    -v $HOME:$HOME \
                    -w "$(pwd)" "$@"
  fi
}

container_exist_by_name(){
  docker inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_container_id() {
  log "docker inspect -f '{{.Id}}' ${1}"
  docker inspect -f '{{.Id}}' ${1}
}

container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until container_is_running ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    log "sleep 1"
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}


check_docker() {
  if ! has_docker; then
    error "Docker not found. Get it at https://docs.docker.com/engine/installation/."
    return 1;
  fi

  # If DOCKER_HOST is not set, then it should bind mounted
  if [ -z "${DOCKER_HOST+x}" ]; then
    if ! docker ps > /dev/null 2>&1; then
      info "Welcome to ${CHE_FORMAL_PRODUCT_NAME}!"
      info ""
      info "$CHE_FORMAL_PRODUCT_NAME commands require additional parameters:"
      info "  Mounting 'docker.sock', which let's us access Docker"
      info ""
      info "Syntax:"
      info "  docker run -it --rm ${BOLD} -v /var/run/docker.sock:/var/run/docker.sock${NC}"
      info "                  $CHE_MINI_PRODUCT_NAME/cli $*"
      return 2;
    fi
  fi

  DOCKER_VERSION=($(docker version |  grep  "Version:" | sed 's/Version://'))

  MAJOR_VERSION_ID=$(echo ${DOCKER_VERSION[0]:0:1})
  MINOR_VERSION_ID=$(echo ${DOCKER_VERSION[0]:2:2})

  # Docker needs to be greater than or equal to 1.11
  if [[ ${MAJOR_VERSION_ID} -lt 1 ]] ||
     [[ ${MINOR_VERSION_ID} -lt 11 ]]; then
       error "Error - Docker engine 1.11+ required."
       return 2;
  fi

  # Detect version so that we can provide better error warnings
  DEFAULT_CHE_VERSION=$(cat "/version/latest.ver")
  CHE_IMAGE_FULLNAME=$(docker inspect --format='{{.Config.Image}}' $(get_this_container_id))
  CHE_IMAGE_NAME=$(echo "${CHE_IMAGE_FULLNAME}" | cut -d : -f1 -s)
  CHE_IMAGE_VERSION=$(echo "${CHE_IMAGE_FULLNAME}" | cut -d : -f2 -s)
  if [[ "${CHE_IMAGE_VERSION}" = "" ]] ||
     [[ "${CHE_IMAGE_VERSION}" = "latest" ]]; then
     warning "You are using CLI image version 'latest' which is set to '$DEFAULT_CHE_VERSION'."
    CHE_IMAGE_VERSION=$DEFAULT_CHE_VERSION
  else
    CHE_IMAGE_VERSION=$CHE_IMAGE_VERSION
  fi

  CHE_VERSION=$CHE_IMAGE_VERSION
}


check_mounts() {

  # Verify that we can write to the host file system from the container
  check_host_volume_mount

  DATA_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}")
  INSTANCE_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}/instance")
  BACKUP_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}/backup")
  REPO_MOUNT=$(get_container_folder ":/repo")
  CLI_MOUNT=$(get_container_folder ":/cli")
  SYNC_MOUNT=$(get_container_folder ":/sync")
  UNISON_PROFILE_MOUNT=$(get_container_folder ":/unison")

  if [[ "${DATA_MOUNT}" = "not set" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
    info ""
    info "We need some information before we can start ${CHE_FORMAL_PRODUCT_NAME}."
    info ""
    info "$CHE_FORMAL_PRODUCT_NAME commands require additional parameters:"
    info "  1: Mounting 'docker.sock', which let's us access Docker"
    info "  2: A local path where ${CHE_FORMAL_PRODUCT_NAME} will save user data"
    info ""
    info "Simplest syntax:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                         ${CHE_IMAGE_FULLNAME} $*"
    info ""
    info ""
    info "Or run with overrides for instance and/or backup:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                      -v <YOUR_INSTANCE_PATH>:${CHE_CONTAINER_ROOT}/instance"
    info "                      -v <YOUR_BACKUP_PATH>:${CHE_CONTAINER_ROOT}/backup"
    info "                         ${CHE_IMAGE_FULLNAME} $*"
    return 2;
  fi

  DEFAULT_CHE_CONFIG="${DATA_MOUNT}"
  DEFAULT_CHE_INSTANCE="${DATA_MOUNT}"/instance
  DEFAULT_CHE_BACKUP="${DATA_MOUNT}"/backup

  if [[ "${INSTANCE_MOUNT}" != "not set" ]]; then
    DEFAULT_CHE_INSTANCE="${INSTANCE_MOUNT}"
  fi

  if [[ "${BACKUP_MOUNT}" != "not set" ]]; then
    DEFAULT_CHE_BACKUP="${BACKUP_MOUNT}"
  fi

  #   Set offline to CONFIG_MOUNT
  CHE_HOST_CONFIG=${CHE_CONFIG:-${DEFAULT_CHE_CONFIG}}
  CHE_CONTAINER_CONFIG="${CHE_CONTAINER_ROOT}"

  CHE_HOST_INSTANCE=${CHE_INSTANCE:-${DEFAULT_CHE_INSTANCE}}
  CHE_CONTAINER_INSTANCE="${CHE_CONTAINER_ROOT}/instance"

  CHE_HOST_BACKUP=${CHE_BACKUP:-${DEFAULT_CHE_BACKUP}}
  CHE_CONTAINER_BACKUP="${CHE_CONTAINER_ROOT}/backup"

  ### DEV MODE VARIABLES
  CHE_DEVELOPMENT_MODE="off"
  if [[ "${REPO_MOUNT}" != "not set" ]]; then
    CHE_DEVELOPMENT_MODE="on"
    CHE_HOST_DEVELOPMENT_REPO="${REPO_MOUNT}"
    CHE_CONTAINER_DEVELOPMENT_REPO="/repo"

    CHE_ASSEMBLY="${CHE_HOST_INSTANCE}/dev"

    if [[ ! -d "${CHE_CONTAINER_DEVELOPMENT_REPO}"  ]] || [[ ! -d "${CHE_CONTAINER_DEVELOPMENT_REPO}/assembly" ]]; then
      info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
      info ""
      info "You volume mounted ':/repo', but we did not detect a valid ${CHE_FORMAL_PRODUCT_NAME} source repo."
      info ""
      info "Volume mounting ':/repo' activate dev mode, using assembly and CLI files from $CHE_FORMAL_PRODUCT_NAME repo."
      info ""
      info "Please check the path you mounted to verify that is a valid $CHE_FORMAL_PRODUCT_NAME git repository."
      info ""
      info "Simplest syntax::"
      info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
      info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
      info "                      -v <YOUR_${CHE_PRODUCT_NAME}_REPO>:/repo"
      info "                         ${CHE_IMAGE_FULLNAME} $*"
      info ""
      info ""
      info "Or run with overrides for instance, and backup (all required):"
      info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
      info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
      info "                      -v <YOUR_INSTANCE_PATH>:${CHE_CONTAINER_ROOT}/instance"
      info "                      -v <YOUR_BACKUP_PATH>:${CHE_CONTAINER_ROOT}/backup"
      info "                      -v <YOUR_${CHE_PRODUCT_NAME}_REPO>:/repo"
      info "                         ${CHE_IMAGE_FULLNAME} $*"
      return 2
    fi
    if [[ ! -d $(echo ${CHE_CONTAINER_DEVELOPMENT_REPO}/${CHE_ASSEMBLY_IN_REPO}) ]]; then
      info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
      info ""
      info "You volume mounted a valid $CHE_FORMAL_PRODUCT_NAME repo to ':/repo', but we could not find a ${CHE_FORMAL_PRODUCT_NAME} assembly."
      info "Have you built ${CHE_ASSEMBLY_IN_REPO_MODULE_NAME} with 'mvn clean install'?"
      return 2
    fi
  fi
}

docker_compose() {
  debug $FUNCNAME

  if has_compose; then
    docker-compose "$@"
  else
    docker_run -v "${CHE_HOST_INSTANCE}":"${CHE_CONTAINER_INSTANCE}" \
                  docker/compose:1.8.1 "$@"
  fi
}

