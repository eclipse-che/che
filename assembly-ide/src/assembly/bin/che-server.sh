#!/bin/sh
#
# Copyright (c) 2012-2015 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

### Set constants
# Name of the virtualbox VM that will be created
VM=default

# For coloring console output
BLUE='\033[1;34m'
GREEN='\033[0;32m'
NC='\033[0m'

### Define various output windows
# If exit=2, then no output upon finish
# If exit=1 then output error message + usage
# if exit=0 then print usage

WRONG="\nLooks like something went wrong. Possible issues: 
  1. (Win | Mac) VirtualBox not installed           ==> Rerun Docker Toolbox installation
  2. (Win | Mac) Docker Machine not installed       ==> Rerun Docker Toolbox installation
  3. (Win | Mac) Docker is not reachable            ==> Docker VM failed to start
  4. (Win | Mac) Docker ok, but docker ps fails     ==> Docker environment variables not set properly
  5. (Linux) Docker is not reachable                ==> Is Docker installed ==> wget -qO- https://get.docker.com/ | sh
  6. Could not find the Che app server              ==> Did /tomcat get moved away from CHE_HOME?
  7. Did you use the right parameter syntax?        ==> See usage

We have seen issues with VirtualBox on windows where your VM gets corrupted when your computer is suspended while the VM is still running. This will appear as SSH or ethernet connection issues. This is rare, but if encountered, current known solution is to uninstall VirtualBox and Docker Toolbox, and then reinstall."

CHE_VARIABLES="\n
Che Environment Variables:
  (REQUIRED) JAVA_HOME                              ==> Location of Java runtime
  (REQUIRED: WIN|MAC) DOCKER_TOOLBOX_INSTALL_PATH   ==> Location of Docker Toolbox
  (REQUIRED: WIN|MAC) VBOX_MSI_INSTALL_PATH         ==> Location of VirtualBox  
  (OPTIONAL) CHE_HOME                               ==> Directory where Che is installed
  (OPTIONAL) CHE_LOCAL_CONF_DIR                     ==> Directory with custom Che .properties files
  (OPTIONAL) CHE_LOGS_DIR                           ==> Directory for Che output logs\n\n"

USAGE="
Usage: 
  che-server [-i] [-i:tag] [-p:port] [run | start | stop]

     -i,      --image        Launches Che within a Docker container using latest image
     -i:tag,  --image:tag    Launches Che within a Docker container using specific image tag
     -p:port, --port:port    Port that Che server will use for HTTP requests; default=8080
     -h,      --help         Show this help
     run                     Starts Che application server in current console
     start                   Starts Che application server in new console
     stop                    Stops Che application server"

function usage {
  echo -e "$USAGE"
}

function error_exit {
  echo -e "$1"
  echo -e "$WRONG $CHE_VARIABLES $USAGE"
  JUMP_TO_END=true
}

# Run the finish function if exit signal initiated
trap error_exit SIGHUP SIGINT SIGTERM

function parse_command_line {

  ### Parse command line parameters
  USE_DOCKER=false
  USE_DOCKER_TAG=latest
  USE_PORT=8080
  USE_HELP=false
  USE_SERVER_ACTION=run

  for command_line_option in "$@"
  do
  case $command_line_option in
    -i|--image)
      USE_DOCKER=true
      shift # past argument
    ;;
    -i:*|--image:*)
      USE_DOCKER=true
      USE_DOCKER_TAG="${command_line_option#*:}"
      shift # past argument=value
    ;;
    -p:*|--port:*)
      USE_PORT="${command_line_option#*:}"
      shift # past argument=value
    ;;
    -h|--help)
      USE_HELP=true
      usage
      return
      shift # past argument
    ;;
    start|stop|run)
      USE_SERVER_ACTION=${command_line_option}
      shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
  esac
  done
}

function determine_os {
  # Set OS.  Mac & Windows require VirtualBox and docker-machine.
  win=false
  mac=false
  linux=false

  if [[ "$OSTYPE" == "linux-gnu" ]]; then
    # Linux
    linux=true
  elif [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    mac=true
  elif [[ "$OSTYPE" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    win=true
  elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    win=true
  elif [[ "$OSTYPE" == "win32" ]]; then
    # I'm not sure this can happen.
    win=true
  elif [[ "$OSTYPE" == "freebsd"* ]]; then
    # FreeBSD
    linux=true
  else
    # Unknown.
    echo We could not detect your operating system.
    echo Che is unlikely to manage Docker properly.
  fi
}

function set_environment_variables {
  ### Set the value of derived environment variables.
  ### Use values set by user, unless they are broken, then fix them
  # The base directory of Che
  if [ -z "${CHE_HOME}" ] || [ ! -f "${CHE_HOME}" ]; then
    export CHE_HOME="$(dirname "$(cd $(dirname ${0}) && pwd -P)")"
  fi

  # Convert Tomcat environment variables to POSIX format.
  if [[ $JAVA_HOME == *":"* ]]
  then 
    JAVA_HOME=$(echo /$JAVA_HOME | sed  's|\\|/|g' | sed 's|:||g')
  fi

  if [[ $CHE_HOME == *":"* ]]
  then 
    CHE_HOME=$(echo /$CHE_HOME | sed  's|\\|/|g' | sed 's|:||g')
  fi

  # Che configuration directory - where .properties files can be placed by user
  if [ -z "${CHE_LOCAL_CONF_DIR}" ] || [ ! -f "${CHE_LOCAL_CONF_DIR}" ]; then
    export CHE_LOCAL_CONF_DIR="${CHE_HOME}/conf/"
  fi

  # Sets the location of the application server and its executables
  export CATALINA_HOME="${CHE_HOME}/tomcat"

  # Convert windows path name to POSIX
  if [[ $CATALINA_HOME == *":"* ]]
  then 
    CATALINA_HOME=$(echo /$CATALINA_HOME | sed  's|\\|/|g' | sed 's|:||g')
  fi

  export CATALINA_BASE="${CHE_HOME}/tomcat"
  export ASSEMBLY_BIN_DIR="${CATALINA_HOME}/bin"

  # Global logs directory
  if [ -z "${CHE_LOGS_DIR}" ] || [ ! -f "${CHE_LOGS_DIR}" ]; then
    export CHE_LOGS_DIR="${CATALINA_HOME}/logs/"
  fi
}

function get_docker_ready {
  # Create absolute file names for docker and docker-machine
  # DOCKER_TOOLBOX_INSTALL_PATH set globally by Docker Toolbox installer
  if [ $win ]; then
    if [ ! -z "$DOCKER_TOOLBOX_INSTALL_PATH" ]; then
      export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker-machine.exe
      export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker.exe
    else
      error_exit "\n!!! DOCKER_TOOL_BOX_INSTALL_PATH environment variable not set. Add it or rerun Docker Toolbox installation."
      return
    fi
  elif [ $mac ]; then
    if [ ! -z "$DOCKER_TOOLBOX_INSTALL_PATH" ]; then
      export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}/docker-machine
      export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}/docker
    else
      error_exit "\n!!! DOCKER_TOOL_BOX_INSTALL_PATH environment variable not set. Add it or rerun Docker Toolbox installation."
      return
    fi
  elif [ $linux ]; then
    export DOCKER_MACHINE=
    export DOCKER=docker
  fi 

  ### If Windows or Mac, launch docker-machine, if necessary
  if [ $win ] || [ $mac ]; then
    # Path to run VirtualBox on the command line - used for creating VMs
    if [ ! -z "$VBOX_MSI_INSTALL_PATH" ]; then
      VBOXMANAGE=${VBOX_MSI_INSTALL_PATH}VBoxManage.exe
    else
      VBOXMANAGE=${VBOX_INSTALL_PATH}VBoxManage.exe
    fi

    if [ ! -f "${DOCKER_MACHINE}" ]; then
      error_exit "\n!!! Could not find docker-machine executable. DOCKER_TOOL_BOX_INSTALL_PATH environment variable not set. Add it or rerun Docker Toolbox installation."
      return
    fi

    if [ ! -f "${VBOXMANAGE}" ]; then
      error_exit "\n!!! Could not find VirtualBox. VBOX_MSI_INSTALL_PATH environment variable not set. Add it or rerun Docker Toolbox installation."
      return
    fi

    # Test to see if the VM we need is already running
    "${VBOXMANAGE}" showvminfo $VM &> /dev/null
    VM_EXISTS_CODE=$?

    # Stops execution of script if there is an error
    #set -e

    if [ $VM_EXISTS_CODE -eq 1 ]; then
      echo "Creating docker machine named $VM..."
      "$DOCKER_MACHINE" rm -f $VM &> /dev/null || :
      rm -rf ~/.docker/machine/machines/$VM
      "$DOCKER_MACHINE" create -d virtualbox $VM
    else
      echo -e "Docker machine named ${GREEN}$VM${NC} already exists..."
    fi

    VM_STATUS=$("${DOCKER_MACHINE}" status $VM 2>&1)

    if [ "$VM_STATUS" != "Running" ]; then
      echo "Starting machine named $VM..."
      "${DOCKER_MACHINE}" start $VM
      yes | "${DOCKER_MACHINE}" regenerate-certs $VM
    fi

    echo -e "Setting environment variables for machine ${GREEN}$VM${NC}..."
    eval "$("${DOCKER_MACHINE}" env --shell=bash $VM)"
  fi
  ### End logic block to create / remove / start docker-machine VM

  # Docker should be available, either in a VM or natively.
  # Test to see if docker binary is installed
  if [ ! -f "${DOCKER}" ]; then
      error_exit "\n!!! Could not find Docker client."
    return
  fi

  # Test to see that docker command works
  "${DOCKER}" &> /dev/null
  DOCKER_EXISTS=$?

  # Test to verify that docker can reach the VM
  "${DOCKER}" ps &> /dev/null
  DOCKER_VM_REACHABLE=$?

  if [ $DOCKER_EXISTS -eq 1 ] || [ $DOCKER_VM_REACHABLE -eq 1 ]; then
      error_exit "\n!!! Running 'docker' and 'docker ps' failed. Either docker is unreachable, or docker cannot reach your vbox VM."
    return
  fi

  # EOF
  if [ $win ] || [ $mac ]; then
    echo -e "${BLUE}Docker${NC} is configured to use vbox docker-machine named ${GREEN}$VM${NC} with IP ${GREEN}$("${DOCKER_MACHINE}" ip $VM)${NC}..."
    echo
  else
    echo -e "Docker is natively installed and reachable..."
  fi  
}

function strip_url {
  # extract the protocol
  proto="`echo $1 | grep '://' | sed -e's,^\(.*://\).*,\1,g'`"
  
  # remove the protocol
  url=`echo $1 | sed -e s,$proto,,g`

  # extract the user and password (if any)
  userpass="`echo $url | grep @ | cut -d@ -f1`"
  pass=`echo $userpass | grep : | cut -d: -f2`
  
  if [ -n "$pass" ]; then
      user=`echo $userpass | grep : | cut -d: -f1`
  else
      user=$userpass
  fi

  # extract the host and remove the port
  hostport=`echo $url | sed -e s,$userpass@,,g | cut -d/ -f1`
  port=`echo $hostport | grep : | cut -d: -f2`
  if [ -n "$port" ]; then
      host=`echo $hostport | grep : | cut -d: -f1`
  else
      host=$hostport
  fi

  # extract the path (if any)
  path="`echo $url | grep / | cut -d/ -f2-`"

}

function print_client_connect {
  if [ ! $USE_DOCKER ]; then 
    echo -e "
############## HOW TO CONNECT YOUR CHE CLIENT ###############
After Che server has booted, you can connect your clients by:
1. Open browser to http://localhost:${USE_PORT}
2. Open chromium app.
#############################################################\n\n"
  else 
    echo -e "
############## HOW TO CONNECT YOUR CHE CLIENT ###############
After Che server has booted, you can connect your clients by:
1. Open browser to http://${host}:${USE_PORT}
2. Open chromium app.
#############################################################\n\n"

  fi
}

function call_catalina {

  # Test to see that Che application server is where we expect it to be
  if [ ! -d "${ASSEMBLY_BIN_DIR}" ]; then
    error_exit
    return
  fi

  ### Cannot add this in setenv.sh.
  ### We do the port mapping here, and this gets inserted into server.xml when tomcat boots
  [ -z "${JAVA_OPTS}" ]  && export JAVA_OPTS="-Dport.http=${USE_PORT}"
  #Class path
  [ -z "${SERVER_PORT}" ]  && export SERVER_PORT=${USE_PORT}

  # Launch the Che application server, passing in command line parameters
  ${ASSEMBLY_BIN_DIR}/catalina.sh ${USE_SERVER_ACTION}

}

function stop_che_server {

  if ! $USE_DOCKER; then
    echo -e "Stopping Che server running on localhost:${USE_PORT}"
    call_catalina >/dev/null 2>&1
  else
    echo -e "Stopping Che server running in docker container."
    "${DOCKER}" exec che /home/user/che/bin/che.sh stop 
    DOCKER_EXIT=$?

    echo -e "Stopping docker container named che."
    "${DOCKER}" stop che
    DOCKER_EXIT=$?
  fi
}

function launch_che_server {

 strip_url $DOCKER_HOST
 print_client_connect

  # Launch Che natively as a tomcat server
  if ! $USE_DOCKER; then
    call_catalina

  # Launch Che as a docker image
  else
    
    # Check to see if the Che docker was not properly shut down
    "${DOCKER}" inspect che &> /dev/null
    DOCKER_INSPECT_EXIT=$?

    echo -e "Starting Che server in existing docker container named che."

    # Attempt restart of existing container named "che"
    "${DOCKER}" start che &> /dev/null
    DOCKER_EXIT=$?
    
    # If either command fails, then wipe any existing image and restarting
    if [ ${DOCKER_INSPECT_EXIT} -eq 1 ] || [ ${DOCKER_EXIT} -eq 1 ]; then
      echo -e "Either che container does not exist, or duplicate conflict was discovered. "
      echo -e "\nRemoving any old containers and launching a new one using image: codenvy/che:${USE_DOCKER_TAG}...\n"
      "${DOCKER}" kill che &> /dev/null
      "${DOCKER}" rm che &> /dev/null

      if [ $win ] || [ $mac ]; then
        "${DOCKER}" run --privileged -e '"'DOCKER_MACHINE_HOST=${host}'"' --name che -it -p ${USE_PORT}:${USE_PORT} -p 32768-32788:32768-32788 codenvy/che:${USE_DOCKER_TAG} #&> /dev/null
      else
        "${DOCKER}" run --privileged --name che -it -p ${USE_PORT}:${USE_PORT} -p 32768-32788:32768-32788 codenvy/che:${USE_DOCKER_TAG} &> /dev/null
      fi    
    fi
  fi
}


# Call function
parse_command_line $@

if ! $USE_HELP; then
  
  ### Short circuit
  JUMP_TO_END=false

  # Call function
  determine_os

  # Call function
  set_environment_variables

  ### Variables are all set.  Get Docker ready
  get_docker_ready

  ### Launch or shut down Che server
  if ! $JUMP_TO_END; then 
    if [ $USE_SERVER_ACTION == "stop" ]; then
      stop_che_server 
    else
      launch_che_server
    fi
  fi
fi

# exec "$BASH" --login -i
