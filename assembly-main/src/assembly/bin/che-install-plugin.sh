#!/bin/bash
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

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -o pipefail

# Run the finish function if exit signal initiated
trap exit SIGHUP SIGINT SIGTERM

function init_global_variables {

  # For coloring console output
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  NC='\033[0m'

  ### Define various error and usage messages
  WRONG="
Looks like something went wrong. Possible issues: 
"

  CHE_VARIABLES="
Che Environment Variables:
  (OPTIONAL) DOCKER_MACHINE_HOST                   ==> (Linux) Docker host IP - set if browser clients remote
  "

  USAGE="
Usage: 
  che [OPTIONS] [run | start | stop]
     -i,      --image        Launches Che within a Docker container using latest image
     -i:tag,  --image:tag    Launches Che within a Docker container using specific image tag

."

  # Sets value of operating system
  WIN=false
  MAC=false
  LINUX=false

  # Specifies the location of the directory that contains 3rd-party extensions
  EXT_DIR_REL_PATH="plugins"

  # Specifies the location of the directory that contains resource files to re-build Codenvy IDE
  EXT_RES_DIR_REL_PATH="sdk-resources"
  EXT_RES_WORK_DIR_REL_PATH="sdk-resources/temp"

}

function usage {
  echo "$USAGE"
}

function error_exit {
  echo
  echo "$1"
  echo "$WRONG $CHE_VARIABLES $USAGE"
}

function parse_command_line {

  for command_line_option in "$@"
  do
  case $command_line_option in
    -i|--image)
      USE_DOCKER=true
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
    -s|--suppress)
      PRINT_CLIENT_CONNECT=false
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
      error_exit "!!! You passed an unknown command line option."
    ;;
  esac
  done

  if $USE_DEBUG; then
    echo "USE_DOCKER: ${USE_DOCKER}"
    echo "CHE_DOCKER_TAG: ${CHE_DOCKER_TAG}"
    echo "CHE_PORT: ${CHE_PORT}"
    echo "CHE_IP: \"${CHE_IP}\""
    echo "CHE_DOCKER_MACHINE: ${VM}"
    echo "PRINT_CLIENT_CONNECT: ${PRINT_CLIENT_CONNECT}"
    echo "USE_HELP: ${USE_HELP}"
    echo "CHE_SERVER_ACTION: ${CHE_SERVER_ACTION}"
    echo "USE_DEBUG: ${USE_DEBUG}"
  fi
}

function set_environment_variables {
  ### Set the value of derived environment variables.
  ### Use values set by user, unless they are broken, then fix them
  # The base directory of Che
  if [ -z "${CHE_HOME}" ]; then
    export CHE_HOME="$(dirname "$(cd $(dirname ${0}) && pwd -P)")"
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


}


# Install every 3rd-party extension into local Maven repository
for file in $EXT_DIR_REL_PATH/*.jar
do
    if [ -f $file ]; then
        mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=$file
    fi
done

# Prepare to re-build Codenvy IDE
java -cp "sdk-tools/che-plugin-sdk-tools.jar" org.eclipse.che.ide.sdk.tools.InstallExtension --extDir=$EXT_DIR_REL_PATH --extResourcesDir=$EXT_RES_DIR_REL_PATH

# Re-build Codenvy IDE
cd $EXT_RES_WORK_DIR_REL_PATH
mvn clean package -Dskip-validate-sources=true
cd ../..
cp $EXT_RES_WORK_DIR_REL_PATH/target/*.war webapps/che.war
rm -rf webapps/che

echo Restart Codenvy IDE if it is currently running
