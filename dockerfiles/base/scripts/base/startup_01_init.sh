#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#


init_usage() {
  USAGE="
USAGE: 
  docker run -it --rm <DOCKER_PARAMETERS> ${CHE_IMAGE_FULLNAME} [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}                Where user, instance, and log data saved${ADDITIONAL_MANDATORY_PARAMETERS}  

OPTIONAL DOCKER PARAMETERS:${ADDITIONAL_OPTIONAL_DOCKER_PARAMETERS}  
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/repo                ${CHE_MINI_PRODUCT_NAME} git repo - uses local binaries and manifests
  -v <LOCAL_PATH>:/assembly            ${CHE_MINI_PRODUCT_NAME} assembly - uses local binaries 
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimizing sync command resides
  -v <LOCAL_PATH>:/chedir              Soure repository to convert into workspace with Chedir utility${ADDITIONAL_OPTIONAL_DOCKER_MOUNTS}  
    
COMMANDS:
  archetype                            Generate, build, and run custom assemblies of ${CHE_MINI_PRODUCT_NAME}
  action <action-name>                 Start action on ${CHE_MINI_PRODUCT_NAME} instance
  backup                               Backups ${CHE_MINI_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
  config                               Generates a ${CHE_MINI_PRODUCT_NAME} config from vars; run on any start / restart
  destroy                              Stops services, and deletes ${CHE_MINI_PRODUCT_NAME} instance data
  dir <command>                        Use Chedir and Chefile in the directory mounted to :/chedir
  download                             Pulls Docker images for the current ${CHE_MINI_PRODUCT_NAME} version
  help                                 This message
  info                                 Displays info about ${CHE_MINI_PRODUCT_NAME} and the CLI
  init                                 Initializes a directory with a ${CHE_MINI_PRODUCT_NAME} install
  offline                              Saves ${CHE_MINI_PRODUCT_NAME} Docker images into TAR files for offline install
  restart                              Restart ${CHE_MINI_PRODUCT_NAME} services
  restore                              Restores ${CHE_MINI_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
  rmi                                  Removes the Docker images for <version>, forcing a repull
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  start                                Starts ${CHE_MINI_PRODUCT_NAME} services
  stop                                 Stops ${CHE_MINI_PRODUCT_NAME} services
  sync <wksp-name>                     Synchronize workspace with local directory mounted to :/sync
  test <test-name>                     Start test on ${CHE_MINI_PRODUCT_NAME} instance
  upgrade                              Upgrades ${CHE_MINI_PRODUCT_NAME} from one version to another with migrations and backups
  version                              Installed version and upgrade paths${ADDITIONAL_COMMANDS}

GLOBAL COMMAND OPTIONS:
  --fast                               Skips networking, version, nightly and preflight checks
  --offline                            Runs CLI in offline mode, loading images from disk
  --debug                              Enable debugging of ${CHE_MINI_PRODUCT_NAME} server
  --trace                              Activates trace output for debugging CLI${ADDITIONAL_GLOBAL_OPTIONS}
  --help                               Get help for a command  
"
}

init_constants() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[38;5;220m'
  BOLD='\033[1m'
  UNDERLINE='\033[4m'
  NC='\033[0m'

  # CLI DEVELOPERS - ONLY INCREMENT THIS CHANGE IF MODIFYING SECTIONS THAT AFFECT LOADING
  #                  BEFORE :/REPO IS VOLUME MOUNTED.  CLI ASSEMBLIES WILL FAIL UNTIL THEY
  #                  ARE RECOMPILED WITH MATCHING VERSION.
  CHE_BASE_API_VERSION=2
}

init_global_vars() {
  LOG_INITIALIZED=false
  FAST_BOOT=false
  CHE_DEBUG=false
  CHE_OFFLINE=false
  CHE_SKIP_NIGHTLY=false
  CHE_SKIP_NETWORK=false
  CHE_SKIP_PULL=false
  CHE_COMMAND_HELP=false
  CHE_SKIP_SCRIPTS=false

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

  DEFAULT_CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR="/scripts/base"
  CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR=${CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR:-${DEFAULT_CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR}}

  DEFAULT_CHE_LICENSE_URL="https://www.eclipse.org/legal/epl-2.0/"
  CHE_LICENSE_URL=${CHE_LICENSE_URL:-${DEFAULT_CHE_LICENSE_URL}}

  DEFAULT_CHE_IMAGE_FULLNAME="eclipse/che-cli:<version>"
  CHE_IMAGE_FULLNAME=${CHE_IMAGE_FULLNAME:-${DEFAULT_CHE_IMAGE_FULLNAME}}

  # Constants
  CHE_MANIFEST_DIR="/version"
  CHE_VERSION_FILE="${CHE_MINI_PRODUCT_NAME}.ver.do_not_modify"
  CHE_ENVIRONMENT_FILE="${CHE_MINI_PRODUCT_NAME}.env"
  CHE_COMPOSE_FILE="docker-compose-container.yml"
  CHE_HOST_COMPOSE_FILE="docker-compose.yml"

  # Keep for backwards compatibility
  DEFAULT_CHE_SERVER_CONTAINER_NAME="${CHE_MINI_PRODUCT_NAME}"
  CHE_SERVER_CONTAINER_NAME="${CHE_SERVER_CONTAINER_NAME:-${DEFAULT_CHE_SERVER_CONTAINER_NAME}}"
 
  DEFAULT_CHE_CONTAINER_NAME="${CHE_SERVER_CONTAINER_NAME}"
  CHE_CONTAINER_NAME="${CHE_CONTAINER:-${DEFAULT_CHE_CONTAINER_NAME}}"

  DEFAULT_CHE_CONTAINER_PREFIX="${CHE_SERVER_CONTAINER_NAME}"
  CHE_CONTAINER_PREFIX="${CHE_CONTAINER_PREFIX:-${DEFAULT_CHE_CONTAINER_PREFIX}}"

  CHE_BACKUP_FILE_NAME="${CHE_MINI_PRODUCT_NAME}_backup.tar.gz"
  CHE_COMPOSE_STOP_TIMEOUT="180"

  DEFAULT_CHE_CLI_ACTION="help"
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}

  DEFAULT_CHE_LICENSE=false
  CHE_LICENSE=${CHE_LICENSE:-${DEFAULT_CHE_LICENSE}}

  if [[ "${CHE_CONTAINER_NAME}" = "${CHE_MINI_PRODUCT_NAME}" ]]; then   
    if [[ "${CHE_PORT}" != "${DEFAULT_CHE_PORT}" ]]; then
      CHE_CONTAINER_NAME="${CHE_CONTAINER_PREFIX}-${CHE_PORT}"
    else 
      CHE_CONTAINER_NAME="${CHE_CONTAINER_PREFIX}"
    fi
  fi

  DEFAULT_CHE_COMPOSE_PROJECT_NAME="${CHE_CONTAINER_NAME}"
  CHE_COMPOSE_PROJECT_NAME="${CHE_COMPOSE_PROJECT_NAME:-${DEFAULT_CHE_COMPOSE_PROJECT_NAME}}"

  DEFAULT_CHE_USER="root"
  CHE_USER="${CHE_USER:-${DEFAULT_CHE_USER}}"

  CHE_USER_GROUPS=""

  UNAME_R=${UNAME_R:-$(uname -r)}

}

usage() {
 # debug $FUNCNAME
  init_usage
  printf "%s" "${USAGE}"
}

init_cli_version_check() {
  if [[ $CHE_BASE_API_VERSION != $CHE_CLI_API_VERSION ]]; then
    printf "CLI base ($CHE_BASE_API_VERSION) does not match CLI ($CHE_CLI_API_VERSION) version.\n"
    printf "Recompile the CLI with the latest version of the CLI base.\n"
    return 1;
  fi
}

init_usage_check() {
  # If there are no parameters, immediately display usage

  if [[ $# == 0 ]]; then
    usage
    return 1
  fi

  if [[ "$@" == *"--fast"* ]]; then
    FAST_BOOT=true
  fi

  if [[ "$@" == *"--debug"* ]]; then
    CHE_DEBUG=true
  fi

  if [[ "$@" == *"--offline"* ]]; then
    CHE_OFFLINE=true
  fi

  if [[ "$@" == *"--trace"* ]]; then
    CHE_TRACE=true
    set -x
  fi

  if [[ "$@" == *"--skip:nightly"* ]]; then
    CHE_SKIP_NIGHTLY=true
  fi

  if [[ "$@" == *"--skip:network"* ]]; then
    CHE_SKIP_NETWORK=true
  fi

  if [[ "$@" == *"--skip:pull"* ]]; then
    CHE_SKIP_PULL=true
  fi

  if [[ "$@" == *"--help"* ]]; then
    CHE_COMMAND_HELP=true
  fi

  if [[ "$@" == *"--skip:scripts"* ]]; then
    CHE_SKIP_SCRIPTS=true
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
  # pre_init is unique to each CLI assembly. This can be called before networking is established.
  source "/scripts/pre_init.sh"
  pre_init

  # Yo, constants
  init_constants

  # Variables used throughout
  init_global_vars

  # Check to make sure CLI assembly matches base
  init_cli_version_check

  # Checks for global parameters
  init_usage_check "$@"

  # Removes global parameters from the positional arguments
  ORIGINAL_PARAMETERS=$@
  set -- "${@/\-\-fast/}"
  set -- "${@/\-\-debug/}"
  set -- "${@/\-\-offline/}"
  set -- "${@/\-\-trace/}"
  set -- "${@/\-\-skip\:nightly/}"
  set -- "${@/\-\-skip\:network/}"
  set -- "${@/\-\-skip\:pull/}"
  set -- "${@/\-\-help/}"
  set -- "${@/\-\-skip\:scripts/}"

  source "${CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR}"/startup_02_pre_docker.sh

  # Make sure Docker is working and we have /var/run/docker.sock mounted or valid DOCKER_HOST
  init_check_docker "$@"

  # Check to see if Docker is configured with a proxy and pull values
  init_check_docker_networking

  # Verify that -i is passed on the command line
  init_check_interactive "$@"

  # Only verify mounts after Docker is confirmed to be working.
  init_check_mounts "$@"

  # Extract the value of --user from the docker command line
  init_check_user "$@"

  # Extract the value of --group-add from the docker command line
  init_check_groups "$@"

  # Only initialize after mounts have been established so we can write cli.log out to a mount folder
  init_logging "$@"

  # Determine where the remaining scripts will be sourced from (inside image, or repo?)
  init_scripts "$@"

  # We now know enough to load scripts from different locations - so source from proper source
  source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/startup_03_pre_networking.sh

  # If offline mode, then load dependent images from disk and populate the local Docker cache.
  # If not in offline mode, verify that we have access to DockerHub.
  # This is also the first usage of curl
  init_offline_or_network_mode "$@"

  # Pull the list of images that are necessary. If in offline mode, verifies that the images
  # are properly loaded into the cache.
  init_initial_images "$@"

  # Each CLI assembly must provide this cli.sh - loads overridden functions and variables for the CLI
  source "${SCRIPTS_CONTAINER_SOURCE_DIR}"/post_init.sh

  # The post_init method is unique to each assembly. This method must be provided by 
  # a custom CLI assembly in their container and can set global variables which are 
  # specific to that implementation of the CLI. Place initialization functions that
  # require networking here.
  post_init

  # Begin product-specific CLI calls
  info "cli" "$CHE_VERSION - using docker ${DOCKER_SERVER_VERSION} / $(get_docker_install_type)"

  source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/startup_04_pre_cli_init.sh

  # Allow CLI assemblies to load variables assuming networking, logging, docker activated  
  cli_pre_init

  # Set CHE_HOST, CHE_PORT, and apply any CLI-specific command-line overrides to variables  
  cli_init "$@"

  # Additional checks for nightly version
  cli_verify_nightly "$@"

  # Additional checks to verify image matches version installed on disk & upgrade suitability
  cli_verify_version "$@"

  # Allow CLI assemblies to load variables assuming CLI is finished bootstrapping
  cli_post_init

  source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/startup_05_pre_exec.sh

  # Loads the library and associated dependencies
  cli_load "$@"

  # Parses the command list for validity
  cli_parse "$@"

  # Executes command lifecycle
  cli_execute "$@"
}
