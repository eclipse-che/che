#!/bin/sh
#
# CODENVY CONFIDENTIAL
# __________________
#
# [2012] - [2013] Codenvy, S.A.
# All Rights Reserved.
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

LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
CODENVY_CONFIG_OPTS="-Xshare:auto -Xms512m -Xmx1024m -XX:MaxPermSize=256m"
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
JAVA_OPTS="$JAVA_OPTS $LOG_OPTS $CODENVY_CONFIG_OPTS $REMOTE_DEBUG -Djava.security.properties=${CATALINA_BASE}/conf/codenvy-security.properties"
export JAVA_OPTS
export SERVER_PORT=8081
export CLASSPATH="${CATALINA_HOME}/conf/:${CATALINA_HOME}/lib/jul-to-slf4j.jar:${CATALINA_HOME}/lib/slf4j-api.jar:${CATALINA_HOME}/lib/logback-classic.jar:${CATALINA_HOME}/lib/logback-core.jar:${JAVA_HOME}/lib/tools.jar"
