#
# Copyright (c) 2012-2018 Red Hat, Inc.
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
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-XX:MinRAMPercentage=60.0 -XX:MaxRAMPercentage=90.0 -Djava.security.egd=file:/dev/./urandom"
# Check compatible JAVA_OPTS
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1 | sed 's/[^0-9]*//g')
if ([ "$JAVA_VERSION" -ge 11 ])
then
   echo "Incompatible JAVA_OPTS configured to use with Java 11. Reset to default"
   echo $JAVA_OPTS
   JAVA_OPTS="-XX:MinRAMPercentage=60.0 -XX:MaxRAMPercentage=90.0 -Djava.security.egd=file:/dev/./urandom"
else
   echo "Sorry. Not found."
fi

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
