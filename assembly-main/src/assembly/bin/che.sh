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

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set +o posix

# Run the finish function if exit signal initiated
trap exit SIGHUP SIGINT SIGTERM

init_global_variables () {

  # Short circuit
  JUMP_TO_END=false

  # For coloring console output
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  NC='\033[0m'

  ### Define various error and usage messages
  WRONG="
Looks like something went wrong. Possible issues:
  1. (Win | Mac) VirtualBox not installed          ==> Rerun Docker Toolbox installation
  2. (Win | Mac) Docker Machine not installed      ==> Rerun Docker Toolbox installation
  3. (Win | Mac) Docker is not reachable           ==> Docker VM failed to start
  4. (Win | Mac) Docker ok, but docker ps fails    ==> Docker environment variables not set properly
  5. (Linux) Docker is not reachable               ==> Install: wget -qO- https://get.docker.com/ | sh
  6. (Linux) Permissions not properly set          ==> Che must run as UID 1000 with user in docker group
  7. Could not find the Che app server             ==> Did /tomcat get moved away from CHE_HOME?
  8. Wrong version of Java found                   ==> Che requires Java 1.8
  9. Did you use the right parameter syntax?       ==> See usage

We have seen issues with VirtualBox on Windows where your VM gets corrupted when your computer is
suspended while the VM is still running. This will appear as SSH or ethernet connection issues. This is
rare, but if encountered, current known solution is to uninstall VirtualBox and Docker Toolbox, and then
reinstall.
"

  CHE_VARIABLES="
Che Environment Variables:
  (REQUIRED) JAVA_HOME                             ==> Location of Java runtime
  (REQUIRED: WIN|MAC) DOCKER_TOOLBOX_INSTALL_PATH  ==> Location of Docker Toolbox
  (REQUIRED: WIN|MAC) VBOX_MSI_INSTALL_PATH        ==> Location of VirtualBox
  (OPTIONAL) CHE_HOME                              ==> Directory where Che is installed
  (OPTIONAL) CHE_LOCAL_CONF_DIR                    ==> Directory with custom Che .properties files
  (OPTIONAL) CHE_LOGS_DIR                          ==> Directory for Che output logs
  (OPTIONAL) CHE_DOCKER_MACHINE_NAME               ==> (Win | Mac) Name of VM created by docker-machine
  (OPTIONAL) CHE_CONTAINER_NAME                    ==> Name to apply to Docker container if using -i option
  (OPTIONAL) DOCKER_MACHINE_HOST                   ==> (Linux) Docker host IP - set if browser clients remote
  "

  USAGE="
Usage:
  che [OPTIONS] [run | start | stop]
     -i,        --image              Launches Che within a Docker container using latest image
     -i:tag,    --image:tag          Launches Che within a Docker container using specific image tag
     -c:name,   --container:name     Sets the container name if -i provided; default=che
     -p:port,   --port:port          Port that Che server will use for HTTP requests; default=8080
     -r:ip,     --remote:ip          If Che clients are not localhost, set to IP address of Che server
     -g,        --registry           Launch Docker registry as a container (used for ws snapshots)
     -m:name,   --machine:name       For Win & Mac, sets the docker-machine VM name; default=default
     -s:client, --skip:client        Do not print browser client connection information
     -s:java,   --skip:java          Do not enforce Java version checks
     -s:uid,    --skip:uid           Do not enforce UID=1000 for Docker
     -h,        --help               Show this help
     -d,        --debug              Use debug mode (prints command line options + app server debug)
     run                             Starts Che application server in current console
     start                           Starts Che application server in new console
     stop                            Stops Che application server

The -r flag sets the DOCKER_MACHINE_HOST system environment variable. Set this to the IP address of the node
that is running your Docker daemon. Only necessary to set this if on Linux and your browser clients are not
localhost, ie they are remote. This property automatically set for Che on Windows and Mac."

  # Command line parameters
  USE_DOCKER=false
  CHE_DOCKER_TAG=latest
  CHE_PORT=8080
  CHE_IP=
  USE_HELP=false
  CHE_SERVER_ACTION=run
  VM=${CHE_DOCKER_MACHINE_NAME:-default}
  CONTAINER=${CHE_CONTAINER_NAME:-che}
  USE_DEBUG=false
  SKIP_PRINT_CLIENT=false
  SKIP_DOCKER_UID=false
  SKIP_JAVA_VERSION=false
  LAUNCH_REGISTRY=false

  # Sets value of operating system
  WIN=false
  MAC=false
  LINUX=false
}

usage () {
  echo "${USAGE}"
}

error_exit () {
  echo
  echo "!!!"
  echo "!!! ${1}"
  echo "!!!"
  echo "${WRONG} ${CHE_VARIABLES} ${USAGE}"
  JUMP_TO_END=true
}

parse_command_line () {

  for command_line_option in "$@"
  do
  case ${command_line_option} in
    -i|--image)
      USE_DOCKER=true
    ;;
    -g|--registry)
      LAUNCH_REGISTRY=true
    ;;
    -i:*|--image:*)
      USE_DOCKER=true
      if [ "${command_line_option#*:}" != "" ]; then
        CHE_DOCKER_TAG="${command_line_option#*:}"
      fi
    ;;
    -c:*|--container:*)
      if [ "${command_line_option#*:}" != "" ]; then
        CONTAINER="${command_line_option#*:}"
      fi
    ;;
    -p:*|--port:*)
      if [ "${command_line_option#*:}" != "" ]; then
        CHE_PORT="${command_line_option#*:}"
      fi
    ;;
    -r:*|--remote:*)
      if [ "${command_line_option#*:}" != "" ]; then
        CHE_IP="${command_line_option#*:}"
      fi
    ;;
    -m:*|--machine:*)
      if [ "${command_line_option#*:}" != "" ]; then
        VM="${command_line_option#*:}"
      fi
    ;;
    -s:*|--skip:*)
      if [ "${command_line_option#*:}" != "" ]; then
        case "${command_line_option#*:}" in
          client)
            SKIP_PRINT_CLIENT=true
          ;;
          uid)
            SKIP_DOCKER_UID=true
          ;;
          java)
            SKIP_JAVA_VERSION=true
          ;;
        esac
      fi
    ;;
    -h|--help)
      USE_HELP=true
      usage
      return
    ;;
    -d|--debug)
      USE_DEBUG=true
    ;;
    start|stop|run)
      CHE_SERVER_ACTION=${command_line_option}
    ;;
    *)
      # unknown option
      error_exit "You passed an unknown command line option."
    ;;
  esac
  done

  if ${USE_DEBUG}; then
    echo "USE_DOCKER: ${USE_DOCKER}"
    echo "CHE_DOCKER_TAG: ${CHE_DOCKER_TAG}"
    echo "CHE_PORT: ${CHE_PORT}"
    echo "CHE_IP: \"${CHE_IP}\""
    echo "CHE_DOCKER_MACHINE: ${VM}"
    echo "LAUNCH_REGISTRY: ${LAUNCH_REGISTRY}"
    echo "SKIP_PRINT_CLIENT: ${SKIP_PRINT_CLIENT}"
    echo "SKIP_DOCKER_UID: ${SKIP_DOCKER_UID}"
    echo "SKIP_JAVA_VERSION: ${SKIP_JAVA_VERSION}"
    echo "USE_HELP: ${USE_HELP}"
    echo "CHE_SERVER_ACTION: ${CHE_SERVER_ACTION}"
    echo "USE_DEBUG: ${USE_DEBUG}"
  fi
}

determine_os () {
  # Set OS.  Mac & Windows require VirtualBox and docker-machine.

  if [[ "${OSTYPE}" == "linux"* ]]; then
    # Linux
    LINUX=true
  elif [[ "${OSTYPE}" == "darwin"* ]]; then
    # Mac OSX
    MAC=true
  elif [[ "${OSTYPE}" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    WIN=true
  elif [[ "${OSTYPE}" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    WIN=true
  elif [[ "${OSTYPE}" == "win32" ]]; then
    # I'm not sure this can happen.
    WIN=true
  elif [[ "${OSTYPE}" == "freebsd"* ]]; then
    # FreeBSD
    LINUX=true
  else
    error_exit "We could not detect your operating system. Che is unlikely to work properly."
  fi

}

set_environment_variables () {
  ### Set the value of derived environment variables.
  ### Use values set by user, unless they are broken, then fix them
  # The base directory of Che
  if [ -z "${CHE_HOME}" ]; then
    if [ "${WIN}" == "true" ]; then
      export CHE_HOME="${CHE_WINDOWS_SHORT_DIR}"
    else 
      export CHE_HOME="$(dirname "$(cd "$(dirname "${0}")" && pwd -P)")"
    fi
  fi

  if [[ "${CHE_IP}" != "" ]]; then
    export DOCKER_MACHINE_HOST="${CHE_IP}"
  fi

  # Convert Tomcat environment variables to POSIX format.
  if [[ "${JAVA_HOME}" == *":"* ]]
  then
    JAVA_HOME=$(echo /"${JAVA_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  if [[ "${CHE_HOME}" == *":"* ]]
  then
    CHE_HOME=$(echo /"${CHE_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  # Che configuration directory - where .properties files can be placed by user
  if [ -z "${CHE_LOCAL_CONF_DIR}" ]; then
    export CHE_LOCAL_CONF_DIR="${CHE_HOME}/conf/"
  fi

  # Sets the location of the application server and its executables
  export CATALINA_HOME="${CHE_HOME}"/tomcat

  # Convert windows path name to POSIX
  if [[ "${CATALINA_HOME}" == *":"* ]]
  then
    CATALINA_HOME=$(echo /"${CATALINA_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  export CATALINA_BASE="${CHE_HOME}"/tomcat
  export ASSEMBLY_BIN_DIR="${CATALINA_HOME}"/bin

  # Global logs directory
  if [ -z "${CHE_LOGS_DIR}" ]; then
    export CHE_LOGS_DIR="${CATALINA_HOME}/logs/"
  fi

}

get_docker_ready () {
  # Create absolute file names for docker and docker-machine
  # DOCKER_TOOLBOX_INSTALL_PATH set globally by Docker Toolbox installer
  if [ "${WIN}" == "true" ]; then
    if [ ! -z "${DOCKER_TOOLBOX_INSTALL_PATH}" ]; then
      export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker-machine.exe
      export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker.exe
    else
      error_exit "DOCKER_TOOLBOX_INSTALL_PATH environment variable not set. Add it or rerun Docker Toolbox installation."
      return
    fi
  elif [ "${MAC}" == "true" ]; then
    export DOCKER_MACHINE=/usr/local/bin/docker-machine
    export DOCKER=/usr/local/bin/docker
  elif [ "${LINUX}" == "true" ]; then
    export DOCKER_MACHINE=
    export DOCKER=/usr/bin/docker
  fi

  ### If Windows or Mac, launch docker-machine, if necessary
  if [ "${WIN}" == "true" ] || [ "${MAC}" == "true" ]; then
    # Path to run VirtualBox on the command line - used for creating VMs
    if [ ! -z "${VBOX_MSI_INSTALL_PATH}" ]; then
      VBOXMANAGE=${VBOX_MSI_INSTALL_PATH}VBoxManage.exe
    else
      VBOXMANAGE=/usr/local/bin/VBoxManage
    fi

    if [ ! -f "${DOCKER_MACHINE}" ]; then
      error_exit "Could not find docker-machine executable. Win: DOCKER_TOOLBOX_INSTALL_PATH env variable not set. Add it or rerun Docker Toolbox installation. Mac: Expected docker-machine at /usr/local/bin/docker-machine."
      return
    fi

    if [ ! -f "${VBOXMANAGE}" ]; then
      error_exit "Could not find VirtualBox. Win: VBOX_MSI_INSTALL_PATH env variable not set. Add it or rerun Docker Toolbox installation. Mac: Expected Virtual Box at /usr/local/bin/VBoxManage."
      return
    fi

    # Test to see if the VM we need is already running
    # Added || true to not fail due to set -e
    "${VBOXMANAGE}" showvminfo ${VM} &> /dev/null || VM_EXISTS_CODE=$? || true

    if [ "${VM_EXISTS_CODE}" == "1" ]; then
      echo "Could not find an existing docker machine."
      echo -e "Creating docker machine named ${GREEN}${VM}${NC}... Please be patient, this takes a couple minutes the first time."
      "${DOCKER_MACHINE}" rm -f ${VM} &> /dev/null || true
      rm -rf ~/.docker/machine/machines/${VM}
      "${DOCKER_MACHINE}" create -d virtualbox --virtualbox-host-dns-resolver ${VM} &> /dev/null || true

      # Seems that sometimes you have to regenerate certs even when creating new machine on windows

      echo -e "Successfully started docker machine named ${GREEN}${VM}${NC}..."

    else
      echo -e "Docker machine named ${GREEN}${VM}${NC} already exists..."
    fi

    VM_STATUS=$("${DOCKER_MACHINE}" status ${VM} 2>&1)

    if [ "${VM_STATUS}" != "Running" ]; then
      echo -e "Docker machine named ${GREEN}${VM}${NC} is not running."
      echo -e "Starting docker machine named ${GREEN}${VM}${NC}..."
      "${DOCKER_MACHINE}" start ${VM} || true
      yes | "${DOCKER_MACHINE}" regenerate-certs ${VM} &> /dev/null  || true
    fi

    echo -e "Setting environment variables for machine ${GREEN}${VM}${NC}..."
    eval "$("${DOCKER_MACHINE}" env --shell=bash ${VM})"
  fi
  ### End logic block to create / remove / start docker-machine VM

  # Test to ensure user is in Docker group with appropriate permissions
  if [ "${LINUX}" == "true" ]; then

    LINUX_USER=$(whoami)
    LINUX_GROUPS=$(groups "${LINUX_USER}")
    LINUX_UID=$(id -u "${LINUX_USER}")

    if [[ "${SKIP_DOCKER_UID}" == "false" ]] ; then
      if echo "${LINUX_GROUPS}" | grep "docker" &>/dev/null; then

        if [[ "${LINUX_UID}" != "1000" ]] ; then
          error_exit "This Linux user was launched with a UID != 1000. Che must run under UID 1000. See https://eclipse-che.readme.io/docs/usage#section-cannot-create-projects"
        fi

      else
        error_exit "This Linux user is not in docker group. See https://docs.docker.com/engine/installation/ubuntulinux/#create-a-docker-group"
      fi
    fi

  fi

  # Docker should be available, either in a VM or natively.
  # Test to see if docker binary is installed
  if [ ! -f "${DOCKER}" ]; then
    error_exit "Could not find Docker client. Expected at Windows: %DOCKER_TOOLBOX_INSTALL_PATH%\\docker.exe, Mac: /usr/local/bin/docker, Linux: /usr/bin/docker."
    return
  fi

  # Test to see that docker command works
  "${DOCKER}" &> /dev/null || DOCKER_EXISTS=$? || true

  if [ "${DOCKER_EXISTS}" == "1" ]; then
    error_exit "We found the 'docker' binary, but running 'docker' failed. Is a docker symlink broken?"
    return
  fi

  # Test to verify that docker can reach the VM
  "${DOCKER}" ps &> /dev/null || DOCKER_VM_REACHABLE=$? || true

  if [ "${DOCKER_VM_REACHABLE}" == "1" ]; then
    error_exit "Running 'docker' succeeded, but 'docker ps' failed. This usually means that docker cannot reach its daemon."
    return
  fi

  if [ "${WIN}" == "true" ] || [ "${MAC}" == "true" ]; then
    echo -e "${BLUE}Docker${NC} is configured to use vbox docker-machine named ${GREEN}${VM}${NC} with IP ${GREEN}$("${DOCKER_MACHINE}" ip ${VM})${NC}..."
  else
    echo "Docker is natively installed and reachable..."
  fi
}

# Added || true to not fail due to set -e
# We set -o pipefail to cause failures in pipe processing, but not needed for this functional
strip_url () {
  # extract the protocol
  proto="`echo ${1} | grep '://' | sed -e's,^\(.*://\).*,\1,g'`" || true

  # remove the protocol
  url=`echo ${1} | sed -e s,${proto},,g` || true

  # extract the user and password (if any)
  userpass=`echo ${url} | grep @ | cut -d@ -f1` || true
  pass=`echo ${userpass} | grep : | cut -d: -f2` || true


  if [ -n "${pass}" ]; then
      user=`echo ${userpass} | grep : | cut -d: -f1` || true
  else
      user=${userpass}
  fi

  # extract the host and remove the port
  hostport=`echo ${url} | sed -e s,${userpass}@,,g | cut -d/ -f1` || true
  port=`echo ${hostport} | grep : | cut -d: -f2` || true
  if [ -n "${port}" ]; then
      host=`echo ${hostport} | grep : | cut -d: -f1` || true
  else
      host=${hostport}
  fi

  # extract the path (if any)
  path="`echo ${url} | grep / | cut -d/ -f2-`" || true

}

print_client_connect () {
  if [ "${USE_DOCKER}" == "false" ]; then
  	HOST_PRINT_VALUE="localhost"
  else
  	HOST_PRINT_VALUE=${host}
  fi
      echo "
############## HOW TO CONNECT YOUR CHE CLIENT ###############
After Che server has booted, you can connect your clients by:
1. Open browser to http://${HOST_PRINT_VALUE}:${CHE_PORT}, or:
2. Open native chromium app.
#############################################################
"
}

call_catalina () {

  # Test to see that Che application server is where we expect it to be
  if [ ! -d "${ASSEMBLY_BIN_DIR}" ]; then
    error_exit "Could not find Che's application server."
    return
  fi

  if [ -z "${JAVA_HOME}" ]; then
    error_exit "JAVA_HOME is not set. Please set to directory of JVM or JRE."
    return
  fi


  # Test to see that Java is installed and working
  "${JAVA_HOME}"/bin/java &>/dev/null || JAVA_EXIT=$? || true
  if [ "${JAVA_EXIT}" != "1" ]; then
    error_exit "We could not find a working Java JVM. java command fails."
    return
  fi

  if [[ "${SKIP_JAVA_VERSION}" == false ]]; then
    # Che requires Java version 1.8 or higher.
	JAVA_VERSION=$("${JAVA_HOME}/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
	if [[  -z "${JAVA_VERSION}" || "${JAVA_VERSION}" < "1.8" ]]; then
	  error_exit "Che requires Java version 1.8 or higher. We found a ${JAVA_VERSION}."
	  return
	fi
  fi

  ### Cannot add this in setenv.sh.
  ### We do the port mapping here, and this gets inserted into server.xml when tomcat boots
  export JAVA_OPTS="${JAVA_OPTS} -Dport.http=${CHE_PORT}"
  export SERVER_PORT=${CHE_PORT}

  # Launch the Che application server, passing in command line parameters
  if [[ "${USE_DEBUG}" == true ]]; then
    "${ASSEMBLY_BIN_DIR}"/catalina.sh jpda ${CHE_SERVER_ACTION}
  else
    "${ASSEMBLY_BIN_DIR}"/catalina.sh ${CHE_SERVER_ACTION}
  fi
}

stop_che_server () {

  if ! ${USE_DOCKER}; then
    echo -e "Stopping Che server running on localhost:${CHE_PORT}"
    call_catalina >/dev/null 2>&1
  else
    echo -e "Stopping Che server running in docker container named ${GREEN}${CONTAINER}${NC}..."
    DOCKER_EXEC="//home/user/che/bin/che.sh stop"
    "${DOCKER}" exec ${CONTAINER} ${DOCKER_EXEC} || DOCKER_EXIT=$? || true

    echo -e "Stopping docker container named ${GREEN}${CONTAINER}${NC}...."
    "${DOCKER}" stop --time=10 ${CONTAINER} &>/dev/null 2>&1 || DOCKER_EXIT=$? || true
  fi
  return
}

kill_and_launch_docker_che () {

  echo -e "A Docker container named ${GREEN}${CONTAINER}${NC} does not exist or duplicate conflict was discovered."
  echo -e "Cleaning up any zombie containers named ${GREEN}${CONTAINER}${NC}..."

  # Force remove any lingering zombie containers with the name ${CONTAINER}
  "${DOCKER}" rm -f ${CONTAINER} &> /dev/null || true

  echo -e "Pulling the Docker image labeled ${GREEN}codenvy/che:${CHE_DOCKER_TAG}${NC}..."
  "${DOCKER}" pull codenvy/che:${CHE_DOCKER_TAG} || true

  echo -e "Launching a new Docker container named ${GREEN}${CONTAINER}${NC} from image ${GREEN}codenvy/che:${CHE_DOCKER_TAG}${NC}..."

  if ${WIN} || ${MAC} ; then
    DOCKER_PRINT_VALUE=${host}
  else
    DOCKER_PRINT_VALUE=${DOCKER_MACHINE_HOST}
  fi

  if ${USE_DEBUG}; then
	  set -x
  fi

  # IDEX-4266 - Change launching of docker to avoid using dind
  "${DOCKER}" run -v //var/run/docker.sock:/var/run/docker.sock \
  -v //home/user/che/lib:/home/user/che/lib-copy \
  -v //home/user/che/workspaces:/home/user/che/workspaces \
  -v //home/user/che/tomcat/temp/local-storage:/home/user/che/tomcat/temp/local-storage \
  -e DOCKER_MACHINE_HOST=${DOCKER_PRINT_VALUE} --name ${CONTAINER} -d -p ${CHE_PORT}:${CHE_PORT} --net=host codenvy/che:${CHE_DOCKER_TAG} \
  bash -c "tail -f /dev/null" || DOCKER_EXIT=$? || true 
  set +x
}

kill_and_launch_docker_registry () {
  echo -e "A Docker container named ${GREEN}registry${NC} does not exist or duplicate conflict was discovered."
  echo -e "Launching a new Docker container named ${GREEN}registry${NC} from image ${GREEN}registry:2${NC}."
  "${DOCKER}" rm -f registry &> /dev/null || true
  "${DOCKER}" run -d -p 5000:5000 --restart=always --name registry registry:2
  echo
}

launch_docker_registry () {

    echo "Launching a Docker registry for workspace snapshots."

    CREATE_NEW_CONTAINER=false

    # Check to see if the registry docker was not properly shut down
    "${DOCKER}" inspect registry &> /dev/null || DOCKER_INSPECT_EXIT=$? || true
    if [ "${DOCKER_INSPECT_EXIT}" != "1" ]; then

      # Existing container running registry is found.  Let's start it.
      echo -e "Found a registry container named ${GREEN}registry${NC}. Attempting restart."
      "${DOCKER}" start registry &>/dev/null || DOCKER_EXIT=$? || true

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

launch_che_server () {

  # Set host variable to the hostname that client should connect to.
  if ${WIN} || ${MAC} ; then
    strip_url ${DOCKER_HOST}
  else
    host=localhost
  fi

  if ! ${SKIP_PRINT_CLIENT}; then
    print_client_connect
  fi

  if ${LAUNCH_REGISTRY} ; then
    # Export the value of host here
    # Will be used on Che properties to set the location of registry
    export CHE_REGISTRY_HOST="${host}"
    launch_docker_registry

  else
    export CHE_REGISTRY_HOST="localhost"
  fi

  if ! ${USE_DOCKER}; then

    #########################################
    # Launch Che natively as a tomcat server
    call_catalina

  # Launch Che as a docker image
  else

    echo -e "Starting Che server in docker container named ${GREEN}${CONTAINER}${NC}."

    CREATE_NEW_CONTAINER=false

    # Check to see if the Che docker was not properly shut down
    "${DOCKER}" inspect ${CONTAINER} &> /dev/null || DOCKER_INSPECT_EXIT=$? || true
    if [ "${DOCKER_INSPECT_EXIT}" == "1" ]; then

      # No existing Che container found, we need to create a new one.
      CREATE_NEW_CONTAINER=true

    else

      # Existing container found.  Let's start it.
      echo -e "Found a container named ${GREEN}${CONTAINER}${NC}. Attempting restart."
      "${DOCKER}" start ${CONTAINER} &>/dev/null || DOCKER_EXIT=$? || true

      if [ "${DOCKER_EXIT}" == "1" ]; then

        # Existing container found, but could not start it properly.
        echo "Initial start of docker container failed... Attempting docker restart and exec."
        "${DOCKER}" exec ${CONTAINER} bash -c "true && sudo service docker start && "`
                                              `"//home/user/che/bin/che.sh "-p:${CHE_PORT}" "`
                                              `"--skip:client "${DEBUG_PRINT_VALUE}" "${CHE_SERVER_ACTION}"" || DOCKER_EXIT=$? || true   

        if [ "${DOCKER_EXIT}" == "1" ]; then
          # If we get to this point and Docker is still failing, then we will destroy the container entirely
          CREATE_NEW_CONTAINER=true
        fi

      else 

      	# Existing container found, and it was started properly.
        echo -e "Successful restart of container named ${GREEN}${CONTAINER}${NC}. Restarting Che server..."
        "${DOCKER}" exec ${CONTAINER} bash -c "//home/user/che/bin/che.sh "-p:${CHE_PORT}" "`
                                             `"--skip:client "${DEBUG_PRINT_VALUE}" "${CHE_SERVER_ACTION}"" || DOCKER_EXIT=$? || true   
        echo
        # Do not attempt additional docker exec command if we are restarting existing container
        return
      fi
    fi  

    if ${CREATE_NEW_CONTAINER} ; then

      # Container in bad state or not found, kill and launch new container.
      kill_and_launch_docker_che

    fi

    echo -e "Docker container named ${GREEN}${CONTAINER}${NC} successfully started."
    echo -e "Launching Che in a container named ${GREEN}${CONTAINER}${NC}..."

    # Launching tomcat with the start option in a docker container does not run successfully
    # because main process of container exits after starting Che as child process

    if [[ "${USE_DEBUG}" == true ]]; then
   	  DEBUG_PRINT_VALUE=--debug
	else
	  DEBUG_PRINT_VALUE=
	fi

	if ${USE_DEBUG}; then
	  set -x
	fi

    "${DOCKER}" exec -i ${CONTAINER} bash -c "sudo rm -rf /home/user/che/lib-copy/* && "`
                                            `"mkdir -p /home/user/che/lib-copy/ && "`
                                            `"sudo chown -R user:user /home/user && "`
                                            `"cp -rf /home/user/che/lib/* /home/user/che/lib-copy && "`
                                            `"sudo sed -i 's/random/urandom/g' /opt/jre1.8.0_65/lib/security/java.security && "`
                                            `"cd /home/user/che/bin/ && ./che.sh "-p:${CHE_PORT}" "`
                                            `"--skip:client "${DEBUG_PRINT_VALUE}" "${CHE_SERVER_ACTION}"" || DOCKER_EXIT=$? || true
	if ${USE_DEBUG}; then
	  set +x
	fi
	return
  fi
}

init_global_variables
parse_command_line "$@"
determine_os

if [ "${USE_HELP}" == "false" ] && [ "${JUMP_TO_END}" == "false" ]; then

  set_environment_variables

  if ${WIN} ; then
    # Prep windows
    # Check to see if %userprofile%\AppData\Local\che exists.
    # Create directory if it doesn't exist
    echo
    echo "#############################################################"
    echo "On Windows, Che projects can only reside in %userprofile% due"
    echo "to limitations of Docker. On this computer, %userprofile% is "
    echo -e "${GREEN}${USERPROFILE}${NC}"
    echo "#############################################################"
    echo

  fi

  ### Variables are all set.  Get Docker ready
  get_docker_ready

  ### Launch or shut down Che server
  if [ "${JUMP_TO_END}" == "false" ]; then
    if [ "${CHE_SERVER_ACTION}" == "stop" ]; then
      stop_che_server
    else
      launch_che_server
    fi
  fi
fi
