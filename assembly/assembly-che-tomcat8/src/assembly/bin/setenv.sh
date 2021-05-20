#
# Copyright (c) 2012-2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

#Global Conf dir
[ -z "${CHE_LOCAL_CONF_DIR}" ]  && CHE_LOCAL_CONF_DIR="${CATALINA_HOME}/conf/"

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-XX:MaxRAMPercentage=85.0 "

#Global LOGS DIR
[ -z "${CHE_LOGS_DIR}" ]  && CHE_LOGS_DIR="$CATALINA_HOME/logs"

[ -z "${CHE_LOGS_LEVEL}" ]  && CHE_LOGS_LEVEL="INFO"

[ -z "${JPDA_ADDRESS}" ]  && JPDA_ADDRESS="8000"

[ -z "${UMASK}" ] && UMASK="022"

#Tomcat options
[ -z "${CATALINA_OPTS}" ]  && CATALINA_OPTS="-Dche.local.conf.dir=${CHE_LOCAL_CONF_DIR}"

#Class path
[ -z "${CLASSPATH}" ]  && CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar"


export JAVA_OPTS="$JAVA_OPTS  -Dche.logs.dir=${CHE_LOGS_DIR} -Dche.logs.level=${CHE_LOGS_LEVEL} -Djuli-logback.configurationFile=file:$CATALINA_HOME/conf/tomcat-logger.xml"
