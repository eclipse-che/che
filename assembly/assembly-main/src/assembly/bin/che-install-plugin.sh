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

  USAGE="
Usage: 
  che-install-plugin [OPTIONS]         
     -a            --assembly          Creates new distributable Che assembly with your plugins 
     -s:deps,      --skip:deps         Skips automatic injection of POM dependencies of your plugins 
     -s:maven,     --skip:maven        Skips running maven to inject your plugins into local repository
     -s:update,    --skip:update       Skips updating this assembly with new packages; leaves them in /temp build dir 
     -s:wsagent,   --skip:wsagent      Skips creating new ws agent 
     -s:wsmaster,  --skip:wsmaster     Skips creating new ws master, which contains IDE & ws manager
     -d,           --debug             Additional verbose logging for this program
     -h,           --help              This help message
"

  # Sets value of operating system
  WIN=false
  MAC=false
  LINUX=false


  USE_DEBUG=false
  USE_HELP=false
  SKIP_MAVEN=false
  SKIP_DEPENDENCIES=false
  SKIP_UPDATE=false
  SKIP_WSMASTER=false
  SKIP_WSAGENT=false
  ASSEMBLY=false
}

function usage {
  echo "$USAGE"
}

function error_exit {
  echo
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "$1"
  echo "$WRONG $USAGE"
  USE_HELP=true
}

function parse_command_line {

  for command_line_option in "$@"
  do
    case $command_line_option in
      -a|--assembly)
        ASSEMBLY=true
      ;;
      -s:*|--skip:*)
        if [ "${command_line_option#*:}" != "" ]; then
          case "${command_line_option#*:}" in
            maven)
              SKIP_MAVEN=true
            ;;
            deps)
              SKIP_DEPENDENCIES=true
            ;;
            update)
              SKIP_UPDATE=true
            ;;
            wsagent)
              SKIP_WSAGENT=true
            ;;
            wsmaster)
              SKIP_WSMASTER=true
            ;;
          esac
        fi
      ;;
      -d|--debug)
        USE_DEBUG=true
      ;;
      -h|--help)
        USE_HELP=true
        usage
      ;;
      *)
        # unknown option
        error_exit "!!! You passed an unknown command line option."
      ;;
    esac
  done

  if $USE_DEBUG; then
    echo "ASSEMBLY: ${ASSEMBLY}"
    echo "SKIP_MAVEN: ${SKIP_MAVEN}"
    echo "SKIP_DEPENDENCIES: ${SKIP_DEPENDENCIES}"
    echo "SKIP_UPDATE: ${SKIP_UPDATE}"
    echo "SKIP_WSMASTER: ${SKIP_WSMASTER}"
    echo "SKIP_WSAGENT: ${SKIP_WSAGENT}"
    echo "USE_HELP: ${USE_HELP}"
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

function set_environment_variables {
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

  if [ "${WIN}" == "true" ] && [ ! -z "${JAVA_HOME}" ]; then
    # che-497: Determine windows short directory name in bash
    export JAVA_HOME=`(cygpath -u $(cygpath -w --short-name "${JAVA_HOME}"))` 
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

  # Where 3rd party extensions are staged
  PLUGIN_DIR="${CHE_HOME}/plugins"
  SDK_DIR="${CHE_HOME}/sdk"

  PLUGIN_IDE_DIR="${PLUGIN_DIR}/ide"
  PLUGIN_WSAGENT_DIR="${PLUGIN_DIR}/ws-agent"
  
  # The IDE project that will be built with the extension
  PLUGIN_IDE_WAR_DIR="${SDK_DIR}/assembly-ide-war"

  # The machine web app project that will be built with the extension
  PLUGIN_WSAGENT_WAR_DIR="${SDK_DIR}/assembly-wsagent-war"

  # Creates the ws-agent.tar.gz artifact for a Che assembly, which packages Tomcat + machine war into single package
  PLUGIN_WSAGENT_SERVER_DIR="${SDK_DIR}/assembly-wsagent-server"

  # Generates a new Che assembly that contains new IDE, ws-master, and ws-agent.
  PLUGIN_ASSEMBLY_DIR="${SDK_DIR}/assembly-main"

  if $USE_DEBUG; then
    echo "CHE_HOME                  = " $CHE_HOME
    echo "JAVA_HOME                 = " $JAVA_HOME
    echo "PLUGIN_DIR                = " $PLUGIN_DIR
    echo "SDK_DIR                   = " $SDK_DIR
    echo "PLUGIN_IDE_DIR            = " $PLUGIN_IDE_DIR
    echo "PLUGIN_WSAGENT_DIR        = " $PLUGIN_WSAGENT_DIR
    echo "PLUGIN_IDE_WAR_DIR        = " $PLUGIN_IDE_WAR_DIR
    echo "PLUGIN_WSAGENT_WAR_DIR    = " $PLUGIN_WSAGENT_WAR_DIR
    echo "PLUGIN_WSAGENT_SERVER_DIR = " $PLUGIN_WSAGENT_SERVER_DIR
    echo "PLUGIN_ASSEMBLY_DIR       = " $PLUGIN_ASSEMBLY_DIR
  fi
}

function echo_stage {

    echo
    echo "#####################################################################"
    echo "$1"
    echo "#####################################################################"
    echo

}

init_global_variables
parse_command_line "$@"
determine_os
set_environment_variables

if [ "${USE_HELP}" == "false" ]; then



  if [ "${SKIP_MAVEN}" == "false" ]; then
    echo_stage "CHE SDK: Installing each extension into local maven repository"

  # Install every 3rd-party extension into local Maven repository
    for file in $PLUGIN_DIR/*.jar
    do
      if [ -f $file ]; then
        cp $file $PLUGIN_IDE_DIR
        cp $file $PLUGIN_CHE_DIR
        cp $file $PLUGIN_WORKSPACE_DIR
        
        if [ "${SKIP_MAVEN}" == "false" ]; then
          mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=$file
        fi
      fi
    done
  fi

  if [ "${SKIP_DEPENDENCIES}" == "false" ]; then

    if [ "${SKIP_WSMASTER}" == "false" ]; then

      echo_stage "CHE SDK: Adding IDE extensions as dependencies"

      # Performs dependency injection of your plug-ins into che ide.war pom.xml & GWT module
      java -cp "${CHE_HOME}/sdk/che-plugin-sdk-tools.jar":`
              `"${CHE_HOME}/sdk/che-plugin-sdk-logger.jar":`
              `"${CHE_HOME}/sdk/che-plugin-sdk-logger-core.jar" `
              `org.eclipse.che.ide.sdk.tools.InstallExtension --extDir="${PLUGIN_IDE_DIR}" `
              `--extResourcesDir="${PLUGIN_IDE_WAR_DIR}"
    fi

    if [ "${SKIP_WSAGENT}" == "false" ]; then
  
      echo_stage "CHE SDK: Adding extensions as dependencies to ws-agent"
  
      # Performs dependency injection of your plug-ins into che ide.war pom.xml & GWT module
      java -cp "${CHE_HOME}/sdk/che-plugin-sdk-tools.jar":`
              `"${CHE_HOME}/sdk/che-plugin-sdk-logger.jar":`
              `"${CHE_HOME}/sdk/che-plugin-sdk-logger-core.jar" `
              `org.eclipse.che.ide.sdk.tools.InstallExtension --extDir="${PLUGIN_WSAGENT_DIR}" `
              `--extResourcesDir="${PLUGIN_WSAGENT_WAR_DIR}"

    fi
  fi

  if [ "${SKIP_WSMASTER}" == "false" ]; then

    echo_stage "CHE SDK: Compiling everything into new IDE. ~5 minutes."
      
    # Re-build the che web application with extensions from ide/ and che/ directories included. This artifact is deployed into Che server.
    cd "${PLUGIN_IDE_WAR_DIR}/temp"
    mvn sortpom:sort
    mvn -Denforcer.skip=true clean install -Dskip-validate-sources=true
    cd "${CHE_HOME}"

    if [ "${SKIP_UPDATE}" == "false" ]; then
      cp -r "${PLUGIN_IDE_WAR_DIR}"/temp/target/*.war tomcat/webapps/ide.war
    fi
  fi 

  # Re-build the machine web application with custom extension included from workspace/ directories included. This artifact is packaged into ws-agent.tar.gz and deployed into workspace machine.
  if [ "${SKIP_WSAGENT}" == "false" ]; then

    echo_stage "CHE SDK: Compiling ws-agent plug-ins into new workspace agent."
      
    # Re-build the che web application with extensions from ide/ and che/ directories included. This artifact is deployed into Che server.
    cd "${PLUGIN_WSAGENT_WAR_DIR}/temp"
    mvn sortpom:sort
    mvn -Denforcer.skip=true clean install -Dskip-validate-sources=true

    echo_stage "CHE SDK: Packaging ws-agent web app and Tomcat into ws-agent.tar.gz."

    cd "${PLUGIN_WSAGENT_SERVER_DIR}"
    mvn -Denforcer.skip=true clean install -Dskip-validate-sources=true
    cd "${CHE_HOME}"


    if [ "${SKIP_UPDATE}" == "false" ]; then
      cp -r "${PLUGIN_WSAGENT_SERVER_DIR}"/target/*.tar.gz lib/ws-agent.tar.gz
    fi
  fi  

  if [ "${ASSEMBLY}" == "true" ]; then
    echo_stage "CHE SDK: Creating new distributable Che assembly in /plugins/assembly"

    cd "${PLUGIN_ASSEMBLY_DIR}"
    mvn -Denforcer.skip=true clean install -Dskip-validate-sources=true

    cd "${CHE_HOME}"
    rm -rf plugins/assembly
    mkdir plugins/assembly
    cp -r --target-directory=plugins/assembly "${PLUGIN_ASSEMBLY_DIR}"/target/eclipse-che*
  fi  


  echo 
  echo "#####################################################################"
  echo "Good Job! Your build has completed."
  echo "#####################################################################"
  echo 

  if [ "${SKIP_WSMASTER}" == "false" ] && [ "${SKIP_UPDATE}" == "true" ]; then
    echo "New Workspace Master Web App: 
    ${PLUGIN_IDE_WAR_DIR}/temp/target/ide.war"
  fi

  if [ "${SKIP_WSMASTER}" == "false" ] && [ "${SKIP_UPDATE}" == "false" ]; then
    echo "New Workspace Master Web App: 
    ${CHE_HOME}/tomcat/webapps/ide.war"
  fi

  if [ "${SKIP_WSAGENT}" == "false" ] && [ "${SKIP_UPDATE}" == "true" ]; then
    echo "New Workspace Agent: 
    ${PLUGIN_WSAGENT_SERVER_DIR}/target/*.tar.gz"
  fi

  if [ "${SKIP_WSAGENT}" == "false" ] && [ "${SKIP_UPDATE}" == "false" ]; then
    echo "New Workspace Agent: 
    ${CHE_HOME}/lib/ws-agent.tar.gz"
  fi

  if [ "${ASSEMBLY}" == "true" ]; then
    echo "New Che Assembly 
    ${CHE_HOME}/plugins/assembly/"
  fi

  echo 

  if [ "${SKIP_UPDATE}" == "false" ]; then
    echo "You can start this Che assemby to see your plugins."
  fi
  if [ "${ASSEMBLY}" == "false" ]; then
    echo "You can start the new Che assembly to see your plugins."
  fi

fi
