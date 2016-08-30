#!/bin/sh
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
  NC='\033[0m'
}

error_exit() {
  echo  "---------------------------------------"
  error "!!!"
  error "!!! ${1}"
  error "!!!"
  echo  "---------------------------------------"
  return
}

check_docker() {
  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    error_exit "Error - Docker not installed properly: \n${output}"
  fi

  # Prep script by getting default image
  if [ "$(docker images -q alpine 2> /dev/null)" = "" ]; then
    info "ECLIPSE CHE: PULLING IMAGE alpine:latest"
    docker pull alpine > /dev/null 2>&1
  fi
}

init_global_variables() {

  # Name used in INFO statements
  DEFAULT_CHE_PRODUCT_NAME="ECLIPSE CHE"

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="che"

  DEFAULT_CHE_LAUNCHER_IMAGE_NAME="codenvy/che-launcher"
  DEFAULT_CHE_SERVER_IMAGE_NAME="codenvy/che-server"
  DEFAULT_CHE_DIR_IMAGE_NAME="codenvy/che-dir"
  DEFAULT_CHE_MOUNT_IMAGE_NAME="codenvy/che-mount"
  DEFAULT_CHE_ACTION_IMAGE_NAME="codenvy/che-action"
  DEFAULT_CHE_TEST_IMAGE_NAME="codenvy/che-test"
  DEFAULT_CHE_DEV_IMAGE_NAME="codenvy/che-dev"
  DEFAULT_CHE_SERVER_CONTAINER_NAME="che-server"
  DEFAULT_CHE_VERSION="latest"
  DEFAULT_CHE_CLI_ACTION="help"
  DEFAULT_IS_INTERACTIVE="true"
  DEFAULT_IS_PSEUDO_TTY="true"

  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}
  CHE_LAUNCHER_IMAGE_NAME=${CHE_LAUNCHER_IMAGE_NAME:-${DEFAULT_CHE_LAUNCHER_IMAGE_NAME}}
  CHE_SERVER_IMAGE_NAME=${CHE_SERVER_IMAGE_NAME:-${DEFAULT_CHE_SERVER_IMAGE_NAME}}
  CHE_DIR_IMAGE_NAME=${CHE_DIR_IMAGE_NAME:-${DEFAULT_CHE_DIR_IMAGE_NAME}}
  CHE_MOUNT_IMAGE_NAME=${CHE_MOUNT_IMAGE_NAME:-${DEFAULT_CHE_MOUNT_IMAGE_NAME}}
  CHE_ACTION_IMAGE_NAME=${CHE_ACTION_IMAGE_NAME:-${DEFAULT_CHE_ACTION_IMAGE_NAME}}
  CHE_TEST_IMAGE_NAME=${CHE_TEST_IMAGE_NAME:-${DEFAULT_CHE_TEST_IMAGE_NAME}}
  CHE_DEV_IMAGE_NAME=${CHE_DEV_IMAGE_NAME:-${DEFAULT_CHE_DEV_IMAGE_NAME}}
  CHE_SERVER_CONTAINER_NAME=${CHE_SERVER_CONTAINER_NAME:-${DEFAULT_CHE_SERVER_CONTAINER_NAME}}
  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}
  IS_INTERACTIVE=${IS_INTERACTIVE:-${DEFAULT_IS_INTERACTIVE}}
  IS_PSEUDO_TTY=${IS_PSEUDO_TTY:-${DEFAULT_IS_PSEUDO_TTY}}

  GLOBAL_NAME_MAP=$(docker info | grep "Name:" | cut -d" " -f2)
  GLOBAL_HOST_ARCH=$(docker version --format {{.Client}} | cut -d" " -f5)
  GLOBAL_UNAME=$(docker run --rm alpine sh -c "uname -r")
  GLOBAL_GET_DOCKER_HOST_IP=$(get_docker_host_ip)

  USAGE="
Usage: ${CHE_MINI_PRODUCT_NAME} [COMMAND]
           start                              Starts ${CHE_MINI_PRODUCT_NAME} server
           stop                               Stops ${CHE_MINI_PRODUCT_NAME} server
           restart                            Restart ${CHE_MINI_PRODUCT_NAME} server
           update                             Pulls specific version, respecting CHE_VERSION
           profile add <name>                 Add a profile to ~/.che/ 
           profile set <name>                 Set this profile as the default for ${CHE_MINI_PRODUCT_NAME} CLI
           profile unset                      Removes the default profile - leaves it unset
           profile rm <name>                  Remove this profile from ~/.che/
           profile update <name>              Update profile in ~/.che/
           profile info <name>                Print the profile configuration
           profile list                       List available profiles
           mount <local-path> <ws-ssh-port>   Synchronize workspace to a local directory
           dir init                           Initialize directory with ${CHE_MINI_PRODUCT_NAME} configuration
           dir up                             Create workspace from source in current directory
           dir down                           Stop workspace running in current directory
           dir status                         Display status of ${CHE_MINI_PRODUCT_NAME} in current directory
           action <action-name> [--help]      Start action on ${CHE_MINI_PRODUCT_NAME} instance
           compile <mvn-command>              SDK - Builds Che source code or modules
           test <test-name> [--help]          Start test on ${CHE_MINI_PRODUCT_NAME} instance
           info [ --all                       Run all debugging tests
                  --server                    Run ${CHE_MINI_PRODUCT_NAME} launcher and server debugging tests
                  --networking                Test connectivity between ${CHE_MINI_PRODUCT_NAME} sub-systems
                  --cli                       Print CLI (this program) debugging info
                  --create [<url>]            Test creating a workspace and project in ${CHE_MINI_PRODUCT_NAME}
                           [<user>] 
                           [<pass>] ]
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

parse_command_line () {
  if [ $# -eq 0 ]; then 
    CHE_CLI_ACTION="help"
  else
    case $1 in
      start|stop|restart|update|info|profile|action|dir|mount|compile|test|help|-h|--help)
        CHE_CLI_ACTION=$1
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  fi
}

docker_exec() {
  if has_docker_for_windows_client; then
    MSYS_NO_PATHCONV=1 docker.exe "$@"
  else
    "$(which docker)" "$@"
  fi
}

docker_run() {
  docker_exec run --rm -v /var/run/docker.sock:/var/run/docker.sock "$@"
}

docker_run_with_env_file() {
  if has_che_env_variables; then
    get_list_of_che_system_environment_variables
    docker_run --env-file=tmp "$@"
    rm -rf $PWD/tmp > /dev/null
  else
    docker_run "$@"
  fi
}

docker_run_with_pseudo_tty() {
  if has_pseudo_tty; then
    docker_run_with_env_file -t "$@"
  else
    docker_run_with_env_file "$@"
  fi
}

docker_run_with_interactive() {
  if has_interactive; then
    docker_run_with_pseudo_tty -i "$@"
  else
    docker_run_with_pseudo_tty "$@"
  fi
}

docker_run_with_che_properties() {
  if [ ! -z ${CHE_CONF_FOLDER+x} ]; then

    # Configuration directory set by user - this has precedence.
    docker_run_with_interactive -e "CHE_CONF_FOLDER=${CHE_CONF_FOLDER}" "$@"
  else 
    if has_che_properties; then
      # No user configuration directory, but CHE_PROPERTY_ values set
      generate_temporary_che_properties_file
      docker_run_with_interactive -e "CHE_CONF_FOLDER=$(get_mount_path ~/.che/conf)" "$@"
      rm -rf ~/.che/conf/che.properties > /dev/null
    else
      docker_run_with_interactive "$@"
    fi
  fi
}

has_interactive() {
  if [ "${IS_INTERACTIVE}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

has_pseudo_tty() {
  if [ "${IS_PSEUDO_TTY}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

get_docker_host_ip() {
  case $(get_docker_install_type) in
   boot2docker)
     NETWORK_IF="eth1"
   ;;
   native)
     NETWORK_IF="docker0"
   ;;
   *)
     NETWORK_IF="eth0"
   ;;
  esac
  
  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
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

is_boot2docker() {
  if echo "$GLOBAL_UNAME" | grep -q "boot2docker"; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  if is_moby_vm && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  if is_moby_vm && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}

is_moby_vm() {
  if echo "$GLOBAL_NAME_MAP" | grep -q "moby"; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_client(){
  if [ "${GLOBAL_HOST_ARCH}" = "windows" ]; then
    return 0
  else
    return 1
  fi
}

get_full_path() {
  # "/some/path" => /some/path
  OUTPUT_PATH=${1//\"}

  # create full directory path
  echo "$(cd "$(dirname "${OUTPUT_PATH}")"; pwd)/$(basename "$1")"
}

convert_windows_to_posix() {
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
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

get_mount_path() {
  FULL_PATH=$(get_full_path "${1}")

  POSIX_PATH=$(convert_windows_to_posix "${FULL_PATH}")

  CLEAN_PATH=$(get_clean_path "${POSIX_PATH}")
  echo $CLEAN_PATH
}

has_docker_for_windows_ip() {
  if [ "${GLOBAL_GET_DOCKER_HOST_IP}" = "10.0.75.2" ]; then
    return 0
  else
    return 1
  fi
}

get_che_hostname() {
  INSTALL_TYPE=$(get_docker_install_type)
  if [ "${INSTALL_TYPE}" = "boot2docker" ]; then
    echo $GLOBAL_GET_DOCKER_HOST_IP
  else
    echo "localhost"
  fi
}

has_che_env_variables() {
  PROPERTIES=$(env | grep CHE_)

  if [ "$PROPERTIES" = "" ]; then
    return 1
  else 
    return 0
  fi 
}

get_list_of_che_system_environment_variables() {
  # See: http://stackoverflow.com/questions/4128235/what-is-the-exact-meaning-of-ifs-n
  IFS=$'\n'
  DOCKER_ENV=$(get_mount_path $PWD)/tmp
  touch $DOCKER_ENV
  
  if has_default_profile; then
    cat ~/.che/profiles/${CHE_PROFILE} >> $DOCKER_ENV
  else

    # Grab these values to send to other utilities - they need to know the values  
    echo "CHE_SERVER_CONTAINER_NAME=${CHE_SERVER_CONTAINER_NAME}" >> $DOCKER_ENV
    echo "CHE_SERVER_IMAGE_NAME=${CHE_SERVER_IMAGE_NAME}" >> $DOCKER_ENV
    echo "CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME}" >> $DOCKER_ENV
    echo "CHE_MINI_PRODUCT_NAME=${CHE_PRODUCT_NAME}" >> $DOCKER_ENV

    CHE_VARIABLES=$(env | grep CHE_)

    if [ ! -z ${CHE_VARIABLES+x} ]; then
      env | grep CHE_ >> $DOCKER_ENV
    fi

    # Add in known proxy variables
    if [ ! -z ${http_proxy+x} ]; then
      echo "http_proxy=${http_proxy}" >> $DOCKER_ENV
    fi

    if [ ! -z ${https_proxy+x} ]; then
      echo "https_proxy=${https_proxy}" >> $DOCKER_ENV
    fi

    if [ ! -z ${no_proxy+x} ]; then
      echo "no_proxy=${no_proxy}" >> $DOCKER_ENV
    fi
  fi
}

check_current_image_and_update_if_not_found() {

  CURRENT_IMAGE=$(docker images -q "$1":"${CHE_VERSION}")

  if [ "${CURRENT_IMAGE}" == "" ]; then
    update_che_image $1
  fi
}

has_che_properties() {
  PROPERTIES=$(env | grep CHE_PROPERTY_)

  if [ "$PROPERTIES" = "" ]; then
    return 1
  else 
    return 0
  fi 
}

generate_temporary_che_properties_file() {
  if has_che_properties; then
    test -d ~/.che/conf || mkdir -p ~/.che/conf
    touch ~/.che/conf/che.properties

    # Get list of properties
    PROPERTIES_ARRAY=($(env | grep CHE_PROPERTY_))
    for PROPERTY in "${PROPERTIES_ARRAY[@]}"
    do
      # CHE_PROPERTY_NAME=value ==> NAME=value
      PROPERTY_WITHOUT_PREFIX=${PROPERTY#CHE_PROPERTY_}

      # NAME=value ==> separate name / value into different variables
      PROPERTY_NAME=$(echo $PROPERTY_WITHOUT_PREFIX | cut -f1 -d=)
      PROPERTY_VALUE=$(echo $PROPERTY_WITHOUT_PREFIX | cut -f2 -d=)
     
      # Replace "_" in names to periods

      # Replace "_" in names to periods
      CONVERTED_PROPERTY_NAME=$(echo "$PROPERTY_NAME" | tr _ .)

      # Replace ".." in names to "_"
      SUPER_CONVERTED_PROPERTY_NAME="${CONVERTED_PROPERTY_NAME//../_}"

      echo "$SUPER_CONVERTED_PROPERTY_NAME=$PROPERTY_VALUE" >> ~/.che/conf/che.properties
    done
  fi
}

###########################################################################
### END HELPER FUNCTIONS
###
### START CLI COMMANDS
###########################################################################

execute_che_launcher() {
  check_current_image_and_update_if_not_found ${CHE_LAUNCHER_IMAGE_NAME}
  docker_run_with_che_properties "${CHE_LAUNCHER_IMAGE_NAME}":"${CHE_VERSION}" "${CHE_CLI_ACTION}" || true
}

execute_profile(){

  if [ ! $# -ge 2 ]; then 
    error ""
    error "${CHE_MINI_PRODUCT_NAME} profile: Wrong number of arguments."
    error ""
    return
  fi

  case ${2} in
    add|rm|set|info|update)
    if [ ! $# -eq 3 ]; then 
      error ""
      error "${CHE_MINI_PRODUCT_NAME} profile: Wrong number of arguments."
      error ""
      return
    fi
    ;;
    unset|list)
    if [ ! $# -eq 2 ]; then 
      error ""
      error "${CHE_MINI_PRODUCT_NAME} profile: Wrong number of arguments."
      error ""
      return
    fi
    ;;
  esac

  case ${2} in
    add)
      if [ -f ~/.che/profiles/"${3}" ]; then
        error ""
        error "Profile ~/.che/profiles/${3} already exists. Nothing to do. Exiting."
        error ""
        return
      fi

      test -d ~/.che/profiles || mkdir -p ~/.che/profiles
      touch ~/.che/profiles/"${3}"

      echo "CHE_PRODUCT_NAME=$CHE_PRODUCT_NAME" > ~/.che/profiles/"${3}"
      echo "CHE_MINI_PRODUCT_NAME=$CHE_MINI_PRODUCT_NAME" > ~/.che/profiles/"${3}"
      echo "CHE_LAUNCHER_IMAGE_NAME=$CHE_LAUNCHER_IMAGE_NAME" > ~/.che/profiles/"${3}"
      echo "CHE_SERVER_IMAGE_NAME=$CHE_SERVER_IMAGE_NAME" >> ~/.che/profiles/"${3}"
      echo "CHE_DIR_IMAGE_NAME=$CHE_DIR_IMAGE_NAME" >> ~/.che/profiles/"${3}"
      echo "CHE_MOUNT_IMAGE_NAME=$CHE_MOUNT_IMAGE_NAME" >> ~/.che/profiles/"${3}"
      echo "CHE_TEST_IMAGE_NAME=$CHE_TEST_IMAGE_NAME" >> ~/.che/profiles/"${3}"
      echo "CHE_SERVER_CONTAINER_NAME=$CHE_SERVER_CONTAINER_NAME" >> ~/.che/profiles/"${3}"

      # Add all other variables to the profile
      env | grep CHE_ >> ~/.che/profiles/"${3}" || true

      # Remove duplicates, if any
      cat ~/.che/profiles/"${3}" | sort | uniq > ~/.che/profiles/tmp
      mv -f ~/.che/profiles/tmp ~/.che/profiles/"${3}"


      info ""
      info "Added new ${CHE_MINI_PRODUCT_NAME} CLI profile ~/.che/profiles/${3}."
      info ""
    ;;
    update)
      if [ ! -f ~/.che/profiles/"${3}" ]; then
        error ""
        error "Profile ~/.che/profiles/${3} does not exist. Nothing to update. Exiting."
        error ""
        return
      fi

      execute_profile profile rm "${3}"
      execute_profile profile add "${3}"
    ;;
    rm)
      if [ ! -f ~/.che/profiles/"${3}" ]; then
        error ""
        error "Profile ~/.che/profiles/${3} does not exist. Nothing to do. Exiting."
        error ""
        return
      fi

      rm ~/.che/profiles/"${3}" > /dev/null

      info ""
      info "Removed ${CHE_MINI_PRODUCT_NAME} CLI profile ~/.che/profiles/${3}."
      info ""
    ;;
    info)
      if [ ! -f ~/.che/profiles/"${3}" ]; then
        error ""
        error "Profile ~/.che/profiles/${3} does not exist. Nothing to do. Exiting."
        error ""
        return
      fi
 

      debug "---------------------------------------"
      debug "---------   CLI PROFILE INFO   --------"
      debug "---------------------------------------"
      debug ""
      debug "Profile ~/.che/profiles/${3} contains:"
      while IFS= read line
      do
        # display $line or do somthing with $line
        debug "$line"
      done <~/.che/profiles/"${3}"
    ;;
    set)
      if [ ! -f ~/.che/profiles/"${3}" ]; then
        error ""
        error "Profile ~/.che/${3} does not exist. Nothing to do. Exiting."
        error ""
        return
      fi
      
      echo "CHE_PROFILE=${3}" > ~/.che/profiles/.profile

      info ""
      info "Set active ${CHE_MINI_PRODUCT_NAME} CLI profile to ~/.che/profiles/${3}."
      info ""
    ;;
    unset)
      if [ ! -f ~/.che/profiles/.profile ]; then
        error ""
        error "Default profile not set. Nothing to do. Exiting."
        error ""
        return
      fi
      
      rm -rf ~/.che/profiles/.profile

      info ""
      info "Unset the default ${CHE_MINI_PRODUCT_NAME} CLI profile. No profile currently set."
      info ""
    ;;
    list)
      if [ -d ~/.che ]; then
        info "Available ${CHE_MINI_PRODUCT_NAME} CLI profiles:"
        ls ~/.che/profiles
      else
        info "No ${CHE_MINI_PRODUCT_NAME} CLI profiles currently set."
      fi

      if has_default_profile; then
        info "Default profile set to:"
        get_default_profile
      else
        info "Default profile currently unset."
      fi
    ;;
  esac
}

has_default_profile() {
  if [ -f ~/.che/profiles/.profile ]; then
    return 0
  else 
    return 1
  fi 
}

get_default_profile() {
  if [ has_default_profile ]; then
    source ~/.che/profiles/.profile
    echo "${CHE_PROFILE}"
  else
    echo ""
  fi
}

load_profile() {
  if has_default_profile; then

    source ~/.che/profiles/.profile

    if [ ! -f ~/.che/profiles/"${CHE_PROFILE}" ]; then
      error ""
      error "${CHE_MINI_PRODUCT_NAME} CLI profile set in ~/.che/profiles/.profile to '${CHE_PROFILE}' but ~/.che/profiles/${CHE_PROFILE} does not exist."
      error ""
      return
    fi

    source ~/.che//profiles/"${CHE_PROFILE}"
  fi
}

execute_che_dir() {
  check_current_image_and_update_if_not_found ${CHE_DIR_IMAGE_NAME}
  CURRENT_DIRECTORY=$(get_mount_path "${PWD}")
  docker_run_with_che_properties -v "$CURRENT_DIRECTORY":"$CURRENT_DIRECTORY" "${CHE_DIR_IMAGE_NAME}":"${CHE_VERSION}" "${CURRENT_DIRECTORY}" "$@"
}

execute_che_action() {
  check_current_image_and_update_if_not_found ${CHE_ACTION_IMAGE_NAME}
  docker_run_with_che_properties "${CHE_ACTION_IMAGE_NAME}":"${CHE_VERSION}" "$@"
}

update_che_image() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  info "${CHE_PRODUCT_NAME}: Pulling image $1:${CHE_VERSION}"
  docker pull $1:${CHE_VERSION}
  echo ""
}

execute_che_mount() {
  if [ ! $# -eq 3 ]; then 
    error "${CHE_MINI_PRODUCT_NAME} mount: Wrong number of arguments provided."
    return
  fi

  MOUNT_PATH=$(get_mount_path "${2}")

  if [ ! -e "${MOUNT_PATH}" ]; then
    error "${CHE_MINI_PRODUCT_NAME} mount: Path provided does not exist."
    return
  fi

  if [ ! -d "${MOUNT_PATH}" ]; then
    error "${CHE_MINI_PRODUCT_NAME} mount: Path provided is not a valid directory."
    return
  fi

  docker_run_with_che_properties --cap-add SYS_ADMIN \
              --device /dev/fuse \
              -v "${MOUNT_PATH}":/mnthost \
              "${CHE_MOUNT_IMAGE_NAME}":"${CHE_VERSION}" "${GLOBAL_GET_DOCKER_HOST_IP}" $3
}

execute_che_compile() {
  if [ $# -eq 0 ]; then 
    error "${CHE_MINI_PRODUCT_NAME} compile: Missing argument - pass compilation command as paramters."
    return
  fi

  check_current_image_and_update_if_not_found ${CHE_DEV_IMAGE_NAME}
  CURRENT_DIRECTORY=$(get_mount_path "${PWD}")
  docker_run_with_che_properties -v "$CURRENT_DIRECTORY":/home/user/che-build \
                                 -v "$(get_mount_path ~/.m2):/home/user/.m2" \
                                 -w /home/user/che-build \
                                 "${CHE_DEV_IMAGE_NAME}":"${CHE_VERSION}" "$@"
}

execute_che_test() {
  check_current_image_and_update_if_not_found ${CHE_TEST_IMAGE_NAME}
  docker_run_with_che_properties "${CHE_TEST_IMAGE_NAME}":"${CHE_VERSION}" "$@"
}

execute_che_info() {
  if [ $# -eq 1 ]; then
    TESTS="--server"
  else
    TESTS=$2
  fi
  
  case $TESTS in
    --all|-all)
      print_che_cli_debug
      execute_che_launcher
      run_connectivity_tests
      execute_che_test post-flight-check "$@"
    ;;
    --cli|-cli)
      print_che_cli_debug
    ;;
    --networking|-networking)
      run_connectivity_tests
    ;;
    --server|-server)
      print_che_cli_debug
      execute_che_launcher
    ;;
    --create|-create)
      execute_che_test "$@"
    ;;
    *)
      debug "Unknown debug flag passed: $2. Exiting."
    ;;
  esac
}

print_che_cli_debug() {
  debug "---------------------------------------"
  debug "---------    CLI DEBUG INFO    --------"
  debug "---------------------------------------"
  debug ""
  debug "---------  PLATFORM INFO  -------------"
  debug "CLI DEFAULT PROFILE       = $(has_default_profile && echo $(get_default_profile) || echo "not set")"
  debug "DOCKER_INSTALL_TYPE       = $(get_docker_install_type)"
  debug "DOCKER_HOST_IP            = ${GLOBAL_GET_DOCKER_HOST_IP}"
  debug "IS_DOCKER_FOR_WINDOWS     = $(is_docker_for_windows && echo "YES" || echo "NO")"
  debug "IS_DOCKER_FOR_MAC         = $(is_docker_for_mac && echo "YES" || echo "NO")"
  debug "IS_BOOT2DOCKER            = $(is_boot2docker && echo "YES" || echo "NO")"
  debug "IS_NATIVE                 = $(is_native && echo "YES" || echo "NO")"
  debug "IS_WINDOWS                = $(has_docker_for_windows_client && echo "YES" || echo "NO")"
  debug "HAS_DOCKER_FOR_WINDOWS_IP = $(has_docker_for_windows_ip && echo "YES" || echo "NO")"
  debug "IS_MOBY_VM                = $(is_moby_vm && echo "YES" || echo "NO")"
  debug "HAS_CHE_ENV_VARIABLES     = $(has_che_env_variables && echo "YES" || echo "NO")"
  debug "HAS_TEMP_CHE_PROPERTIES   = $(has_che_properties && echo "YES" || echo "NO")"
  debug "HAS_INTERACTIVE           = $(has_interactive && echo "YES" || echo "NO")"
  debug "HAS_PSEUDO_TTY            = $(has_pseudo_tty && echo "YES" || echo "NO")"
  debug ""
}

run_connectivity_tests() {
  debug ""
  debug "---------------------------------------"
  debug "--------   CONNECTIVITY TEST   --------"
  debug "---------------------------------------"
  # Start a fake workspace agent
  docker_exec run -d -p 12345:80 --name fakeagent alpine httpd -f -p 80 -h /etc/ > /dev/null

  AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
  AGENT_INTERNAL_PORT=80
  AGENT_EXTERNAL_IP=$GLOBAL_GET_DOCKER_HOST_IP
  AGENT_EXTERNAL_PORT=12345


  ### TEST 1: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I $(get_che_hostname):${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o /dev/null --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" > /dev/null

  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Browser             => Workspace Agent (Hostname)   : Connection succeeded"
  else
      debug "Browser             => Workspace Agent (Hostname)   : Connection failed"
  fi

  ### TEST 1a: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o /dev/null --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" > /dev/null

  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Browser             => Workspace Agent (External IP): Connection succeeded"
  else
      debug "Browser             => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 2: Simulate Che server ==> workspace agent (external IP) connectivity 
  export HTTP_CODE=$(docker run --rm --name fakeserver \
                                --entrypoint=curl \
                                codenvy/che-server:${DEFAULT_CHE_VERSION} \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o /dev/null \
                                  --write-out "%{http_code}")
  
  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Che Server          => Workspace Agent (External IP): Connection succeeded"
  else
      debug "Che Server          => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 3: Simulate Che server ==> workspace agent (internal IP) connectivity 
  export HTTP_CODE=$(docker run --rm --name fakeserver \
                                --entrypoint=curl \
                                codenvy/che-server:${DEFAULT_CHE_VERSION} \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o /dev/null \
                                  --write-out "%{http_code}")

  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Che Server          => Workspace Agent (Internal IP): Connection succeeded"
  else
      debug "Che Server          => Workspace Agent (Internal IP): Connection failed"
  fi

  docker rm -f fakeagent > /dev/null
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u
init_logging
check_docker
init_global_variables
parse_command_line "$@"

if is_boot2docker; then
  debug ""
  debug "!!! Boot2docker detected - save workspaces only in %userprofile% !!!"
  debug ""
fi

case ${CHE_CLI_ACTION} in
  start|stop|restart)
    load_profile
    execute_che_launcher
  ;;
  profile)
    execute_profile "$@"
  ;;
  dir)
    # remove "dir" arg by shifting it
    shift
    load_profile
    execute_che_dir "$@"
  ;;
  action)
    # remove "action" arg by shifting it
    shift
    load_profile
    execute_che_action "$@"
  ;;
  update)
    load_profile
    update_che_image ${CHE_LAUNCHER_IMAGE_NAME}
    update_che_image ${CHE_MOUNT_IMAGE_NAME}
    update_che_image ${CHE_DIR_IMAGE_NAME}
    update_che_image ${CHE_ACTION_IMAGE_NAME}
    update_che_image ${CHE_TEST_IMAGE_NAME}
    update_che_image ${CHE_DEV_IMAGE_NAME}

    # Delegate updating che-server to the launcher
    execute_che_launcher
  ;;
  mount)
    load_profile
    execute_che_mount "$@"
  ;;
  compile)
    # remove "compile" arg by shifting it
    shift
    load_profile
    execute_che_compile "$@"
  ;;
  test)
    # remove "test" arg by shifting it
    shift
    load_profile
    execute_che_test "$@"
  ;;
  info)
    load_profile
    execute_che_info "$@"
  ;;
  help)
    usage
  ;;
esac
