#
# Copyright (c) 2012-2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

#Global Conf dir
[ -z "${CHE_LOCAL_CONF_DIR}" ]  && CHE_LOCAL_CONF_DIR="${CATALINA_HOME}/conf/"

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

#Global LOGS DIR
[ -z "${CHE_LOGS_DIR}" ]  && CHE_LOGS_DIR="$CATALINA_HOME/logs"

[ -z "${CHE_LOGS_LEVEL}" ]  && CHE_LOGS_LEVEL="INFO"

[ -z "${JPDA_ADDRESS}" ]  && JPDA_ADDRESS="4403"

[ -z "${UMASK}" ] && UMASK="022"

#Tomcat options
[ -z "${CATALINA_OPTS}" ]  && CATALINA_OPTS="-Dcom.sun.management.jmxremote  \
                                             -Dcom.sun.management.jmxremote.ssl=false \
                                             -Dcom.sun.management.jmxremote.authenticate=false \
                                             -Dche.local.conf.dir=${CHE_LOCAL_CONF_DIR}"

#Class path
[ -z "${CLASSPATH}" ]  && CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar"


# On java9 runtime, enable activation and JAXB API
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1 | sed 's/[^0-9]*//g')
[ "$JAVA_VERSION" -ge 9 ] && JAVA_OPTS="$JAVA_OPTS --add-modules java.activation --add-modules java.xml.bind"

export JAVA_OPTS="$JAVA_OPTS  -Dche.logs.dir=${CHE_LOGS_DIR} -Dche.logs.level=${CHE_LOGS_LEVEL} -Djuli-logback.configurationFile=file:$CATALINA_HOME/conf/tomcat-logger.xml"

[ -z "${SERVER_PORT}" ]  && SERVER_PORT=8080
export SERVER_PORT
