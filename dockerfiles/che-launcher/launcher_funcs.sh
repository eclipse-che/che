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

usage () {
  printf "%s" "${USAGE}"
}

info() {
  printf  "${GREEN}INFO:${NC} %s\n" "${1}"
}

debug() {
  printf  "${BLUE}DEBUG:${NC} %s\n" "${1}"
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
}

error_exit() {
  echo  "---------------------------------------"
  error "!!!"
  error "!!! ${1}"
  error "!!!"
  echo  "---------------------------------------"
  container_self_destruction
  exit 1
}

get_clean_path() {
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_che_launcher_container_id() {
  hostname
}

get_che_launcher_version() {
  LAUNCHER_CONTAINER_ID=$(get_che_launcher_container_id)
  LAUNCHER_IMAGE_NAME=$(docker inspect --format='{{.Config.Image}}' "${LAUNCHER_CONTAINER_ID}")
  echo "${LAUNCHER_IMAGE_NAME}" | cut -d : -f2
}

is_boot2docker() {
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_ip() {
  DOCKER_HOST_IP=$(get_docker_host_ip)
  if [ "${DOCKER_HOST_IP}" = "10.0.75.2" ]; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  if uname -r | grep -q 'moby' && ! has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  if uname -r | grep -q 'moby' && has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

get_docker_install_type() {
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

get_docker_host_ip() {
  NETWORK_IF="eth0"
  if is_boot2docker; then
    NETWORK_IF="eth1"
  fi

  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
}

get_che_hostname() {
  INSTALL_TYPE=$(get_docker_install_type)
  if [ "${INSTALL_TYPE}" = "boot2docker" ] ||
     [ "${INSTALL_TYPE}" = "docker4windows" ]; then
    get_docker_host_ip
  else
    echo "localhost"
  fi
}

check_docker() {
  if [ ! -S /var/run/docker.sock ]; then
    error_exit "Docker socket (/var/run/docker.sock) hasn't be mounted \
inside the container. Verify the syntax of the \"docker run\" command."
  fi

  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    error_exit "Error when running \"docker ps\": ${output}"
  fi
}

che_container_exist() {
  if [ "$(docker ps -aq  -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_stopped() {
  if [ "$(docker ps -qa -f "status=exited" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_running || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  HTTP_STATUS_CODE=$(curl -I http://"${CHE_HOST_IP}":"${CHE_PORT}"/api/  \
                     -s -o /dev/null --write-out "%{http_code}")
  if [ "${HTTP_STATUS_CODE}" = "200" ]; then
    return 0
  else
    return 1
  fi
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

execute_command_with_progress() {
  progress=$1
  command=$2
  shift 2

  pid=""
  printf "\n"

  case "$progress" in
    extended)
      $command "$@"
      ;;
    basic|*)
      $command "$@" &>/dev/null &
      pid=$!
      while kill -0 "$pid" >/dev/null 2>&1; do
        printf "#"
        sleep 10
      done
      wait $pid # return pid's exit code
      printf "\n"
    ;;
  esac
  printf "\n"
}

container_self_destruction() {
  docker rm -f "$(get_che_launcher_container_id)" > /dev/null 2>&1
}
