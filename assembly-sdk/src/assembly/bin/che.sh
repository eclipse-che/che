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

if [ -z "$CHE_HOME" ] 
then 
    export CHE_HOME="$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )" )"
fi

#Global Conf dir
[ -z "${CHE_LOCAL_CONF_DIR}" ]  && export CHE_LOCAL_CONF_DIR="${CHE_HOME}/conf/"

CATALINA_HOME=$CHE_HOME/tomcat
CATALINA_BASE=$CHE_HOME/tomcat
ASSEMBLY_BIN_DIR=$CHE_HOME/tomcat/bin

#Global logs dir
[ -z "${CHE_LOGS_DIR}" ]  && CHE_LOGS_DIR="${CATALINA_HOME}/logs/"


if [ ! -d "${ASSEMBLY_BIN_DIR}" ]
then
  echo "$(tput setaf 1)Could not find the app server binaries in Eclipse Che."$(tput sgr0)
  exit 1
fi

$ASSEMBLY_BIN_DIR/che.sh $*



