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
  USAGE="
Usage:
  che [OPTIONS] [COMMAND]
     -v,        --vmware                Use the docker-machine VMware driver (instead of VirtualBox)
     -m:name,   --machine:name          For Win & Mac, sets the docker-machine VM name; default=che
     -a:driver, --machine-driver:driver For Win & Mac, specifies the docker-machine driver to use; default=vbox
     -p:port,   --port:port             Port that Che server will use for HTTP requests; default=8080
     -l:level,  --logs_level:level      Logging level. Possible values are the logback logging levels; default=INFO
     -r:ip,     --remote:ip             If Che clients are not localhost, set to IP address of Che server
     -h,        --help                  Show this help
     -d,        --debug                 Use debug mode (prints command line options + app server debug)

  Options when running Che natively:
     -b,        --blocking-entropy      Security: https://wiki.apache.org/tomcat/HowTo/FasterStartUp
     -g,        --registry              Launch Docker registry as a container (used for ws snapshots)
     -s:client, --skip:client           Do not print browser client connection information
     -s:java,   --skip:java             Do not enforce Java version checks
     -s:uid,    --skip:uid              Do not enforce UID=1000 for Docker

  Options to launch Che in a Docker container:
     -i,        --image                 (Deprecated) Launches Che within a Docker container using latest image
     -i:tag,    --image:tag             (Deprecated) Launches Che within a Docker container using specific version

  Commands:
     run                                (Default) Starts Che server with logging in current console
     start                              Starts Che server in new console
     stop                               Stops Che server

Docs: http://eclipse.org/che/getting-started.

If you are running Che as a server on a VM for multiple users, review the additional networking
configuration that control how clients, Che and workspaces initiate connections. See:
https://eclipse-che.readme.io/docs/networking."

  # Command line parameters
  USE_BLOCKING_ENTROPY=false
  USE_DOCKER=false
  USE_VMWARE=false
  CHE_DOCKER_TAG=latest
  CHE_PORT=8080
  CHE_LOGS_LEVEL=INFO
  CHE_IP=
  USE_HELP=false
  CHE_SERVER_ACTION=run
  COPY_LIB=false
  VM=${CHE_DOCKER_MACHINE_NAME:-che}
  MACHINE_DRIVER=virtualbox
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
  echo -e "!!! ${1}"
  echo "!!!"
  echo 
  JUMP_TO_END=true
}

parse_command_line () {

  for command_line_option in "$@"
  do
  case ${command_line_option} in
    -b|--blocking-entropy)
      USE_BLOCKING_ENTROPY=true
    ;;
    -c|--copy-lib)
      COPY_LIB=true
    ;;
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
    -l:*|--log_level:*)
      if [ "${command_line_option#*:}" != "" ]; then
        CHE_LOGS_LEVEL="${command_line_option#*:}"
      fi
    ;;
    -a:*|--machine-driver:*)
      if [ "${command_line_option#*:}" != "" ]; then
        case "${command_line_option#*:}" in
          vbox)
            MACHINE_DRIVER=virtualbox
          ;;
          fusion)
            MACHINE_DRIVER=fusion
          ;;
          *)
          # unsupported driver
          error_exit "Only vbox and fusion docker-machine drivers are supported."
          ;;
        esac
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
      CHE_LOGS_LEVEL=DEBUG
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
    echo "USE_BLOCKING_ENTROPY: ${USE_BLOCKING_ENTROPY}"
    echo "USE_DOCKER: ${USE_DOCKER}"
    echo "CHE_DOCKER_TAG: ${CHE_DOCKER_TAG}"
    echo "CHE_PORT: ${CHE_PORT}"
    echo "CHE_IP: \"${CHE_IP}\""
    echo "LOGGING_LEVEL: ${CHE_LOGS_LEVEL}"
    echo "CHE_DOCKER_MACHINE: ${VM}"
    echo "COPY_LIB: ${COPY_LIB}"
    echo "MACHINE_DRIVER: ${MACHINE_DRIVER}"
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
      # che-497: Determine windows short directory name in bash
      export CHE_HOME=`(cd "$( dirname "${BASH_SOURCE[0]}" )" && cmd //C 'FOR %i in (..) do @echo %~Si')`
    else
      export CHE_HOME="$(dirname "$(cd "$(dirname "${0}")" && pwd -P)")"
    fi
  fi

  # The environment variable is used by Che to set its IP address
  if [[ -n "${CHE_IP}" ]]; then
    export CHE_DOCKER_MACHINE_HOST="${CHE_IP}"
  fi

  #if [ "${WIN}" == "true" ] && [ ! -z "${JAVA_HOME}" ]; then
    # che-497: Determine windows short directory name in bash
    # export JAVA_HOME=`(cygpath -u $(cygpath -w --short-name "${JAVA_HOME}"))`
  #fi

  # Convert Tomcat environment variables to POSIX format.
  if [[ "${JAVA_HOME}" == *":"* ]]; then
    JAVA_HOME=$(echo /"${JAVA_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  if [[ "${CHE_HOME}" == *":"* ]]; then
    CHE_HOME=$(echo /"${CHE_HOME}" | sed  's|\\|/|g' | sed 's|:||g')
  fi

  if [[ "${CHE_HOME}" =~ \ |\' ]] && [[ "${WIN}" == "true" ]]; then
    echo "!!!"
    echo "!!! Ohhhhh boy."
    echo "!!! You are on Windows and installed Che into a directory that contains a space."
    echo "!!! Tomcat behaves badly because of this."
    echo "!!!"
    echo "!!! We attempted to work around this by converting your path to one without a space."
    echo "!!! However, it seems that the drive where Che is installed does not allow this."
    echo "!!! So we seem to be buggered."
    echo "!!!"
    echo "!!! You can fix this issue by installing Che into a directory without spaces in the name."
    echo "!!! Isn't Windows fun?  Long live William Shatner."
    echo "!!!"
    JUMP_TO_END=true
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
  export CHE_LOGS_LEVEL="${CHE_LOGS_LEVEL}"

  # Global logs directory
  if [ -z "${CHE_LOGS_DIR}" ]; then
    export CHE_LOGS_DIR="${CATALINA_HOME}/logs/"
  fi

}

get_docker_ready () {

  if [ "${WIN}" == "true" ]; then
    export DOCKER=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker.exe
  elif [ "${MAC}" == "true" ]; then
    export DOCKER=/usr/local/bin/docker
  elif [ "${LINUX}" == "true" ]; then
    export DOCKER=/usr/bin/docker
  fi

  # Test to ensure user is in Docker group with appropriate permissions
  if [ "${LINUX}" == "true" ]; then

    LINUX_USER=$(whoami)
    LINUX_GROUPS=$(groups "${LINUX_USER}")
    LINUX_UID=$(id -u "${LINUX_USER}")

    if [[ "${SKIP_DOCKER_UID}" == "false" ]] ; then
      if echo "${LINUX_GROUPS}" | grep "docker" &>/dev/null; then

        if [[ "${LINUX_UID}" != "1000" ]] ; then
          error_exit "This Linux user was launched with a UID != 1000. `
                     `Che must run under UID 1000. See https://eclipse-che.readme.io/docs/usage#section-cannot-create-projects"
        fi

      else
        error_exit "This Linux user is not in 'docker' group. `
                   `See https://docs.docker.com/engine/installation/ubuntulinux/#create-a-docker-group"
      fi
    fi

  fi

  # Docker should be available, either in a VM or natively.
  # Test to see if docker binary is installed
  if [ ! -f "${DOCKER}" ]; then
    error_exit "Could not find Docker client. `
               `Expected at Windows: %DOCKER_TOOLBOX_INSTALL_PATH%\\docker.exe, `
               `Mac: /usr/local/bin/docker, `
               `Linux: /usr/bin/docker."
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
    
    # If this is Mac or Windows and we get a failure, assume Docker is not installed.
    # Use Docker Machine to launch a VM where we can use Docker
    if [ "${WIN}" == "true" ] || [ "${MAC}" == "true" ]; then
      launch_docker_vm
    else

      # CHE-1202: Improve error messages in case of docker ps failure
      # Verify that /var/run/docker.sock has read / write permissions
      PERMS=$(stat -c %A /var/run/docker.sock)
      OWNER_READ=$(cut -c2 <(echo $PERMS))
      OWNER_WRITE=$(cut -c3 <(echo $PERMS))
      GROUP_READ=$(cut -c5 <(echo $PERMS))
      GROUP_WRITE=$(cut -c6 <(echo $PERMS))
      OTHER_READ=$(cut -c8 <(echo $PERMS))
      OTHER_WRITE=$(cut -c9 <(echo $PERMS))

      if [[ "$OWNER_READ" != "r" || "$OWNER_WRITE" != "w" || `
           `"$GROUP_READ" != "r" || "$GROUP_WRITE" != "w" || `
           `"$OTHER_READ" != "r" || "$OTHER_WRITE" != "w" ]]; then
        error_exit "Running 'docker' succeeded, but 'docker ps' failed. \n`
                   `The file /var/run/docker.sock does not have appropriate permissions. \n`
                   `OWNER READ:  ${OWNER_READ} \n`
                   `OWNER WRITE: ${OWNER_WRITE} \n`
                   `GROUP READ:  ${GROUP_READ} \n`
                   `GROUP WRITE: ${GROUP_WRITE} \n`
                   `OTHER READ:  ${OTHER_READ} \n`
                   `OTHER WRITE: ${OTHER_WRITE} \n`
                   `Run 'sudo chmod 666 /var/run/docker.sock' to give the right permissions."
        return
      fi

      # CHE-1202: Improve error messages in case of docker ps failure
      # Verify that docker client and server versions match
      DOCKER_SERVER_VERSION=$(docker version --format '{{.Server.Version}}')
      DOCKER_CLIENT_VERSION=$(docker version --format '{{.Client.Version}}')

      if [[ "$DOCKER_SERVER_VERSION" != "$DOCKER_CLIENT_VERSION" ]]; then
        error_exit "Running 'docker' succeeded, but 'docker ps' failed. \n`
                   `The docker client version does not match the docker server version. \n`
                   `DOCKER SERVER: ${DOCKER_SERVER_VERSION} \n`
                   `DOCKER CLIENT: ${DOCKER_CLIENT_VERSION} \n`
                   `This can occur if you are running Che as a container itself. \n`
                   `The Che container has an internal docker client that uses your host's docker server. \n` 
                   `Consider updating docker engine to have the versions match."
        return
      fi
      
      error_exit "Running 'docker' succeeded, but 'docker ps' failed. \n`
                 `/var/run/docker.sock is ok and your docker client and server have matching versions. \n`
                 `Run 'docker ps' and inspect the output for additional clues."
      return
    fi
  fi

  # Hidden parameter
  # Only used if running Che server inside a Docker container.
  # Copies Che libraries to a temporary directory which is mounted by the container to be reachable by external host.
  # Files must be copied otherwise host will overwrite them to blank.
  if [ "${COPY_LIB}" == "true" ]; then
    sudo chown -R user:user ${CHE_HOME}
    rm -rf ${CHE_HOME}/lib-copy/*
    mkdir -p ${CHE_HOME}/lib-copy
    cp -rf ${CHE_HOME}/lib/* ${CHE_HOME}/lib-copy

    export JAVA_OPTS="${JAVA_OPTS} -Dche.docker.che_host_network=bridge"
  fi 
}

launch_docker_vm () {
  # Create absolute file names for docker and docker-machine
  # DOCKER_TOOLBOX_INSTALL_PATH set globally by Docker Toolbox installer
  if [ "${WIN}" == "true" ]; then
    if [ ! -z "${DOCKER_TOOLBOX_INSTALL_PATH}" ]; then
      export DOCKER_MACHINE=${DOCKER_TOOLBOX_INSTALL_PATH}\\docker-machine.exe
    else
      error_exit "DOCKER_TOOLBOX_INSTALL_PATH environment variable not set. Add it or rerun Docker Toolbox installation."
      return
    fi
  else 
    export DOCKER_MACHINE=/usr/local/bin/docker-machine
  fi

  # Path to run VMware on the command line - used for creating VMs
  # TODO: add support for VMware Workstation
  if [ "${MAC}" == "true" ] && [ "${MACHINE_DRIVER}" == "fusion" ]; then
    VMRUN="/Applications/VMware Fusion.app/Contents/Library/vmrun"
    echo "$VMRUN"
    VM_CHECK_CMD="$("$VMRUN" list \| grep $VM)"
    DOCKER_MACHINE_DRIVER=vmwarefusion
    if [ ! -f "$VMRUN" ]; then
      error_exit "Could not find vmrun. Expected vmrun in $VMRUN. Check VMware Fusion installation."
      return
    fi
  else
    if [ ! -z "${VBOX_MSI_INSTALL_PATH}" ]; then
      # Convert this directory to its short form name on Windows
      if [ "${WIN}" == "true" ]; then
        export VBOX_MSI_INSTALL_PATH=`(cd "${VBOX_MSI_INSTALL_PATH}" && cmd //C 'FOR %i in (.) do @echo %~Si')`\\
    fi
      VBOXMANAGE="${VBOX_MSI_INSTALL_PATH}"VBoxManage.exe
    else
      VBOXMANAGE=/usr/local/bin/VBoxManage
    fi
    VM_CHECK_CMD="${VBOXMANAGE} showvminfo ${VM}"
    DOCKER_MACHINE_DRIVER=virtualbox
    DOCKER_MACHINE_DRIVER_OPTIONS='--virtualbox-host-dns-resolver --virtualbox-memory 2048 --virtualbox-cpu-count 2'
    if [ ! -f "${VBOXMANAGE}" ]; then
      error_exit "Could not find VirtualBox. Win: VBOX_MSI_INSTALL_PATH env variable not set. `
                 `Add it or rerun Docker Toolbox installation. Mac: Expected Virtual Box at /usr/local/bin/VBoxManage."
    return
    fi
  fi

  if [ ! -f "${DOCKER_MACHINE}" ]; then
    error_exit "Could not find docker-machine executable. Win: DOCKER_TOOLBOX_INSTALL_PATH env variable not set. `
               `Mac: Expected docker-machine at /usr/local/bin/docker-machine."
    return
  fi

  # Test to see if the VM we need is already running
  # Added || true to not fail due to set -e
  ${VM_CHECK_CMD} &> /dev/null || VM_EXISTS_CODE=$? || true
  if [ "${VM_EXISTS_CODE}" == "1" ]; then
    echo -e "Could not find an existing docker machine named ${GREEN}${VM}${NC}."
    echo -e "Creating docker machine named ${GREEN}${VM}${NC}... Be patient, this takes a couple minutes the first time."
    "${DOCKER_MACHINE}" rm -f ${VM} &> /dev/null || true
    rm -rf ~/.docker/machine/machines/${VM}
    "${DOCKER_MACHINE}" create -d $DOCKER_MACHINE_DRIVER $DOCKER_MACHINE_DRIVER_OPTIONS ${VM} &> /dev/null || true

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

  echo -e "${BLUE}Docker${NC} is configured to use docker-machine named `
          `${GREEN}${VM}${NC} with IP ${GREEN}$("${DOCKER_MACHINE}" ip ${VM})${NC}..."
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
  HOST_PRINT_VALUE=${host}
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
    error_exit "We could not find a working Java JVM. 'java' command fails."
    return
  fi

  if [[ "${SKIP_JAVA_VERSION}" == false ]]; then
    # Che requires Java version 1.8 or higher.
    JAVA_VERSION=$("${JAVA_HOME}"/bin/java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [  -z "${JAVA_VERSION}" ]; then
      error_exit "Failure running JAVA_HOME/bin/java -version. We received ${JAVA_VERSION}."
      return
    fi

    if [[ "${JAVA_VERSION}" < "1.8" ]]; then
      error_exit "Che requires Java version 1.8 or higher. We found ${JAVA_VERSION}."
      return
    fi
  fi

  ### Initialize default JVM arguments to run che
  if [[ "${USE_BLOCKING_ENTROPY}" == true ]]; then
    [ -z "${JAVA_OPTS}" ] && JAVA_OPTS="-Xms256m -Xmx1024m"
  else
    [ -z "${JAVA_OPTS}" ] && JAVA_OPTS="-Xms256m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
  fi

  ### Cannot add this in setenv.sh.
  ### We do the port mapping here, and this gets inserted into server.xml when tomcat boots
  export JAVA_OPTS="${JAVA_OPTS} -Dport.http=${CHE_PORT} -Dche.home=${CHE_HOME}"
  export SERVER_PORT=${CHE_PORT}

  # Launch the Che application server, passing in command line parameters
  if [[ "${USE_DEBUG}" == true ]]; then
    "${ASSEMBLY_BIN_DIR}"/catalina.sh jpda ${CHE_SERVER_ACTION}
  else
    "${ASSEMBLY_BIN_DIR}"/catalina.sh ${CHE_SERVER_ACTION}
  fi
}

stop_che_server () {

  echo -e "Stopping Che server running on localhost:${CHE_PORT}"
  call_catalina >/dev/null 2>&1
  return

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

  if [[ -n "${DOCKER_HOST}" ]]; then
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

  #########################################
  # Launch Che natively as a tomcat server
  call_catalina
}

init_global_variables
parse_command_line "$@"
determine_os

if [ "${USE_DOCKER}" == "true" ]; then
  error_exit "The '--image' option has been deprecated. Please use Che's 'docker run' syntax to run Che in a container."
fi 

if [ "${USE_HELP}" == "false" ] && [ "${JUMP_TO_END}" == "false" ]; then

  set_environment_variables

  if ${WIN} && [ "${JUMP_TO_END}" == "false" ]; then
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
  if [ "${JUMP_TO_END}" == "false" ]; then
    get_docker_ready
  fi

  ### Launch or shut down Che server
  if [ "${JUMP_TO_END}" == "false" ]; then
    if [ "${CHE_SERVER_ACTION}" == "stop" ]; then
      stop_che_server
    else
      launch_che_server
    fi
  fi
fi
