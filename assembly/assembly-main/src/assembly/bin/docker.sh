#!/bin/sh
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

# This file is a specialized launcher to be used as an entrypoint for the Che Dockerfile.
# This script has logic to detect and configure Che if it is running inside of a container.
pid=0

check_docker() {
  if [ ! -S /var/run/docker.sock ]; then
    echo "Docker socket (/var/run/docker.sock) hasn't been mounted. Verify your \"docker run\" syntax."
    return 2;
  fi

  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    echo "Error when running \"docker ps\": ${output}"
    return 2;
  fi
}

init() {
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

  ### Any variables with export is a value that native Tomcat che.sh startup script requires

  ### Set these values for any che server running in a container
  DOCKER_HOST_IP=$(get_docker_host_ip)
  export CHE_IP=${CHE_IP:-${DOCKER_HOST_IP}}
  export CHE_IN_CONTAINER="true"
  export CHE_SKIP_JAVA_VERSION_CHECK="true"

  if [ -f "/assembly/bin/che.sh" ]; then
    echo "Found custom assembly..."
    export CHE_HOME="/assembly"
  else
    echo "Using embedded assembly..."
    export CHE_HOME="/home/user/che"
  fi

  ### Are we using the included assembly or did user provide their own?
#  DEFAULT_CHE_HOME="/assembly"
#  export CHE_HOME=${CHE_ASSEMBLY:-${DEFAULT_CHE_HOME}}

  if [ ! -f $CHE_HOME/bin/che.sh ]; then
    echo "!!!"
    echo "!!! Error: Could not find $CHE_HOME/bin/che.sh."
    echo "!!! Error: Did you use CHE_ASSEMBLY with a typo?"
    echo "!!!"
    exit 1
  fi

  ### We need to discover the host mount provided by the user for `/data`
 # DEFAULT_CHE_DATA="/data"
 # export CHE_DATA=${CHE_DATA:-${DEFAULT_CHE_DATA}}
  export CHE_DATA="/data"
  CHE_DATA_HOST=$(get_che_data_from_host)

  ### Are we going to use the embedded che.properties or one provided by user?`
  ### CHE_LOCAL_CONF_DIR is internal Che variable that sets where to load
#  DEFAULT_CHE_CONF_DIR="/conf"
#  export CHE_LOCAL_CONF_DIR="${CHE_DATA}/conf"
#  export CHE_LOCAL_CONF_DIR=${CHE_LOCAL_CONF_DIR:-${DEFAULT_CHE_CONF_DIR}}

  if [ -f "/conf/che.properties" ]; then
    echo "Found custom che.properties..."
    export CHE_LOCAL_CONF_DIR="/conf"
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

  ### If this container is inside of a VM like boot2docker, then additional internal mods required
  DEFAULT_CHE_IN_VM=$(is_in_vm)
  export CHE_IN_VM=${CHE_IN_VM:-${DEFAULT_CHE_IN_VM}}

  if [ "$CHE_IN_VM" = "true" ]; then
    # CHE_DOCKER_MACHINE_HOST_EXTERNAL must be set if you are in a VM. 
    HOSTNAME=$(get_docker_external_hostname)
    if has_external_hostname; then
      # Internal property used by Che to set hostname.
      # See: LocalDockerInstanceRuntimeInfo.java#L9
      export CHE_DOCKER_MACHINE_HOST_EXTERNAL=${HOSTNAME}
    fi
    ### Necessary to allow the container to write projects to the folder
    export CHE_WORKSPACE_STORAGE="${CHE_DATA_HOST}/workspaces"
    export CHE_WORKSPACE_STORAGE_CREATE_FOLDERS=false
  fi

  # Ensure that the user "user" has permissions for CHE_HOME and CHE_DATA
  sudo chown -R user:user ${CHE_HOME}
  sudo chown -R user:user ${CHE_DATA}

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
  CHE_SERVER_CONTAINER_ID=$(get_che_server_container_id)
  echo $(docker inspect --format='{{(index .Volumes "/data")}}' $CHE_SERVER_CONTAINER_ID)
}

get_che_server_container_id() {
  hostname
}

get_docker_host_ip() {
  case $(get_docker_install_type) in
   boot2docker)
     echo $ETH1_ADDRESS
   ;;
   native)
     echo $DOCKER0_ADDRESS
   ;;
   *)
     echo $ETH0_ADDRESS
   ;;
  esac
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
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  if uname -r | grep -q 'moby' && has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_ip() {
  if [ "${ETH0_ADDRESS}" = "10.0.75.2" ]; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  if uname -r | grep -q 'moby' && ! has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

is_in_vm() {
  if is_docker_for_mac || is_docker_for_windows || is_boot2docker; then
    echo "true"
  else
    echo "false"
  fi
}

get_docker_external_hostname() {
  if is_docker_for_mac || is_docker_for_windows; then
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
  "${CHE_HOME}"/bin/che.sh stop
  wait ${PID}
  exit;
}

# setup handlers
# on callback, kill the last background process, which is `tail -f /dev/null` and execute the specified handler
trap 'responsible_shutdown' SIGHUP SIGTERM SIGINT

check_docker
init

# run application
"${CHE_HOME}"/bin/che.sh run &
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
