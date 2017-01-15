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
  FAST_BOOT=false
  CHE_DEBUG=false

  DEFAULT_CHE_PRODUCT_NAME="CHE"
  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="che"
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}

  DEFAULT_CHE_FORMAL_PRODUCT_NAME="Eclipse Che"
  CHE_FORMAL_PRODUCT_NAME=${CHE_FORMAL_PRODUCT_NAME:-${DEFAULT_CHE_FORMAL_PRODUCT_NAME}}

  # Path to root folder inside the container
  DEFAULT_CHE_CONTAINER_ROOT="/data"
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

  DEFAULT_CHE_IMAGE_FULLNAME="eclipse/che-cli:<version>"
  CHE_IMAGE_FULLNAME=${CHE_IMAGE_FULLNAME:-${DEFAULT_CHE_IMAGE_FULLNAME}}

  # Constants
  CHE_MANIFEST_DIR="/version"
  CHE_VERSION_FILE="${CHE_MINI_PRODUCT_NAME}.ver.do_not_modify"
  CHE_ENVIRONMENT_FILE="${CHE_MINI_PRODUCT_NAME}.env"
  CHE_COMPOSE_FILE="docker-compose-container.yml"
  CHE_HOST_COMPOSE_FILE="docker-compose.yml"

  DEFAULT_CHE_SERVER_CONTAINER_NAME="${CHE_MINI_PRODUCT_NAME}"
  CHE_SERVER_CONTAINER_NAME="${CHE_SERVER_CONTAINER_NAME:-${DEFAULT_CHE_SERVER_CONTAINER_NAME}}"

  CHE_BACKUP_FILE_NAME="${CHE_MINI_PRODUCT_NAME}_backup.tar.gz"
  CHE_COMPOSE_STOP_TIMEOUT="180"

  DEFAULT_CHE_CLI_ACTION="help"
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}

  DEFAULT_CHE_LICENSE=false
  CHE_LICENSE=${CHE_LICENSE:-${DEFAULT_CHE_LICENSE}}

  # Replace all of these with digests
  UTILITY_IMAGE_ALPINE="alpine:3.4"
  UTILITY_IMAGE_CHEIP="eclipse/che-ip:nightly"
  UTILITY_IMAGE_CHEACTION="eclipse/che-action:nightly"
  UTILITY_IMAGE_CHEDIR="eclipse/che-dir:nightly"
  UTILITY_IMAGE_CHETEST="eclipse/che-test:nightly"
  UTILITY_IMAGE_CHEMOUNT="eclipse/che-mount:nightly"
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

usage() {
  debug $FUNCNAME
  init_usage
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

debug_server() {
  if [ "${CHE_DEBUG}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_fast() {
  if [ "${FAST_BOOT}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

init_logging() {
  # Initialize CLI folder
  CLI_DIR=$CHE_CONTAINER_ROOT
  test -d "${CLI_DIR}" || mkdir -p "${CLI_DIR}"

  # Ensure logs folder exists
  LOGS="${CLI_DIR}/cli.log"
  LOG_INITIALIZED=true

  # Log date of CLI execution
  log "$(date)"
}

init() {
  init_constants

  # If there are no parameters, immediately display usage
  if [[ $# == 0 ]]; then
    usage;
  fi

  if [[ "$@" == *"--fast"* ]]; then
  	FAST_BOOT=true
  fi

  if [[ "$@" == *"--debug"* ]]; then
  	CHE_DEBUG=true
  fi

  SCRIPTS_BASE_CONTAINER_SOURCE_DIR="/scripts/base"
  # add helper scripts
  for HELPER_FILE in "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/*.sh
  do
    source "${HELPER_FILE}"
  done

  # Make sure Docker is working and we have /var/run/docker.sock mounted or valid DOCKER_HOST
  check_docker "$@"
  
  # Check to see if Docker is configured with a proxy and pull values
  check_docker_networking

  # Verify that -i is passed on the command line
  check_interactive "$@"

  # Only verify mounts after Docker is confirmed to be working.
  check_mounts "$@"

  # Only initialize after mounts have been established so we can write cli.log out to a mount folder
  init_logging "$@"

  SCRIPTS_CONTAINER_SOURCE_DIR=""
  if $CHE_LOCAL_REPO; then
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

  # If offline mode, then load dependent images from disk and populate the local Docker cache.
  # If not in offline mode, verify that we have access to DockerHub.
  # This is also the first usage of curl
  initiate_offline_or_network_mode "$@"

  # Pull the list of images that are necessary. If in offline mode, verifies that the images
  # are properly loaded into the cache.
  grab_initial_images
}

cli_init() {
  CHE_HOST=$(eval "echo \$${CHE_PRODUCT_NAME}_HOST")
  CHE_PORT=$(eval "echo \$${CHE_PRODUCT_NAME}_PORT")

  if [[ "$(eval "echo \$${CHE_PRODUCT_NAME}_HOST")" = "" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
    info ""
    info "We did not auto-detect a valid HOST or IP address."
    info "Pass ${CHE_PRODUCT_NAME}_HOST with your hostname or IP address."
    info ""
    info "Rerun the CLI:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <local-path>:${CHE_CONTAINER_ROOT}"
    info "                      -e ${CHE_PRODUCT_NAME}_HOST=<your-ip-or-host>"
    info "                         $CHE_IMAGE_FULLNAME $*"
    return 2;
  fi

  # Derived variablea
  REFERENCE_HOST_ENVIRONMENT_FILE="${CHE_HOST_CONFIG}/${CHE_ENVIRONMENT_FILE}"
  REFERENCE_HOST_COMPOSE_FILE="${CHE_HOST_INSTANCE}/${CHE_COMPOSE_FILE}"
  REFERENCE_CONTAINER_ENVIRONMENT_FILE="${CHE_CONTAINER_CONFIG}/${CHE_ENVIRONMENT_FILE}"
  REFERENCE_CONTAINER_COMPOSE_FILE="${CHE_CONTAINER_INSTANCE}/${CHE_COMPOSE_FILE}"
  REFERENCE_CONTAINER_COMPOSE_HOST_FILE="${CHE_CONTAINER_INSTANCE}/${CHE_HOST_COMPOSE_FILE}"

  CHE_CONTAINER_OFFLINE_FOLDER="${CHE_CONTAINER_BACKUP}"
  CHE_HOST_OFFLINE_FOLDER="${CHE_HOST_BACKUP}"

  CHE_HOST_CONFIG_MANIFESTS_FOLDER="${CHE_HOST_INSTANCE}/manifests"
  CHE_CONTAINER_CONFIG_MANIFESTS_FOLDER="${CHE_CONTAINER_INSTANCE}/manifests"

  CHE_HOST_CONFIG_MODULES_FOLDER="${CHE_HOST_INSTANCE}/modules"
  CHE_CONTAINER_CONFIG_MODULES_FOLDER="${CHE_CONTAINER_INSTANCE}/modules"

  # TODO: Change this to use the current folder or perhaps ~?
  if is_boot2docker && has_docker_for_windows_client; then
    if [[ "${CHE_HOST_INSTANCE,,}" != *"${USERPROFILE,,}"* ]]; then
      CHE_HOST_INSTANCE=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CHE_INSTANCE set to $CHE_HOST_INSTANCE"
    fi
    if [[ "${CHE_HOST_CONFIG,,}" != *"${USERPROFILE,,}"* ]]; then
      CHE_HOST_CONFIG=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CHE_CONFIG set to $CHE_HOST_CONFIG"
    fi
  fi

  # Special function to perform special behaviors if you are running nightly version
  verify_nightly_accuracy

  # Do not perform a version compatibility check if running upgrade command.
  # The upgrade command has its own internal checks for version compatibility.
  if [ $1 != "upgrade" ]; then
  	if [[ "${FAST_BOOT}" = "false" ]]; then
      verify_version_compatibility
    else
      warning "Skipping version compatibility check..."
    fi
  else
    verify_version_upgrade_compatibility
  fi
}

cleanup() {
  RETURN_CODE=$?

  # CLI developers should only return '3' in code after the init() method has completed.
  # This will check to see if the CLI directory is not mounted and only offer the error
  # message if it isn't currently mounted.
  if [ $RETURN_CODE -eq "3" ]; then
    error ""
    error "Unexpected exit: Trace output saved to $CHE_HOST_CONFIG/cli.log."
  fi
}

start() {
  # Bootstrap networking, docker, logging, and ability to load cli.sh and cli-functions.sh
  init "$@"

  # Removes "--fast" and "--debug" from the positional arguments if it is set.
  set -- "${@/\-\-fast/}"
  set -- "${@/\-\-debug/}"
  
  # Begin product-specific CLI calls
  info "cli" "Loading cli..."


  # The pre_init method is unique to each assembly. This method must be provided by 
  # a custom CLI assembly in their container and can set global variables which are 
  # specific to that implementation of the CLI. This method must be called after
  # networking has been established and initial images downloaded.
  cli_pre_init

  cli_init "$@"
  cli_parse "$@"
  cli_execute "$@"
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

trap "cleanup" INT TERM EXIT
