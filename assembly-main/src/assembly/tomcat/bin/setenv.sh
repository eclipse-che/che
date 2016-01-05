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


#Global JAVA options
[ -z "${JAVA_OPTS}" ] && JAVA_OPTS="${JAVA_OPTS} -Xms256m -Xmx1024m -server -Dport.http=${CHE_PORT}"

[ -z "${JPDA_ADDRESS}" ]  && JPDA_ADDRESS="8000"

#Tomcat options
[ -z "${CATALINA_OPTS}" ]  && CATALINA_OPTS="-Dcom.sun.management.jmxremote  \
                                             -Dcom.sun.management.jmxremote.ssl=false \
                                             -Dcom.sun.management.jmxremote.authenticate=false \
                                             -Dche.local.conf.dir=${CHE_LOCAL_CONF_DIR} \
                                             -Dche.home=${CHE_HOME} \
                                             -Dche.logs.dir=${CHE_LOGS_DIR}"

#Class path
[ -z "${CLASSPATH}" ]  && CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar"
