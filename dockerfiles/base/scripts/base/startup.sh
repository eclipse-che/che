#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html



init_constants() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[38;5;220m'
  BOLD='\033[1m'
  UNDERLINE='\033[4m'
  NC='\033[0m'
  LOG_INITIALIZED=false

  DEFAULT_CHE_PRODUCT_NAME="CHE"
  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="che"
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}

  DEFAULT_CHE_FORMAL_PRODUCT_NAME="Eclipse Che"
  CHE_FORMAL_PRODUCT_NAME=${CHE_FORMAL_PRODUCT_NAME:-${DEFAULT_CHE_FORMAL_PRODUCT_NAME}}

  # Path to root folder inside the container
  DEFAULT_CHE_CONTAINER_ROOT="/${CHE_MINI_PRODUCT_NAME}"
  CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT:-${DEFAULT_CHE_CONTAINER_ROOT}}

  # Turns on stack trace
  DEFAULT_CHE_CLI_DEBUG="false"
  CHE_CLI_DEBUG=${CLI_DEBUG:-${DEFAULT_CHE_CLI_DEBUG}}

  # Activates console output
  DEFAULT_CHE_CLI_INFO="true"
  CHE_CLI_INFO=${CLI_INFO:-${DEFAULT_CHE_CLI_INFO}}

  # Activates console warnings
  DEFAULT_CHE_CLI_WARN="true"
  CHE_CLI_WARN=${CLI_WARN:-${DEFAULT_CHE_CLI_WARN}}

  # Activates console output
  DEFAULT_CHE_CLI_LOG="true"
  CHE_CLI_LOG=${CLI_LOG:-${DEFAULT_CHE_CLI_LOG}}

  DEFAULT_CHE_ASSEMBLY_IN_REPO_MODULE_NAME="assembly/assembly-main"
  CHE_ASSEMBLY_IN_REPO_MODULE_NAME=${CHE_ASSEMBLY_IN_REPO_MODULE_NAME:-${DEFAULT_CHE_ASSEMBLY_IN_REPO_MODULE_NAME}}

  DEFAULT_CHE_ASSEMBLY_IN_REPO="${DEFAULT_CHE_ASSEMBLY_IN_REPO_MODULE_NAME}/target/eclipse-che*/eclipse-che-*"
  CHE_ASSEMBLY_IN_REPO=${CHE_ASSEMBLY_IN_REPO:-${DEFAULT_CHE_ASSEMBLY_IN_REPO}}

  DEFAULT_CHE_SCRIPTS_CONTAINER_SOURCE_DIR="/repo/dockerfiles/cli/scripts"
  CHE_SCRIPTS_CONTAINER_SOURCE_DIR=${CHE_SCRIPTS_CONTAINER_SOURCE_DIR:-${DEFAULT_CHE_SCRIPTS_CONTAINER_SOURCE_DIR}}

  DEFAULT_CHE_LICENSE_URL="https://www.eclipse.org/legal/epl-v10.html"
  CHE_LICENSE_URL=${CHE_LICENSE_URL:-${DEFAULT_CHE_LICENSE_URL}}

}


# Sends arguments as a text to CLI log file
# Usage:
#   log <argument> [other arguments]
log() {
  if [[ "$LOG_INITIALIZED"  = "true" ]]; then
    if is_log; then
      echo "$@" >> "${LOGS}"
    fi
  fi
}

usage () {
  debug $FUNCNAME
  printf "%s" "${USAGE}"
  return 1;
}

warning() {
  if is_warning; then
    printf  "${YELLOW}WARN:${NC} %s\n" "${1}"
  fi
  log $(printf "WARN: %s\n" "${1}")
}

info() {
  if [ -z ${2+x} ]; then
    PRINT_COMMAND=""
    PRINT_STATEMENT=$1
  else
    PRINT_COMMAND="($CHE_MINI_PRODUCT_NAME $1): "
    PRINT_STATEMENT=$2
  fi
  if is_info; then
    printf "${GREEN}INFO:${NC} %b%b\n" \
              "${PRINT_COMMAND}" \
              "${PRINT_STATEMENT}"
  fi
  log $(printf "INFO: %b %b\n" \
        "${PRINT_COMMAND}" \
        "${PRINT_STATEMENT}")
}

debug() {
  if is_debug; then
    printf  "\n${BLUE}DEBUG:${NC} %s" "${1}"
  fi
  log $(printf "\nDEBUG: %s" "${1}")
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
  log $(printf  "ERROR: %s\n" "${1}")
}

# Prints message without changes
# Usage: has the same syntax as printf command
text() {
  printf "$@"
  log $(printf "$@")
}

## TODO use that for all native calls to improve logging for support purposes
# Executes command with 'eval' command.
# Also logs what is being executed and stdout/stderr
# Usage:
#   cli_eval <command to execute>
# Examples:
#   cli_eval "$(which curl) http://localhost:80/api/"
cli_eval() {
  log "$@"
  tmpfile=$(mktemp)
  if eval "$@" &>"${tmpfile}"; then
    # Execution succeeded
    cat "${tmpfile}" >> "${LOGS}"
    cat "${tmpfile}"
    rm "${tmpfile}"
  else
    # Execution failed
    cat "${tmpfile}" >> "${LOGS}"
    cat "${tmpfile}"
    rm "${tmpfile}"
    fail
  fi
}

# Executes command with 'eval' command and suppress stdout/stderr.
# Also logs what is being executed and stdout+stderr
# Usage:
#   cli_silent_eval <command to execute>
# Examples:
#   cli_silent_eval "$(which curl) http://localhost:80/api/"
cli_silent_eval() {
  log "$@"
  eval "$@" >> "${LOGS}" 2>&1
}

is_log() {
  if [ "${CHE_CLI_LOG}" = "true" ]; then
    return 0
  else
    return 1
  fi
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


init_logging() {
  # Initialize CLI folder
  CLI_DIR="/cli"
  test -d "${CLI_DIR}" || mkdir -p "${CLI_DIR}"

  # Ensure logs folder exists
  LOGS="${CLI_DIR}/cli.log"
  LOG_INITIALIZED=true

  # Log date of CLI execution
  log "$(date)"
}



init() {
  init_constants

  SCRIPTS_BASE_CONTAINER_SOURCE_DIR="/scripts/base"
  # add helper scripts
  for HELPER_FILE in "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/*.sh
  do
    source "${HELPER_FILE}"
  done

  # Make sure Docker is working and we have /var/run/docker.sock mounted or valid DOCKER_HOST
  check_docker "$@"

  init_usage
  if [[ $# == 0 ]]; then
    usage;
  fi

  # Only verify mounts after Docker is confirmed to be working.
  check_mounts "$@"

  # Only initialize after mounts have been established so we can write cli.log out to a mount folder
  init_logging "$@"

  SCRIPTS_CONTAINER_SOURCE_DIR=""
  if [[ "${CHE_DEVELOPMENT_MODE}" = "on" ]]; then
     # Use the CLI that is inside the repository.
     SCRIPTS_CONTAINER_SOURCE_DIR=${CHE_SCRIPTS_CONTAINER_SOURCE_DIR}
  else
     # Use the CLI that is inside the container.
     SCRIPTS_CONTAINER_SOURCE_DIR="/scripts"
  fi

  # Primary source directory
  source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/cli/cli-functions.sh

  # add base commands
  for BASECOMMAND_FILE in "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/commands/*.sh
  do
    source "${BASECOMMAND_FILE}"
  done

  source "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cli.sh
}


start() {
  # Bootstrap enough stuff to load /cli/cli.sh
  init "$@"

  # Begin product-specific CLI calls
  info "cli" "Loading cli..."
  cli_pre_init
  cli_init "$@"
  cli_parse "$@"
  cli_execute "$@"

}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u
