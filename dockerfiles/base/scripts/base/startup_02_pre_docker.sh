#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

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

warning() {
  if [ -z ${2+x} ]; then
    local PRINT_COMMAND=""
    local PRINT_STATEMENT=$1
  else
    local PRINT_COMMAND="($CHE_MINI_PRODUCT_NAME $1): "
    local PRINT_STATEMENT=$2
  fi

  if is_warning; then
    printf "${YELLOW}WARN:${NC} %b%b\n" \
              "${PRINT_COMMAND}" \
              "${PRINT_STATEMENT}"
  fi
  log $(printf "INFO: %b %b\n" \
        "${PRINT_COMMAND}" \
        "${PRINT_STATEMENT}")
}

info() {
  if [ -z ${2+x} ]; then
    local PRINT_COMMAND=""
    local PRINT_STATEMENT=$1
  else
    local PRINT_COMMAND="($CHE_MINI_PRODUCT_NAME $1): "
    local PRINT_STATEMENT=$2
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

is_offline() {
  if [ "${CHE_OFFLINE}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_trace() {
  if [ "${CHE_TRACE}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

skip_nightly() {
  if [ "${CHE_SKIP_NIGHTLY}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

skip_network() {
  if [ "${CHE_SKIP_NETWORK}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

skip_pull() {
  if [ "${CHE_SKIP_PULL}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

local_repo() {
  if [ "${CHE_LOCAL_REPO}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

local_assembly() {
  if [ "${CHE_LOCAL_ASSEMBLY}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

get_command_help() {
  if [ "${CHE_COMMAND_HELP}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

custom_user() {
  if [ "${CHE_USER}" != "${DEFAULT_CHE_USER}" ]; then
    return 0
  else
    return 1
  fi
}

skip_scripts() {
  if [ "${CHE_SKIP_SCRIPTS}" = "true" ]; then
    return 0
  else
    return 1
  fi
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

  if is_initialized; then 
    CHE_HOST_LOCAL=$(get_value_of_var_from_env_file ${CHE_PRODUCT_NAME}_HOST)
    if [[ "${CHE_HOST}" != "${CHE_HOST_LOCAL}" ]]; then
      warning "${CHE_PRODUCT_NAME}_HOST (${CHE_HOST}) overridden by ${CHE_ENVIRONMENT_FILE} (${CHE_HOST_LOCAL})"
    fi
  fi

  # Special function to perform special behaviors if you are running nightly version
  verify_nightly_accuracy

  # Do not perform a version compatibility check if running upgrade command.
  # The upgrade command has its own internal checks for version compatibility.
  if [[ "$@" == *"upgrade"* ]]; then
    verify_version_upgrade_compatibility
  elif ! is_fast; then
    verify_version_compatibility
  fi
}

init_check_docker() {
  if ! has_docker; then
    error "Docker not found. Get it at https://docs.docker.com/engine/installation/."
    return 1;
  fi

  CHECK_VERSION=$(docker ps 2>&1 || true)
  if [[ "$CHECK_VERSION" = *"Error response from daemon: client is newer"* ]]; then
    error "Error - Docker engine 1.11+ required."
    return 2;
  fi

  # If DOCKER_HOST is not set, then it should bind mounted
  if [ -z "${DOCKER_HOST+x}" ]; then
    if ! docker ps > /dev/null 2>&1; then
      info "Welcome to ${CHE_FORMAL_PRODUCT_NAME}!"
      info ""
      info "You are missing a mandatory parameter:"
      info "   1. Mount 'docker.sock' for accessing Docker with unix sockets."
      info "   2. Or, set DOCKER_HOST to Docker's location (unix or tcp)."
      info ""
      info "Mount Syntax:"
      info "   Start with 'docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock' ..."
      info ""
      info "DOCKER_HOST Syntax:"
      info "   Start with 'docker run -it --rm -e DOCKER_HOST=<daemon-location> ...'"
      info ""
      info "Possible root causes:"
      info "   1. Your admin has not granted permissions to /var/run/docker.sock."
      info "   2. You passed '--user uid:gid' with bad values."
      info "   3. Your firewall is blocking TCP ports for accessing Docker daemon."
      return 2;
    fi
  fi

  DOCKER_CLIENT_VERSION=$(docker version --format '{{.Client.Version}}')
  DOCKER_SERVER_VERSION=$(docker version --format '{{.Server.Version}}')

  # Detect version so that we can provide better error warnings
  DEFAULT_CHE_VERSION=$(cat "/version/latest.ver")
  CHE_IMAGE_FULLNAME=$(docker inspect --format='{{.Config.Image}}' $(get_this_container_id))

  # Note - cut command here fails if there is no colon : in the image
  CHE_IMAGE_NAME=${CHE_IMAGE_FULLNAME%:*}
  CHE_IMAGE_VERSION=$(echo "${CHE_IMAGE_FULLNAME}" | cut -d : -f2 -s)
  if [[ "${CHE_IMAGE_VERSION}" = "" ]] ||
     [[ "${CHE_IMAGE_VERSION}" = "latest" ]]; then
    CHE_IMAGE_VERSION=$DEFAULT_CHE_VERSION
    warning "Bound '$CHE_IMAGE_NAME' to '$CHE_IMAGE_NAME:$CHE_IMAGE_VERSION'"
  else
    CHE_IMAGE_VERSION=$CHE_IMAGE_VERSION
  fi

  CHE_VERSION=$CHE_IMAGE_VERSION
}

init_check_docker_networking() {
  # Check to see if HTTP_PROXY, HTTPS_PROXY, and NO_PROXY is set within the Docker daemon.
  OUTPUT=$(docker info)
  HTTP_PROXY=$(grep "Http Proxy" <<< "$OUTPUT" || true) 
  if [ ! -z "$HTTP_PROXY" ]; then
    HTTP_PROXY=${HTTP_PROXY#"Http Proxy: "}
  else 
    HTTP_PROXY=""
  fi

  HTTPS_PROXY=$(grep "Https Proxy" <<< "$OUTPUT" || true) 
  if [ ! -z "$HTTPS_PROXY" ]; then
    HTTPS_PROXY=${HTTPS_PROXY#"Https Proxy: "}
  else
    HTTPS_PROXY=""
  fi

  NO_PROXY=$(grep "No Proxy" <<< "$OUTPUT" || true) 
  if [ ! -z "$NO_PROXY" ]; then
    NO_PROXY=${NO_PROXY#"No Proxy: "}
  else
    NO_PROXY=""
  fi

  if [[ ! ${HTTP_PROXY} = "" ]] ||
     [[ ! ${HTTPS_PROXY} = "" ]] ||
     [[ ! ${NO_PROXY} = "" ]]; then
     info "Proxy: HTTP_PROXY=${HTTP_PROXY}, HTTPS_PROXY=${HTTPS_PROXY}, NO_PROXY=${NO_PROXY}"
     if [[ ${NO_PROXY} = "" ]]; then
     warning "Potential networking issue discovered!"
     warning "We have identified that http and https proxies are set but no_proxy is not. This may cause fatal networking errors. Set no_proxy for your Docker daemon!"
     fi
  fi

  export http_proxy=$HTTP_PROXY
  export https_proxy=$HTTPS_PROXY
  export no_proxy=$NO_PROXY
}

init_check_interactive() {
  # Detect and verify that the CLI container was started with -it option.
  TTY_ACTIVATED=true
  CHE_CLI_IS_INTERACTIVE=true

  # check if no terminal
  if [ ! -t 1 ]; then
    TTY_ACTIVATED=false
    CHE_CLI_IS_INTERACTIVE=false
    warning "Did not detect TTY - interactive mode disabled"
  else
    # There is a terminal, check if it's in interactive mode
    CHE_CLI_IS_INTERACTIVE=$(docker inspect --format='{{.Config.AttachStdin}}' $(get_this_container_id))
    if [[ ${CHE_CLI_IS_INTERACTIVE} == "false" ]]; then
      CHE_CLI_IS_INTERACTIVE=false
      warning "Did detect TTY but not in interactive mode"
    fi
  fi
}

# Add check to see if --user uid:gid passed in.
init_check_user() {
  DOCKER_CHE_USER=$(docker inspect --format='{{.Config.User}}' $(get_this_container_id))
  if [[ "${DOCKER_CHE_USER}" != "" ]]; then
    CHE_USER=$DOCKER_CHE_USER
  fi

  if custom_user; then
    true
  fi
}

# Extract groups of the docker run command
init_check_groups() {
  DOCKER_CHE_GROUPS=$(docker inspect --format='{{.HostConfig.GroupAdd}}' $(get_this_container_id) | cut -d '[' -f 2 | cut -d ']' -f 1 | xargs)
  if [[ "${DOCKER_CHE_GROUPS}" != "" ]]; then
    CHE_USER_GROUPS=${DOCKER_CHE_GROUPS}
  fi
}

init_check_mounts() {
  DATA_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}")
  INSTANCE_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}/instance")
  BACKUP_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}/backup")
  REPO_MOUNT=$(get_container_folder ":/repo")
  ASSEMBLY_MOUNT=$(get_container_folder ":/assembly")
  SYNC_MOUNT=$(get_container_folder ":/sync")
  UNISON_PROFILE_MOUNT=$(get_container_folder ":/unison")
  CHEDIR_MOUNT=$(get_container_folder ":/chedir")
  DOCKER_MOUNT=$(get_container_folder ":/var/run/docker.sock")
 
  if [[ "${DATA_MOUNT}" = "not set" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
    info ""
    info "We could not detect a location to save data."
    info "Volume mount a local directory to ':${CHE_CONTAINER_ROOT}'."
    info ""
    info "Simplest syntax:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                         ${CHE_IMAGE_FULLNAME} $*"
    info ""
    info ""
    info "Or, run with additional overrides:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                      -v <YOUR_INSTANCE_PATH>:${CHE_CONTAINER_ROOT}/instance"
    info "                      -v <YOUR_BACKUP_PATH>:${CHE_CONTAINER_ROOT}/backup"
    info "                         ${CHE_IMAGE_FULLNAME} $*"
    return 2;
  fi

  # Verify that we can write to the host file system from the container
  if ! is_fast; then 
    check_host_volume_mount
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

  # DERIVED VARIABLES FROM MOUNTS
  CHE_HOST_CONFIG=${CHE_CONFIG:-${DEFAULT_CHE_CONFIG}}
  CHE_CONTAINER_CONFIG="${CHE_CONTAINER_ROOT}"

  CHE_HOST_INSTANCE=${CHE_INSTANCE:-${DEFAULT_CHE_INSTANCE}}
  CHE_CONTAINER_INSTANCE="${CHE_CONTAINER_ROOT}/instance"

  CHE_HOST_BACKUP=${CHE_BACKUP:-${DEFAULT_CHE_BACKUP}}
  CHE_CONTAINER_BACKUP="${CHE_CONTAINER_ROOT}/backup"

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


  ### DEV MODE VARIABLES
  CHE_LOCAL_REPO=false
  if [[ "${REPO_MOUNT}" != "not set" ]]; then
    info "cli" "/repo mounted - using assembly and manifests from your local repository"

    CHE_LOCAL_REPO=true
    CHE_HOST_DEVELOPMENT_REPO="${REPO_MOUNT}"
    CHE_CONTAINER_DEVELOPMENT_REPO="/repo"

    # When we build eclipse/che-base, we insert the version of the repo it was built from into the image
    if [[ -f "/repo/dockerfiles/base/scripts/base/startup_01_init.sh" ]]; then
      CHE_REPO_BASE_VERSION=$(grep -hn CHE_BASE_API_VERSION= /repo/dockerfiles/base/scripts/base/startup_01_init.sh)
      CHE_REPO_BASE_VERSION=${CHE_REPO_BASE_VERSION#*=}
    else 
      CHE_REPO_BASE_VERSION=$CHE_BASE_API_VERSION
    fi

    if [[ $CHE_BASE_API_VERSION != $CHE_REPO_BASE_VERSION ]]; then
      warning "The CLI base image version ($CHE_BASE_API_VERSION) does not match your repo ($CHE_REPO_BASE_VERSION)"
      warning "You have mounted :/repo and your repo branch does not match with the image."
    fi

    CHE_ASSEMBLY="${CHE_HOST_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat"

    if [[ ! -d "${CHE_CONTAINER_DEVELOPMENT_REPO}"  ]] || [[ ! -d "${CHE_CONTAINER_DEVELOPMENT_REPO}/assembly" ]]; then
      info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
      info ""
      info "You volume mounted ':/repo', but we did not detect a valid ${CHE_FORMAL_PRODUCT_NAME} source repo."
      info ""
      info "Volume mounting ':/repo' activates dev mode, using assembly and CLI files from $CHE_FORMAL_PRODUCT_NAME repo."
      info ""
      info "Please check the path you mounted to verify that is a valid $CHE_FORMAL_PRODUCT_NAME git repository."
      info ""
      info "Simplest syntax:"
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

    CHE_CONTAINER_ASSEMBLY_FULL_PATH="${CHE_CONTAINER_DEVELOPMENT_REPO}"/"${CHE_ASSEMBLY_IN_REPO}"
  elif debug_server; then
    warning "Debugging activated without ':/repo' mount - using binaries inside Docker image"
  fi

  CHE_LOCAL_ASSEMBLY=false
  if [[ "${ASSEMBLY_MOUNT}" != "not set" ]]; then
    info "cli" ":/assembly mounted - using assembly from local host"

    CHE_LOCAL_ASSEMBLY=true
    CHE_ASSEMBLY="${CHE_HOST_INSTANCE}/dev/${CHE_MINI_PRODUCT_NAME}-tomcat"

    CHE_CONTAINER_ASSEMBLY="/assembly"
    if [[ ! -d "${CHE_CONTAINER_ASSEMBLY}" ]]; then
      info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
      info ""
      info "You volume mounted ':/assembly', but we could not find a valid assembly."
      info ""
      info "Please check the path you mounted."
      info ""
      info "Syntax:"
      info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
      info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
      info "                      -v <YOUR_${CHE_PRODUCT_NAME}_ASSEMBLY>:/assembly"
      info "                         ${CHE_IMAGE_FULLNAME} $*"
      return 2
    fi
    CHE_CONTAINER_ASSEMBLY_FULL_PATH="${CHE_CONTAINER_ASSEMBLY}"
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

init_scripts() {
  SCRIPTS_CONTAINER_SOURCE_DIR=""
  SCRIPTS_BASE_CONTAINER_SOURCE_DIR=""
  if local_repo && ! skip_scripts; then
     # Use the CLI that is inside the repository.
     SCRIPTS_CONTAINER_SOURCE_DIR=${CHE_SCRIPTS_CONTAINER_SOURCE_DIR}

    if [[ -d "/repo/dockerfiles/base/scripts/base" ]]; then
      SCRIPTS_BASE_CONTAINER_SOURCE_DIR="/repo/dockerfiles/base/scripts/base"
    else
      SCRIPTS_BASE_CONTAINER_SOURCE_DIR=${CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR}
    fi

    # Compare scripts inside of the Docker image with those on the repository
    # Fail if they do not match
    DIFF_NUM=$(diff -r "/scripts/base" "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}" | wc -l)
    if [ $DIFF_NUM -gt 0 ]; then 
      error "The scripts in ${CHE_IMAGE_FULLNAME} do not match those in :/repo."
      error "Is your repo branch compatible with this image version?"
      error "Add '--skip:scripts' to skip this check."
    fi
  else
     # Use the CLI that is inside the container.
     SCRIPTS_CONTAINER_SOURCE_DIR="/scripts"
     SCRIPTS_BASE_CONTAINER_SOURCE_DIR=${CHE_BASE_SCRIPTS_CONTAINER_SOURCE_DIR}
  fi
}

has_docker() {
  hash docker 2>/dev/null && return 0 || return 1
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
  #VALUE=${BINDS/\/var\/run\/docker\.sock\:\/var\/run\/docker\.sock/}

  # Remove leading and trailing spaces
  VALUE2=$(echo "${BINDS}" | xargs)

  MOUNT=""
  IFS=$' '
  for SINGLE_BIND in $VALUE2; do
    case $SINGLE_BIND in

      # Fix for CHE-3863 - in case there is :Z after the mount for SELinux, add *
      *$1*)
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

check_host_volume_mount() {
  if is_boot2docker; then
    warning "Boot2docker detected - ensure :/data is mounted to %userprofile%"
  fi

  add_file_system_test "${CHE_CONTAINER_ROOT}/test"

  if ! file_system_writable "${CHE_CONTAINER_ROOT}/test" ||
     ! file_system_executable "${CHE_CONTAINER_ROOT}/test"; then 
    error "Unable to write or execute files on your host."
    error "Have you enabled Docker to allow mounting host directories?"
    error "Did you give our CLI rights to create + exec files on your host?"
    delete_file_system_test "${CHE_CONTAINER_ROOT}/test"
    return 2;
  fi

  delete_file_system_test "${CHE_CONTAINER_ROOT}/test"
}

add_file_system_test() {
  echo '#!/bin/sh' > "${1}"
  echo 'echo hi' >> "${1}"
  chmod +x "${1}" > /dev/null
}

file_system_writable() {
  if [[ -f "${1}" ]]; then
    return 0
  else 
    return 1
  fi
}

file_system_executable() {
  EXEC_OUTPUT=$(bash "${1}")
  if [ $EXEC_OUTPUT = "hi" ]; then
    return 0
  else 
    return 1
  fi
}

delete_file_system_test() {
  rm -rf $1 > /dev/null 2>&1
}

is_boot2docker() {
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

skip_config() {
  if [ "${CHE_SKIP_CONFIG}" = "true" ]; then
    return 0
  else
    return 1
  fi
}
