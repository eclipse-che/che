#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {

  CHE_LAUNCHER_CONTAINER_NAME="che-launcher"
  CHE_LAUNCHER_IMAGE_NAME="che-launcher"

  # User configurable variables
  DEFAULT_CHE_VERSION="latest"
  DEFAULT_CHE_CLI_ACTION="help"

  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}

  USAGE="
Usage:
  che [COMMAND]
     start                              Starts Che server
     stop                               Stops Che server
     restart                            Restart Che server
     update                             Pull latest version of ${CHE_LAUNCHER_IMAGE_NAME}
     info                               Print some debugging information

Docs: http://eclipse.org/che/getting-started.
"
}

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
  exit 1
}

check_docker() {
  if ! docker ps > /dev/null 2>&1; then
    output=$(docker)
    error_exit "Error - Docker not installed properly: ${output}"
  fi
}

parse_command_line () {
  for command_line_option in "$@"; do
    case ${command_line_option} in
      start|stop|restart|update|info|-h|--help)
        CHE_CLI_ACTION=${command_line_option}
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  done
}

execute_che_launcher() {

  update_che_launcher

  info "ECLIPSE CHE: LAUNCHING LAUNCHER"
  docker run -it --rm --name "${CHE_LAUNCHER_CONTAINER_NAME}" \
    -v //var//run//docker.sock://var//run//docker.sock \
    "${CHE_LAUNCHER_IMAGE_NAME}":"${CHE_VERSION}" "${CHE_CLI_ACTION}" \
    # > /dev/null 2>&1
}

execute_command_with_progress() {
  local progress=$1
  local command=$2
  shift 2

  local pid=""
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

update_che_launcher() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  CURRENT_IMAGE=$(docker images -q ${CHE_LAUNCHER_IMAGE_NAME}:${CHE_VERSION})

  if [ "${CURRENT_IMAGE}" != "" ]; then
    info "ECLIPSE CHE: ALREADY HAVE IMAGE ${CHE_LAUNCHER_IMAGE_NAME}:${CHE_VERSION}"
  else
    info "ECLIPSE CHE: PULLING IMAGE ${CHE_LAUNCHER_IMAGE_NAME}:${CHE_VERSION}"
    execute_command_with_progress extended docker pull ${CHE_LAUNCHER_IMAGE_NAME}:${CHE_VERSION}
    info "ECLIPSE CHE: IMAGE ${CHE_LAUNCHER_IMAGE_NAME}:${CHE_VERSION} INSTALLED"
  fi
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

init_logging
check_docker
init_global_variables
parse_command_line "$@"

case ${CHE_CLI_ACTION} in
  start|stop|restart|update|info)
    execute_che_launcher
  ;;
  update-cli)
    update_che_launcher
  ;;
  help)
    usage
  ;;
esac
