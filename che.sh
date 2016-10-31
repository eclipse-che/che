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

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[38;5;220m'
  NC='\033[0m'

  # Which che CLI version to run?
  DEFAULT_CHE_CLI_VERSION="latest"
  CHE_CLI_VERSION=${CHE_CLI_VERSION:-${DEFAULT_CHE_CLI_VERSION}}

  DEFAULT_CHE_PRODUCT_NAME="ECLIPSE CHE"
  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="che"
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}

  # Turns on stack trace
  DEFAULT_CHE_CLI_DEBUG="false"
  CHE_CLI_DEBUG=${CHE_CLI_DEBUG:-${DEFAULT_CHE_CLI_DEBUG}}

  # Activates console output
  DEFAULT_CHE_CLI_INFO="true"
  CHE_CLI_INFO=${CHE_CLI_INFO:-${DEFAULT_CHE_CLI_INFO}}

  # Activates console warnings
  DEFAULT_CHE_CLI_WARN="true"
  CHE_CLI_WARN=${CHE_CLI_WARN:-${DEFAULT_CHE_CLI_WARN}}
}

warning() {
  if is_warning; then
    printf  "${YELLOW}WARN:${NC} %s\n" "${1}"
  fi
}

info() {
  if is_info; then
    if [ -z ${2+x} ]; then 
      PRINT_COMMAND=""
      PRINT_STATEMENT=$1
    else
      PRINT_COMMAND="($CHE_MINI_PRODUCT_NAME $1):"
      PRINT_STATEMENT=$2
    fi
    printf  "${GREEN}INFO:${NC} %s %s\n" \
              "${PRINT_COMMAND}" \
              "${PRINT_STATEMENT}"
  fi
}

debug() {
  if is_debug; then
    printf  "\n${BLUE}DEBUG:${NC} %s" "${1}"
  fi
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
}

is_warning() {
  if [ "${CHE_CLI_WARN}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_info() {
  if [ "${CHE_CLI_INFO}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_debug() {
  if [ "${CHE_CLI_DEBUG}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

has_docker() {
  hash docker 2>/dev/null && return 0 || return 1
}

check_docker() {
  if ! has_docker; then
    error "Error - Docker not found. Get it at https://docs.docker.com/engine/installation/."
    return 1;
  fi

  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    error "Error - Docker not installed properly: \n${output}"
    return 1;
  fi

  # Prep script by getting default image
  if [ "$(docker images -q alpine 2> /dev/null)" = "" ]; then
    info "Pulling image alpine:latest"
    docker pull alpine > /dev/null 2>&1
  fi

  if [ "$(docker images -q appropriate/curl 2> /dev/null)" = "" ]; then
    info "Pulling image curl:latest"
    docker pull appropriate/curl > /dev/null 2>&1
  fi
}

curl () {
  docker run --rm --net=host appropriate/curl "$@"
}

update_che_cli() {
  info "Downloading cli-$CHE_CLI_VERSION"

  CLI_DIR=~/."${CHE_MINI_PRODUCT_NAME}"/cli
  test -d "${CLI_DIR}" || mkdir -p "${CLI_DIR}"

  if [[ "${CHE_CLI_VERSION}" = "latest" ]] || \
     [[ "${CHE_CLI_VERSION}" = "nightly" ]] || \
     [[ ${CHE_CLI_VERSION:0:1} == "4" ]]; then
    GITHUB_VERSION=master
  else
    GITHUB_VERSION=$CHE_CLI_VERSION
  fi
  
  URL=https://raw.githubusercontent.com/eclipse/che/$GITHUB_VERSION/cli.sh

  if ! curl --output /dev/null --silent --head --fail "$URL"; then
    error "CLI download error. Bad network or version. CLI update works for 5.0.0-M3+."
    return 1;
  else 
    curl -sL $URL > ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-$CHE_CLI_VERSION.sh
  fi
}

init() {
  init_logging
  check_docker

  # Test to see if we have cli_funcs
  if [ ! -f ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-${CHE_CLI_VERSION}.sh ]; then
    update_che_cli
  fi

  source ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-${CHE_CLI_VERSION}.sh

  init_global_variables
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u
init
parse_command_line "$@"
execute_cli "$@"
