#
# CODENVY CONFIDENTIAL
# __________________
#
#  [2012] - [2015] Codenvy, S.A.
#  All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any.  The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

#Global Conf dir
[ -z "${CODENVY_LOCAL_CONF_DIR}" ]  && CODENVY_LOCAL_CONF_DIR="${CATALINA_HOME}/conf/"

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx1024m  -server"

#Global LOGS DIR
[ -z "${CODENVY_LOGS_DIR}" ]  && CODENVY_LOGS_DIR="$CATALINA_HOME/logs"

[ -z "${JPDA_ADDRESS}" ]  && JPDA_ADDRESS="8000"

#Tomcat options
[ -z "${CATALINA_OPTS}" ]  && CATALINA_OPTS="-Dcom.sun.management.jmxremote  \
                                             -Dcom.sun.management.jmxremote.ssl=false \
                                             -Dcom.sun.management.jmxremote.authenticate=false \
                                             -Dcodenvy.local.conf.dir=${CODENVY_LOCAL_CONF_DIR}"

#Class path
[ -z "${CLASSPATH}" ]  && CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar"


export JAVA_OPTS="$JAVA_OPTS  -Dcodenvy.logs.dir=${CODENVY_LOGS_DIR}"


#Class path
[ -z "${SERVER_PORT}" ]  && SERVER_PORT=8080
export SERVER_PORT