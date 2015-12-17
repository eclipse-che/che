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

[ -z "${CHE_HOME}" ] && export CHE_HOME="$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )" )"
#Global Conf dir
[ -z "${CHE_LOCAL_CONF_DIR}" ] && export CHE_LOCAL_CONF_DIR="${CHE_HOME}/conf/"

export CATALINA_HOME=${CHE_HOME}/tomcat
export CATALINA_BASE=${CHE_HOME}/tomcat
export ASSEMBLY_BIN_DIR=${CHE_HOME}/tomcat/bin

#Global logs dir
[ -z "${CHE_LOGS_DIR}" ] && export CHE_LOGS_DIR="${CATALINA_HOME}/logs/"

if [ ! -d "${ASSEMBLY_BIN_DIR}" ]; then
  echo "$(tput setaf 1)Could not find the app server binaries in Eclipse Che."$(tput sgr0)
  exit 1
fi

${ASSEMBLY_BIN_DIR}/catalina.sh "$*"