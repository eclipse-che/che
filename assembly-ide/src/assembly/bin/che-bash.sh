#!/bin/bash

### Start by setting configurable properties
# Any non-zero exit will prompt this output.
trap '[ "$?" -eq 0 ] || echo -e "\n\n!!!\nLooks like something went wrong. Possible issues: 
1. VirtualBox is not installed               ==> Rerun Docker Toolbox installation.
2. Docker or Docker Machine is not installed ==> Rerun Docker Toolbox installation.
3. Could not find the Che app server         ==> Did /tomcat get moved away from CHE_HOME?
4. Did you use the right parameter syntax?   ==> See usage below

Usage:
  che-bash [start | run | stop]

Environment Variables:
  (REQUIRED) JAVA_HOME                   ==> Location of Java runtime
  (REQUIRED) DOCKER_TOOLBOX_INSTALL_PATH ==> Location of Docker Toolbox
  (REQUIRED) VBOX_MSI_INSTALL_PATH       ==> Location of VirtualBox  
  (OPTIONAL) CHE_HOME                    ==> Directory where Che is installed
  (OPTIONAL) CHE_LOCAL_CONF_DIR          ==> Directory with .properties files
  (OPTIONAL) CHE_LOGS_DIR                ==> Directory for Che output logs
"' EXIT

# Name of the virtualbox VM that will be created
VM=default

# For coloring console output
BLUE='\033[1;34m'
GREEN='\033[0;32m'
NC='\033[0m'

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

# Creating absolute file names for docker and docker-machine
if [ ! -z "$DOCKER_TOOLBOX_INSTALL_PATH" ]; then
  export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker-machine.exe
  export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker.exe
else
  exit 1
fi

# Path to run VirtualBox on the command line - used for creating VMs
if [ ! -z "$VBOX_MSI_INSTALL_PATH" ]; then
  VBOXMANAGE=${VBOX_MSI_INSTALL_PATH}VBoxManage.exe
else
  VBOXMANAGE=${VBOX_INSTALL_PATH}VBoxManage.exe
fi

if [ ! -f "${DOCKER_MACHINE}" ] || [ ! -f "${DOCKER}" ] || [ ! -f "${VBOXMANAGE}" ]; then
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

# clear
# cat << EOF

# EOF
echo -e "${BLUE}Docker${NC} is configured to use the machine named ${GREEN}$VM${NC} with IP ${GREEN}$("${DOCKER_MACHINE}" ip $VM)${NC}"
echo

if [ ! -d "${ASSEMBLY_BIN_DIR}" ]; then
  exit 1
fi

${ASSEMBLY_BIN_DIR}/catalina.sh "$@"

# exec "$BASH" --login -i
