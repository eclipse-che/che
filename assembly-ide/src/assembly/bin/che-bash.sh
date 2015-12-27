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

### Start by setting configurable properties
# Any non-zero exit will prompt this output.
trap '[ "$?" -eq 0 ] || echo -e "\n\n!!!\nLooks like something went wrong. Possible issues: 
1. (Win | Mac) VirtualBox not installed           ==> Rerun Docker Toolbox installation
2. (Win | Mac) Docker Machine not installed       ==> Rerun Docker Toolbox installation
3. (Win | Mac) Docker is not reachable            ==> Docker VM failed to start
4. (Win | Mac) Docker ok, but ''docker ps'' fails ==> Docker environment variables not set properly
5. (Linux) Docker is not reachable                ==> Is Docker installed ==> wget -qO- https://get.docker.com/ | sh
6. Could not find the Che app server              ==> Did /tomcat get moved away from CHE_HOME?
7. Did you use the right parameter syntax?        ==> See usage below

Usage:
  che-bash [start | run | stop]

Environment Variables:
  (REQUIRED) JAVA_HOME                            ==> Location of Java runtime
  (REQUIRED: WIN|MAC) DOCKER_TOOLBOX_INSTALL_PATH ==> Location of Docker Toolbox
  (REQUIRED: WIN|MAC) VBOX_MSI_INSTALL_PATH       ==> Location of VirtualBox  
  (OPTIONAL) CHE_HOME                             ==> Directory where Che is installed
  (OPTIONAL) CHE_LOCAL_CONF_DIR                   ==> Directory with custom Che .properties files
  (OPTIONAL) CHE_LOGS_DIR                         ==> Directory for Che output logs
"' EXIT

# Set constants
# Name of the virtualbox VM that will be created
VM=default

# For coloring console output
BLUE='\033[1;34m'
GREEN='\033[0;32m'
NC='\033[0m'

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

# Create absolute file names for docker and docker-machine
# DOCKER_TOOLBOX_INSTALL_PATH set globally by Docker Toolbox installer
if [ win=true ]; then
  if [ ! -z "$DOCKER_TOOLBOX_INSTALL_PATH" ]; then
    export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker-machine.exe
    export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker.exe
  else
    exit 1
  fi
elif [ mac=true ]; then
  if [ ! -z "$DOCKER_TOOLBOX_INSTALL_PATH" ]; then
    export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}/docker-machine
    export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}/docker
  else
    exit 1
  fi  
fi 

### If Windows or Mac, launch docker-machine, if necessary
if [ win=true ] || [ mac=true ]; then
  # Path to run VirtualBox on the command line - used for creating VMs
  if [ ! -z "$VBOX_MSI_INSTALL_PATH" ]; then
    VBOXMANAGE=${VBOX_MSI_INSTALL_PATH}VBoxManage.exe
  else
    VBOXMANAGE=${VBOX_INSTALL_PATH}VBoxManage.exe
  fi

  if [ ! -f "${DOCKER_MACHINE}" ] || [ ! -f "${VBOXMANAGE}" ]; then
    exit 1
  fi


  # Test to see if the VM we need is already running
  "${VBOXMANAGE}" showvminfo $VM &> /dev/null
  VM_EXISTS_CODE=$?

  # Stops execution of script if there is an error
  set -e

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
  exit 1
fi

# Test to see that docker command works
docker &> /dev/null
DOCKER_EXISTS=$?

# Test to verify that docker can reach the VM
docker ps &> /dev/null
DOCKER_VM_REACHABLE=$?


# EOF
if [ win=true ] || [ mac=true ]; then
  echo -e "${BLUE}Docker${NC} is configured to use vbox docker-machine named ${GREEN}$VM${NC} with IP ${GREEN}$("${DOCKER_MACHINE}" ip $VM)${NC}..."
  echo
else
  echo -e "Docker is natively installed and reachable..."
fi

# Test to see that Che application server is where we expect it to be
if [ ! -d "${ASSEMBLY_BIN_DIR}" ]; then
  exit 1
fi

# Launch the Che application server, passing in command line parameters
${ASSEMBLY_BIN_DIR}/catalina.sh "$@"

# exec "$BASH" --login -i