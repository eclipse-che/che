#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html


cli_execute() {
  COMMAND="cmd_$1"

  # Need to load all files in advance so commands can invoke other commands.
  for COMMAND_FILE in "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_*.sh
  do
    source "${COMMAND_FILE}"
  done

  shift
  eval $COMMAND "$@"
}

cli_parse () {
  debug $FUNCNAME
  COMMAND="cmd_$1"

  case $1 in
      init|config|start|stop|restart|backup|restore|info|offline|destroy|download|rmi|upgrade|version|ssh|sync|action|test|compile|help)
      ;;
      *)
         error "You passed an unknown command."
         usage
         return 2
      ;;
  esac
}


get_boot_url() {
  echo "$CHE_HOST:$CHE_PORT/api/"
}

get_display_url() {
  if ! is_docker_for_mac; then
    echo "http://${CHE_HOST}:${CHE_PORT}"
  else
    echo "http://localhost:${CHE_PORT}"
  fi
}

check_if_booted() {
  CURRENT_CHE_SERVER_CONTAINER_ID=$(get_server_container_id $CHE_SERVER_CONTAINER_NAME)
  wait_until_container_is_running 20 ${CURRENT_CHE_SERVER_CONTAINER_ID}
  if ! container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for ${CHE_MINI_PRODUCT_NAME} container to start."
    return 2
  fi

  info "start" "Services booting..."
  info "start" "Server logs at \"docker logs -f ${CHE_SERVER_CONTAINER_NAME}\""
  wait_until_server_is_booted 60 ${CURRENT_CHE_SERVER_CONTAINER_ID}

  DISPLAY_URL=$(get_display_url)

  if server_is_booted ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
    info "start" "Booted and reachable"
    info "start" "Ver: $(get_installed_version)"
    info "start" "Use: ${DISPLAY_URL}"
    info "start" "API: ${DISPLAY_URL}/swagger"
  else
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CHE_SERVER_CONTAINER_NAME}\" to inspect the issue."
    return 2
  fi
}

server_is_booted() {
  PING_URL=$(get_boot_url)
  HTTP_STATUS_CODE=$(curl -I -k ${PING_URL} -s -o /dev/null --write-out "%{http_code}")
  log "${HTTP_STATUS_CODE}"
  if [[ "${HTTP_STATUS_CODE}" = "200" ]] || [[ "${HTTP_STATUS_CODE}" = "302" ]]; then
    return 0
  else
    return 1
  fi
}

initiate_offline_or_network_mode(){
  # If you are using ${CHE_FORMAL_PRODUCT_NAME} in offline mode, images must be loaded here
  # This is the point where we know that docker is working, but before we run any utilities
  # that require docker.
  if [[ "$@" == *"--offline"* ]]; then
    info "init" "Importing ${CHE_MINI_PRODUCT_NAME} Docker images from tars..."

    if [ ! -d ${CHE_CONTAINER_OFFLINE_FOLDER} ]; then
      info "init" "You requested offline image loading, but '${CHE_CONTAINER_OFFLINE_FOLDER}' folder not found"
      return 2;
    fi

    IFS=$'\n'
    for file in "${CHE_CONTAINER_OFFLINE_FOLDER}"/*.tar
    do
      if ! $(docker load < "${CHE_CONTAINER_OFFLINE_FOLDER}"/"${file##*/}" > /dev/null); then
        error "Failed to restore ${CHE_MINI_PRODUCT_NAME} Docker images"
        return 2;
      fi
      info "init" "Loading ${file##*/}..."
    done
  else
    # If we are here, then we want to run in networking mode.
    # If we are in networking mode, we have had some issues where users have failed DNS networking.
    # See: https://github.com/eclipse/che/issues/3266#issuecomment-265464165
    local HTTP_STATUS_CODE=$(curl -I -k dockerhub.com -s -o /dev/null --write-out "%{http_code}")
    if [[ ! $HTTP_STATUS_CODE -eq "301" ]]; then
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
      info "     a. Try 'curl --head dockerhub.com'"
      return 2;
    fi
  fi
}

grab_initial_images() {
  # Prep script by getting default image
  if [ "$(docker images -q alpine:3.4 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image alpine:3.4"
    log "docker pull alpine:3.4 >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull alpine:3.4 >> "${LOGS}" > /dev/null 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image alpine:3.4 unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi

  if [ "$(docker images -q eclipse/che-ip:nightly 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image eclipse/che-ip:nightly"
    log "docker pull eclipse/che-ip:nightly >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull eclipse/che-ip:nightly >> "${LOGS}" > /dev/null 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image eclipse/che-ip:nightly unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi
}

has_env_variables() {
  debug $FUNCNAME
  PROPERTIES=$(env | grep "${CHE_PRODUCT_NAME}_")

  if [ "$PROPERTIES" = "" ]; then
    return 1
  else
    return 0
  fi
}

update_image_if_not_found() {
  debug $FUNCNAME

  text "${GREEN}INFO:${NC} (${CHE_MINI_PRODUCT_NAME} download): Checking for image '$1'..."
  CURRENT_IMAGE=$(docker images -q "$1")
  if [ "${CURRENT_IMAGE}" == "" ]; then
    text "not found\n"
    update_image $1
  else
    text "found\n"
  fi
}

update_image() {
  debug $FUNCNAME

  if [ "${1}" == "--force" ]; then
    shift
    info "download" "Removing image $1"
    log "docker rmi -f $1 >> \"${LOGS}\""
    docker rmi -f $1 >> "${LOGS}" 2>&1 || true
  fi

  if [ "${1}" == "--pull" ]; then
    shift
  fi

  info "download" "Pulling image $1"
  text "\n"
  log "docker pull $1 >> \"${LOGS}\" 2>&1"
  TEST=""
  docker pull $1 || TEST=$?
  if [ "$TEST" = "1" ]; then
    error "Image $1 unavailable. Not on dockerhub or built locally."
    return 2;
  fi
  text "\n"
}

is_initialized() {
  debug $FUNCNAME
  if [[ -d "${CHE_CONTAINER_INSTANCE}" ]] && \
     [[ -f "${CHE_CONTAINER_INSTANCE}"/$CHE_VERSION_FILE ]] && \
     [[ -f "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" ]]; then
    return 0
  else
    return 1
  fi
}

is_configured() {
  debug $FUNCNAME
  if [[ -d "${CHE_CONTAINER_CONFIG_MANIFESTS_FOLDER}" ]] && \
     [[ -d "${CHE_CONTAINER_CONFIG_MODULES_FOLDER}" ]]; then
    return 0
  else
    return 1
  fi
}

has_version_registry() {
  if [ -d /version/$1 ]; then
    return 0;
  else
    return 1;
  fi
}

list_versions(){
  # List all subdirectories and then print only the file name
  for version in /version/* ; do
    text " ${version##*/}\n"
  done
}

version_error(){
  text "\nWe could not find version '$1'. Available versions:\n"
  list_versions
  text "\nSet CHE_VERSION=<version> and rerun.\n\n"
}

### Returns the list of ${CHE_FORMAL_PRODUCT_NAME} images for a particular version of ${CHE_FORMAL_PRODUCT_NAME}
### Sets the images as environment variables after loading from file
get_image_manifest() {
  info "cli" "Checking registry for version '$1' images"
  if ! has_version_registry $1; then
    version_error $1
    return 1;
  fi

  IMAGE_LIST=$(cat /version/$1/images)
  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    log "eval $SINGLE_IMAGE"
    eval $SINGLE_IMAGE
  done
}

get_installed_version() {
  if ! is_initialized; then
    echo "<not-configed>"
  else
    cat "${CHE_CONTAINER_INSTANCE}"/$CHE_VERSION_FILE
  fi
}

get_image_version() {
  echo "$CHE_IMAGE_VERSION"
}

less_than() {
  for (( i=0; i<${#1}; i++ )); do
    if [[ ${1:$i:1} != ${2:$i:1} ]]; then
      if [ ${1:$i:1} -lt ${2:$i:1} ]; then
        return 0
      else
        return 1
      fi
    fi
  done
  return 1
}

compare_cli_version_to_installed_version() {
  IMAGE_VERSION=$(get_image_version)
  INSTALLED_VERSION=$(get_installed_version)

  if [[ "$INSTALLED_VERSION" = "$IMAGE_VERSION" ]]; then
    echo "match"
  elif [ "$INSTALLED_VERSION" = "nightly" ] ||
       [ "$IMAGE_VERSION" = "nightly" ]; then
    echo "nightly"
  elif less_than $INSTALLED_VERSION $IMAGE_VERSION; then
    echo "install-less-cli"
  else
    echo "cli-less-install"
  fi
}

verify_version_compatibility() {

  ## If ! is_initialized, then the system hasn't been installed
  ## First, compare the CLI image version to what version was initialized in /config/*.ver.donotmodify
  ##      - If they match, good
  ##      - If they don't match and one is nightly, fail
  ##      - If they don't match, then if CLI is older fail with message to get proper CLI
  ##      - If they don't match, then if CLLI is newer fail with message to run upgrade first

  CHE_IMAGE_VERSION=$(get_image_version)

  if is_initialized; then
    COMPARE_CLI_ENV=$(compare_cli_version_to_installed_version)
    INSTALLED_VERSION=$(get_installed_version)
    case "${COMPARE_CLI_ENV}" in
      "match")
      ;;
      "nightly")
        error ""
        error "Your CLI version '${CHE_IMAGE_FULLNAME}' does not match your installed version '$INSTALLED_VERSION'."
        error ""
        error "The 'nightly' CLI is only compatible with 'nightly' installed versions."
        error "You may not '${CHE_MINI_PRODUCT_NAME} upgrade' from 'nightly' to a numbered (tagged) version."
        error ""
        error "Run the CLI as '${CHE_IMAGE_NAME}:<version>' to install a tagged version."
        return 2
      ;;
      "install-less-cli")
        error ""
        error "Your CLI version '${CHE_IMAGE_FULLNAME}' is newer than your installed version '$INSTALLED_VERSION'."
        error ""
        error "Run '${CHE_IMAGE_FULLNAME} upgrade' to migrate your installation to '$CHE_IMAGE_VERSION'."
        error "Or, run the CLI with '${CHE_IMAGE_NAME}:$INSTALLED_VERSION' to match the CLI with your installed version."
        return 2
      ;;
      "cli-less-install")
        error ""
        error "Your CLI version '${CHE_IMAGE_FULLNAME}' is older than your installed version '$INSTALLED_VERSION'."
        error ""
        error "You cannot use an older CLI with a newer installation."
        error ""
        error "Run the CLI with '${CHE_IMAGE_NAME}:$INSTALLED_VERSION' to match the CLI with your existing installed version."
        return 2
      ;;
    esac
  fi

  # Per request of the engineers, check to see if the locally cached nightly version is older
  # than the one stored on DockerHub.
  if [[ "${CHE_IMAGE_VERSION}" = "nightly" ]]; then

    REMOTE_NIGHTLY_JSON=$(curl -s https://hub.docker.com/v2/repositories/${CHE_IMAGE_NAME}/tags/nightly/)

    # Retrieve info on current nightly
    LOCAL_CREATION_DATE=$(docker inspect --format="{{.Created }}" ${CHE_IMAGE_FULLNAME})
    REMOTE_CREATION_DATE=$(echo $REMOTE_NIGHTLY_JSON | jq ".last_updated")
    REMOTE_CREATION_DATE="${REMOTE_CREATION_DATE//\"}"

    # Unfortunatley, the "last_updated" date on DockerHub is the date it was uploaded, not created.
    # So after you download the image locally, then the local image "created" value reflects when it
    # was originally built, creating a istuation where the local cached version is always older than
    # what is on DockerHub, even if you just pulled it.
    # Solution is to compare the dates, and only print warning message if the locally created ate
    # is less than the updated date on dockerhub.

    if [[ -z ${REMOTE_CREATION_DATE} ]]; then
      warning "Unable to get published date on hub.docker.com for ${CHE_IMAGE_FULLNAME}"
    elif $(newer_date_period ${LOCAL_CREATION_DATE} ${REMOTE_CREATION_DATE}); then
      warning "There is a newer ${CHE_IMAGE_FULLNAME} image on DockerHub."
    fi
  fi
}

# Convert a ISO 8601 date to timestamp
timestamp_date_iso8601() {
  local TMP_DATE=$(date -d "${1}" +%s)
  echo ${TMP_DATE}
}

# Compare two dates with ISO 8601 format and return
# true if the first date is less than the second date (with a 1hour period)
# else false
newer_date_period() {
  local FIRST_DATE=$(timestamp_date_iso8601 $1)
  local SECOND_DATE=$(timestamp_date_iso8601 $2)

  if [[ $(expr ${FIRST_DATE} + 3600) -lt ${SECOND_DATE} ]]; then
    return 0
  else
    return 1
  fi
}


verify_version_upgrade_compatibility() {
  ## Two levels of checks
  ## First, compare the CLI image version to what the admin has configured in /config/.env file
  ##      - If they match, nothing to upgrade
  ##      - If they don't match and one is nightly, fail upgrade is not supported for nightly
  ##      - If they don't match, then if CLI is older fail with message that we do not support downgrade
  ##      - If they don't match, then if CLI is newer then good
  CHE_IMAGE_VERSION=$(get_image_version)

  if ! is_initialized || ! is_configured; then
    info "upgrade" "$CHE_MINI_PRODUCT_NAME is not installed or configured. Nothing to upgrade."
    return 2
  fi

  if is_initialized; then
    COMPARE_CLI_ENV=$(compare_cli_version_to_installed_version)
    CONFIGURED_VERSION=$(get_installed_version)

    case "${COMPARE_CLI_ENV}" in
      "match")
        error ""
        error "Your CLI version '${CHE_IMAGE_FULLNAME}' is identical to your installed version '$INSTALLED_VERSION'."
        error ""
        error "Run '${CHE_IMAGE_NAME}:<version> upgrade' with a newer version to upgrade."
        error "View available versions with '$CHE_FORMAL_PRODUCT_NAME version'."
        return 2
      ;;
      "nightly")
        error ""
        error "Your CLI version '${CHE_IMAGE_FULLNAME}' or installed version '$INSTALLED_VERSION' is nightly."
        error ""
        error "You may not '${CHE_IMAGE_NAME} upgrade' from 'nightly' to a numbered (tagged) version."
        error "You can 'docker pull ${CHE_IMAGE_FULLNAME}' to get a newer nightly version."
        return 2
      ;;
      "install-less-cli")
      ;;
      "cli-less-install")
        error ""
        error "Your CLI version '${CHE_IMAGE_FULLNAME}' is older than your installed version '$INSTALLED_VERSION'."
        error ""
        error "You cannot use '${CHE_IMAGE_NAME} upgrade' to downgrade versions."
        error ""
        error "Run '${CHE_IMAGE_NAME}:<version> upgrade' with a newer version to upgrade."
        error "View available versions with '${CHE_IMAGE_NAME} version'."
        return 2
      ;;
    esac
  fi
}

# Usage:
#   confirm_operation <Warning message> [--force|--no-force]
confirm_operation() {
  debug $FUNCNAME

  FORCE_OPERATION=${2:-"--no-force"}

  if [ ! "${FORCE_OPERATION}" == "--quiet" ]; then
    # Warn user with passed message
    info "${1}"
    text "\n"
    read -p "      Are you sure? [N/y] " -n 1 -r
    text "\n\n"
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      return 1;
    else
      return 0;
    fi
  fi
}

port_open() {
  debug $FUNCNAME

  docker run -d -p $1:$1 --name fake alpine:3.4 httpd -f -p $1 -h /etc/ > /dev/null 2>&1
  NETSTAT_EXIT=$?
  docker rm -f fake > /dev/null 2>&1

  if [ $NETSTAT_EXIT = 125 ]; then
    return 1
  else
    return 0
  fi
}

server_is_booted_extra_check() {
 true
}

wait_until_server_is_booted() {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted ${2} || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    log "sleep 2"
    sleep 2
    server_is_booted_extra_check
    ELAPSED=$((ELAPSED+1))
  done
}
