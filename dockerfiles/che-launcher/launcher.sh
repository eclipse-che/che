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

  CHE_SERVER_CONTAINER_NAME="che-server"
  CHE_SERVER_IMAGE_NAME="codenvy/che-server"
  CHE_LAUNCHER_IMAGE_NAME="codenvy/che-launcher"

  # Possible Docker install types are:
  #     native, boot2docker or moby
  DOCKER_INSTALL_TYPE=$(get_docker_install_type)

  # User configurable variables
  DEFAULT_DOCKER_HOST_IP=$(get_docker_host_ip)
  DEFAULT_CHE_HOSTNAME=$(get_che_hostname)
  DEFAULT_CHE_PORT="8080"
  DEFAULT_CHE_VERSION=$(get_che_launcher_version)
  DEFAULT_CHE_RESTART_POLICY="no"
  DEFAULT_CHE_USER="root"
  DEFAULT_CHE_LOG_LEVEL="info"
  DEFAULT_CHE_DATA_FOLDER="/home/user/che"

  # Clean eventual user provided paths
  CHE_CONF_FOLDER=${CHE_CONF_FOLDER:+$(get_clean_path ${CHE_CONF_FOLDER})}
  CHE_DATA_FOLDER=${CHE_DATA_FOLDER:+$(get_clean_path ${CHE_DATA_FOLDER})}

  CHE_HOSTNAME=${CHE_HOSTNAME:-${DEFAULT_CHE_HOSTNAME}}
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}
  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_RESTART_POLICY=${CHE_RESTART_POLICY:-${DEFAULT_CHE_RESTART_POLICY}}
  CHE_USER=${CHE_USER:-${DEFAULT_CHE_USER}}
  CHE_HOST_IP=${CHE_HOST_IP:-${DEFAULT_DOCKER_HOST_IP}}
  CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}
  CHE_DATA_FOLDER=${CHE_DATA_FOLDER:-${DEFAULT_CHE_DATA_FOLDER}}

  # CHE_CONF_ARGS are the Docker run options that need to be used if users set CHE_CONF_FOLDER:
  #   - empty if CHE_CONF_FOLDER is not set
  #   - -v ${CHE_CONF_FOLDER}:/conf -e "CHE_LOCAL_CONF_DIR=/conf" if CHE_CONF_FOLDER is set
  CHE_CONF_ARGS=${CHE_CONF_FOLDER:+-v "${CHE_CONF_FOLDER}":/conf -e "CHE_LOCAL_CONF_DIR=/conf"}
  CHE_LOCAL_BINARY_ARGS=${CHE_LOCAL_BINARY:+-v ${CHE_LOCAL_BINARY}:/home/user/che}

  if is_docker_for_mac || is_docker_for_windows; then
    CHE_STORAGE_ARGS=${CHE_DATA_FOLDER:+-v "${CHE_DATA_FOLDER}/storage":/home/user/che/storage \
                                        -e "CHE_WORKSPACE_STORAGE=${CHE_DATA_FOLDER}/workspaces" \
                                        -e "CHE_WORKSPACE_STORAGE_CREATE_FOLDERS=false"}
  else
    CHE_STORAGE_ARGS=${CHE_DATA_FOLDER:+-v "${CHE_DATA_FOLDER}/storage":/home/user/che/storage \
                                        -v "${CHE_DATA_FOLDER}/workspaces":/home/user/che/workspaces}
  fi

  if [ "${CHE_LOG_LEVEL}" = "debug" ]; then
    CHE_DEBUG_OPTION="--debug --log_level:debug"
  else
    CHE_DEBUG_OPTION=""
  fi

  USAGE="
Usage:
  docker run -v /var/run/docker.sock:/var/run/docker.sock ${CHE_LAUNCHER_IMAGE_NAME} [COMMAND]
     start                              Starts Che server
     stop                               Stops Che server
     restart                            Restart Che server
     update                             Pull latest version of ${CHE_SERVER_IMAGE_NAME}
     info                               Print some debugging information

Docs: http://eclipse.org/che/getting-started.
"
}

parse_command_line () {
  if [ $# -eq 0 ]; then
    usage
    container_self_destruction
    exit
  fi

  for command_line_option in "$@"; do
    case ${command_line_option} in
      start|stop|restart|update|info)
        CHE_SERVER_ACTION=${command_line_option}
      ;;
      -h|--help)
        usage
        container_self_destruction
        exit
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  done
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
    stop_che_server
  ;;
  restart)
    restart_che_server
  ;;
  update)
    update_che_server
  ;;
  info)
    print_debug_info
  ;;
esac

# This container will self destruct after execution
container_self_destruction
