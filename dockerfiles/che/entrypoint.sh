#!/bin/bash
#
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

init_global_variables () {
  # For coloring console output
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  NC='\033[0m'

  USAGE="
Usage:
  che [COMMAND]
     start                              Starts server with output in the background
     stop                               Stops ${CHE_MINI_PRODUCT_NAME} server
     run                                Starts server with output in the foreground

Variables:
    CHE_SERVER_ACTION                   Another way to set the [COMMAND] to [run | start | stop]
    CHE_PORT                            The port the Che server will listen on
    CHE_IP                              The IP address of the host - must be set if remote clients connecting
    CHE_LOCAL_CONF_DIR                  If set, will load che.properties from folder
    CHE_BLOCKING_ENTROPY                Starts Tomcat with blocking entropy: -Djava.security.egd=file:/dev/./urandom
    CHE_LAUNCH_DOCKER_REGISTRY          If true, uses Docker registry to save ws snapshots instead of disk
    CHE_REGISTRY_HOST                   Hostname of Docker registry to launch, otherwise 'localhost'
    CHE_LOG_LEVEL                       [INFO | DEBUG] Sets the output level of Tomcat messages
    CHE_DEBUG_SERVER                    If true, activates Tomcat's JPDA debugging mode
    CHE_HOME                            Where the Che assembly resides - self-determining if not set
"

  # Use blocking entropy -- needed for some servers
  DEFAULT_CHE_BLOCKING_ENTROPY=false
  CHE_BLOCKING_ENTROPY=${CHE_BLOCKING_ENTROPY:-${DEFAULT_CHE_BLOCKING_ENTROPY}}

  DEFAULT_CHE_SERVER_ACTION=run
  CHE_SERVER_ACTION=${CHE_SERVER_ACTION:-${DEFAULT_CHE_SERVER_ACTION}}

  DEFAULT_CHE_LAUNCH_DOCKER_REGISTRY=false
  CHE_LAUNCH_DOCKER_REGISTRY=${CHE_LAUNCH_DOCKER_REGISTRY:-${DEFAULT_CHE_LAUNCH_DOCKER_REGISTRY}}

  # Must be exported as this will be needed by Tomcat's JVM
  DEFAULT_CHE_REGISTRY_HOST=localhost
  export CHE_REGISTRY_HOST=${CHE_REGISTRY_HOST:-${DEFAULT_CHE_REGISTRY_HOST}}

  DEFAULT_CHE_PORT=8080
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}

  DEFAULT_CHE_IP=
  CHE_IP=${CHE_IP:-${DEFAULT_CHE_IP}}

  DEFAULT_CHE_LOG_LEVEL=INFO
  CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}

  DEFAULT_CHE_DEBUG_SERVER=false
  CHE_DEBUG_SERVER=${CHE_DEBUG_SERVER:-${DEFAULT_CHE_DEBUG_SERVER}}

}

error () {
  echo
  echo "!!!"
  echo -e "!!! ${1}"
  echo "!!!"
  return 0
}

usage () {
  echo "${USAGE}"
}

set_environment_variables () {
  ### Set value of derived environment variables.

  # CHE_DOCKER_MACHINE_HOST is used internally by Che to set its IP address
  if [[ -n "${CHE_IP}" ]]; then
    export CHE_DOCKER_MACHINE_HOST="${CHE_IP}"
  fi

  # Convert Tomcat environment variables to POSIX format.
  if [[ "${JAVA_HOME}" == *":"* ]]; then
    JAVA_HOME=$(echo /"${JAVA_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  # Convert Che environment variables to POSIX format.
  if [[ "${CHE_HOME}" == *":"* ]]; then
    CHE_HOME=$(echo /"${CHE_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  # Che configuration directory - where che.properties lives
  if [ -z "${CHE_LOCAL_CONF_DIR}" ]; then
    export CHE_LOCAL_CONF_DIR="${CHE_HOME}/conf/"
  fi

  # Sets the location of the application server and its executables
  # Internal property - should generally not be overridden
  export CATALINA_HOME="${CHE_HOME}/tomcat"

  # Convert windows path name to POSIX
  if [[ "${CATALINA_HOME}" == *":"* ]]; then
    CATALINA_HOME=$(echo /"${CATALINA_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  # Internal properties - should generally not be overridden
  export CATALINA_BASE="${CHE_HOME}/tomcat"
  export ASSEMBLY_BIN_DIR="${CATALINA_HOME}/bin"
  export CHE_LOGS_LEVEL="${CHE_LOG_LEVEL}"
  export CHE_LOGS_DIR="${CATALINA_HOME}/logs/"
}

docker_exec() {
  "$(which docker)" "$@"
}

start_che_server () {
 if ${CHE_LAUNCH_DOCKER_REGISTRY} ; then
    # Export the value of host here
    launch_docker_registry
  fi

  #########################################
  # Launch Che natively as a tomcat server
  call_catalina
}

stop_che_server () {
  CHE_SERVER_ACTION="stop"
  echo -e "Stopping Che server running on localhost:${CHE_PORT}"
  call_catalina >/dev/null 2>&1
}

call_catalina () {
  # Test to see that Che application server is where we expect it to be
  if [ ! -d "${ASSEMBLY_BIN_DIR}" ]; then
    error "Could not find Che's application server."
    return 1;
  fi

  ### Initialize default JVM arguments to run che
  if [[ "${CHE_BLOCKING_ENTROPY}" == true ]]; then
    [ -z "${JAVA_OPTS}" ] && JAVA_OPTS="-Xms256m -Xmx1024m"
  else
    [ -z "${JAVA_OPTS}" ] && JAVA_OPTS="-Xms256m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
  fi

  ### Cannot add this in setenv.sh.
  ### We do the port mapping here, and this gets inserted into server.xml when tomcat boots
  export JAVA_OPTS="${JAVA_OPTS} -Dport.http=${CHE_PORT} -Dche.home=${CHE_HOME}"
  export SERVER_PORT=${CHE_PORT}

  # Launch the Che application server, passing in command line parameters
  if [[ "${CHE_DEBUG_SERVER}" == true ]]; then
    "${ASSEMBLY_BIN_DIR}"/catalina.sh jpda ${CHE_SERVER_ACTION}
  else
    "${ASSEMBLY_BIN_DIR}"/catalina.sh ${CHE_SERVER_ACTION}
  fi
}

kill_and_launch_docker_registry () {
  echo -e "Launching Docker container named ${GREEN}registry${NC} from image ${GREEN}registry:2${NC}."
  docker_exec rm -f registry &> /dev/null || true
  docker_exec run -d -p 5000:5000 --restart=always --name registry registry:2
}

launch_docker_registry () {
    echo "Launching a Docker registry for workspace snapshots."
    CREATE_NEW_CONTAINER=false

    # Check to see if the registry docker was not properly shut down
    docker_exec inspect registry &> /dev/null || DOCKER_INSPECT_EXIT=$? || true
    if [ "${DOCKER_INSPECT_EXIT}" != "1" ]; then

      # Existing container running registry is found.  Let's start it.
      echo -e "Found a registry container named ${GREEN}registry${NC}. Attempting restart."
      docker_exec start registry &>/dev/null || DOCKER_EXIT=$? || true

      # Existing container found, but could not start it properly.
      if [ "${DOCKER_EXIT}" == "1" ]; then
        echo "Initial start of registry docker container failed... Attempting docker restart and exec."
        CREATE_NEW_CONTAINER=true
      fi

    echo "Successful restart of registry container."
    echo

    # No existing Che container found, we need to create a new one.
    else
      CREATE_NEW_CONTAINER=true
    fi

    if ${CREATE_NEW_CONTAINER} ; then
      # Container in bad state or not found, kill and launch new container.
      kill_and_launch_docker_registry
    fi
}

init() {
  ### Any variables with export is a value that native Tomcat che.sh startup script requires
  export CHE_IP=${CHE_IP}

  if [ -f "/assembly/conf/che.properties" ]; then
    echo "Found custom assembly..."
    export CHE_HOME="/assembly"
  else
    echo "Using embedded assembly..."
    export CHE_HOME=$(echo /home/user/eclipse-che-*)
  fi

  ### Are we using the included assembly or did user provide their own?
  if [ ! -f $CHE_HOME/conf/che.properties ]; then
    echo "!!!"
    echo "!!! Error: Could not find $CHE_HOME/conf/che.properties."
    echo "!!! Error: Did you use CHE_ASSEMBLY with a typo?"
    echo "!!!"
    exit 1
  fi

  ### We need to discover the host mount provided by the user for `/data`
  export CHE_DATA="/data"
  CHE_DATA_HOST=$(get_che_data_from_host)

  CHE_USER=${CHE_USER:-root}
  export CHE_USER=$CHE_USER
  if [ "$CHE_USER" != "root" ]; then
    if [ ! $(getent group docker) ]; then
      echo "!!!"
      echo "!!! Error: The docker group doesn't exist."
      echo "!!!"
      exit 1
    fi
    export CHE_USER_ID=`id -u ${CHE_USER}`:`getent group docker | cut -d: -f3`
    sudo chown -R ${CHE_USER}:docker ${CHE_DATA}
    sudo chown -R ${CHE_USER}:docker ${CHE_HOME}
  fi
  ### Are we going to use the embedded che.properties or one provided by user?`
  ### CHE_LOCAL_CONF_DIR is internal Che variable that sets where to load
  if [ -f "/conf/che.properties" ]; then
    echo "Found custom che.properties..."
    export CHE_LOCAL_CONF_DIR="/conf"
    if [ "$CHE_USER" != "root" ]; then
      sudo chown -R ${CHE_USER}:docker ${CHE_LOCAL_CONF_DIR}
    fi
  else
    echo "Using embedded che.properties... Copying template to ${CHE_DATA_HOST}/conf."
    mkdir -p /data/conf
    cp -rf "${CHE_HOME}/conf/che.properties" /data/conf/che.properties
    export CHE_LOCAL_CONF_DIR="/data/conf"
  fi

  # Update the provided che.properties with the location of the /data mounts
  sed -i "/che.workspace.storage=/c\che.workspace.storage=/data/workspaces" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.database=/c\che.database=/data/storage" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.template.storage=/c\che.template.storage=/data/templates" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.stacks.storage=/c\che.stacks.storage=/data/stacks/stacks.json" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.stacks.images=/c\che.stacks.images=/data/stacks/images" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.workspace.agent.dev=/c\che.workspace.agent.dev=${CHE_DATA_HOST}/lib/ws-agent.tar.gz" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.workspace.terminal_linux_amd64=/c\che.workspace.terminal_linux_amd64=${CHE_DATA_HOST}/lib/linux_amd64/terminal" $CHE_LOCAL_CONF_DIR/che.properties
  sed -i "/che.workspace.terminal_linux_arm7=/c\che.workspace.terminal_linux_arm7=${CHE_DATA_HOST}/lib/linux_arm7/terminal" $CHE_LOCAL_CONF_DIR/che.properties

  # CHE_DOCKER_IP_EXTERNAL must be set if you are in a VM.
  HOSTNAME=${CHE_DOCKER_IP_EXTERNAL:-$(get_docker_external_hostname)}
  if has_external_hostname; then
    # Internal property used by Che to set hostname.
    export CHE_DOCKER_IP_EXTERNAL=${HOSTNAME}
  fi
  ### Necessary to allow the container to write projects to the folder
  export CHE_WORKSPACE_STORAGE="${CHE_DATA_HOST}/workspaces"
  export CHE_WORKSPACE_STORAGE_CREATE_FOLDERS=false

  # Move files from /lib to /lib-copy.  This puts files onto the host.
  rm -rf ${CHE_DATA}/lib/*
  mkdir -p ${CHE_DATA}/lib  
  cp -rf ${CHE_HOME}/lib/* "${CHE_DATA}"/lib

  if [[ ! -f "${CHE_DATA}"/stacks/stacks.json ]];then
    rm -rf "${CHE_DATA}"/stacks/*
    mkdir -p "${CHE_DATA}"/stacks
    cp -rf "${CHE_HOME}"/stacks/* "${CHE_DATA}"/stacks
  fi

  if [[ ! -f "${CHE_DATA}"/templates/samples.json ]];then
    rm -rf "${CHE_DATA}"/templates/*
    mkdir -p "${CHE_DATA}"/templates
    cp -rf "${CHE_HOME}"/templates/* "${CHE_DATA}"/templates
  fi

  # A che property, which names the Docker network used for che + ws to communicate
  export JAVA_OPTS="${JAVA_OPTS} -Dche.docker.network=bridge"
}

get_che_data_from_host() {
  DEFAULT_DATA_HOST_PATH=/data
  CHE_SERVER_CONTAINER_ID=$(get_che_server_container_id)
  # If `docker inspect` fails $DEFAULT_DATA_HOST_PATH is returned
  echo $(docker inspect --format='{{(index .Volumes "/data")}}' $CHE_SERVER_CONTAINER_ID 2>/dev/null || echo $DEFAULT_DATA_HOST_PATH)
}

get_che_server_container_id() {
  # Returning `hostname` doesn't work when running Che on OpenShift/Kubernetes.
  # In these cases `hostname` correspond to the pod ID that is different from
  # the container ID
  hostname
}

is_docker_for_mac_or_windows() {
  if uname -r | grep -q 'moby'; then
    return 0
  else
    return 1
  fi
}

get_docker_external_hostname() {
  if is_docker_for_mac_or_windows; then
    echo "localhost"
  else
    echo ""
  fi
}

has_external_hostname() {
  if [ "${HOSTNAME}" = "" ]; then
    return 1
  else
    return 0
  fi
}

# SITTERM / SIGINT
responsible_shutdown() {
  echo ""
  echo "Received SIGTERM"
  stop_che_server &
  wait ${PID}
  exit;
}

set -e
set +o posix

# setup handlers
# on callback, kill the last background process, which is `tail -f /dev/null` and execute the specified handler
trap 'responsible_shutdown' SIGHUP SIGTERM SIGINT

init
init_global_variables
set_environment_variables

# run che
start_che_server &

PID=$!

# See: http://veithen.github.io/2014/11/16/sigterm-propagation.html
wait ${PID}
wait ${PID}
EXIT_STATUS=$?

# wait forever
while true
do
  tail -f /dev/null & wait ${!}
done
