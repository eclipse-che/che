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

launcher_dir="$(dirname "$0")"
source "$launcher_dir/launcher_funcs.sh"
source "$launcher_dir/launcher_cmds.sh"

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {

  # Set variables that use docker as utilities to avoid over container execution
  ETH0_ADDRESS=$(docker run --rm --net host alpine /bin/sh -c "ifconfig eth0 2> /dev/null" | \
                                                            grep "inet addr:" | \
                                                            cut -d: -f2 | \
                                                            cut -d" " -f1)

  ETH1_ADDRESS=$(docker run --rm --net host alpine /bin/sh -c "ifconfig eth1 2> /dev/null" | \
                                                            grep "inet addr:" | \
                                                            cut -d: -f2 | \
                                                            cut -d" " -f1) 

  DOCKER0_ADDRESS=$(docker run --rm --net host alpine /bin/sh -c "ifconfig docker0 2> /dev/null" | \
                                                              grep "inet addr:" | \
                                                              cut -d: -f2 | \
                                                              cut -d" " -f1)

  # Used to self-determine container version
  LAUNCHER_CONTAINER_ID=$(get_che_launcher_container_id)
  LAUNCHER_IMAGE_NAME=$(get_che_container_image_name ${LAUNCHER_CONTAINER_ID})
  LAUNCHER_IMAGE_VERSION=$(get_che_image_version ${LAUNCHER_IMAGE_NAME})

  # Possible Docker install types are:
  #     native, boot2docker or moby
  DOCKER_INSTALL_TYPE=$(get_docker_install_type)

  # User configurable variables
  DEFAULT_CHE_PRODUCT_NAME="ECLIPSE CHE"
  DEFAULT_CHE_MINI_PRODUCT_NAME="che"
  DEFAULT_CHE_SERVER_CONTAINER_NAME="che-server"
  DEFAULT_CHE_SERVER_IMAGE_NAME="eclipse/che-server"
  DEFAULT_DOCKER_HOST_IP=$(get_docker_host_ip)
  DEFAULT_CHE_HOSTNAME=$(get_che_hostname)
  DEFAULT_CHE_PORT="8080"
  DEFAULT_CHE_VERSION=$(get_che_launcher_version)
  DEFAULT_CHE_RESTART_POLICY="no"
  DEFAULT_CHE_USER="root"
  DEFAULT_CHE_LOG_LEVEL="info"
  DEFAULT_CHE_DEBUG_SERVER="false"
  DEFAULT_CHE_DEBUG_SERVER_PORT="8000"
  DEFAULT_CHE_DEBUG_SERVER_SUSPEND="false"
  DEFAULT_CHE_DOCKER_MACHINE_HOST_EXTERNAL=$(get_docker_external_hostname)

  # Clean eventual user provided paths
  # This is a user-provided override
  CHE_CONF=${CHE_CONF:+$(get_converted_and_clean_path "${CHE_CONF}")}
  CHE_CONF_LOCATION="${CHE_CONF}":"/conf:Z"
  # "<-- Added comment with quote to fix broken syntax highlighting

  # This is a user-provided override
  # If this is not provided by the user, we will not volume mount it and use the internal values
  CHE_ASSEMBLY=${CHE_ASSEMBLY:+$(get_converted_and_clean_path "${CHE_ASSEMBLY}")}
  CHE_ASSEMBLY_LOCATION="${CHE_ASSEMBLY}":"/assembly:Z"
  # "<-- Added comment with quote to fix broken syntax highlighting

  DEFAULT_CHE_DATA_FOLDER="/home/user/che"
  CHE_DATA=${CHE_DATA:-${DEFAULT_CHE_DATA_FOLDER}}
  CHE_DATA=$(get_converted_and_clean_path "${CHE_DATA}")
  CHE_DATA_LOCATION="${CHE_DATA}":"/data:Z"
  # "<-- Added comment with quote to fix broken syntax highlighting

  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}
  CHE_SERVER_CONTAINER_NAME=${CHE_SERVER_CONTAINER_NAME:-${DEFAULT_CHE_SERVER_CONTAINER_NAME}}
  CHE_SERVER_IMAGE_NAME=${CHE_SERVER_IMAGE_NAME:-${DEFAULT_CHE_SERVER_IMAGE_NAME}}
  CHE_HOSTNAME=${CHE_HOSTNAME:-${DEFAULT_CHE_HOSTNAME}}
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}
  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_RESTART_POLICY=${CHE_RESTART_POLICY:-${DEFAULT_CHE_RESTART_POLICY}}
  CHE_USER=${CHE_USER:-${DEFAULT_CHE_USER}}
  CHE_HOST_IP=${CHE_HOST_IP:-${DEFAULT_DOCKER_HOST_IP}}
  CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}
  CHE_DEBUG_SERVER=${CHE_DEBUG_SERVER:-${DEFAULT_CHE_DEBUG_SERVER}}
  CHE_DEBUG_SERVER_PORT=${CHE_DEBUG_SERVER_PORT:-${DEFAULT_CHE_DEBUG_SERVER_PORT}}
  CHE_DEBUG_SERVER_SUSPEND=${CHE_DEBUG_SERVER_SUSPEND:-${DEFAULT_CHE_DEBUG_SERVER_SUSPEND}}
  CHE_DOCKER_MACHINE_HOST_EXTERNAL=${CHE_DOCKER_MACHINE_HOST_EXTERNAL:-${DEFAULT_CHE_DOCKER_MACHINE_HOST_EXTERNAL}}

  USAGE="
Usage:
  docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock ${LAUNCHER_IMAGE_NAME} [COMMAND]
     start                              Starts ${CHE_MINI_PRODUCT_NAME} server
     stop [<container_id>]              Stops ${CHE_MINI_PRODUCT_NAME} server
     restart                            Restart ${CHE_MINI_PRODUCT_NAME} server
     update                             Pull latest version of ${CHE_SERVER_IMAGE_NAME}
     info                               Print some debugging information
"
}

parse_command_line () {
  if [ $# -eq 0 ]; then
    usage
    exit
  fi

  case $1 in
    start|stop|restart|update|info)
      CHE_SERVER_ACTION=$1
    ;;
    -h|--help)
      usage
      exit
    ;;
    *)
      # unknown option
      error_exit "You passed an unknown command line option."
    ;;
  esac
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

init_logging
check_docker
init_global_variables
parse_command_line "$@"

case ${CHE_SERVER_ACTION} in
  start)
    start_che_server
  ;;
  stop)
    shift
    stop_che_server $@
  ;;
  restart)
    shift
    restart_che_server $@
  ;;
  update)
    update_che_server
  ;;
  info)
    print_debug_info
  ;;
esac
