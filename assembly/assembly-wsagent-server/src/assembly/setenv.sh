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

add_cert_to_truststore() {
  SELF_SIGNED_CERT=/tmp/che/secret/ca.crt

  if [ ! -f $SELF_SIGNED_CERT ]; then
    return 0;
  fi

  mkdir -p $CHE_LOCAL_CONF_DIR/conf
  WSAGENT_JAVA_TRUST_STORE=$CHE_LOCAL_CONF_DIR/conf/wsagent_cacerts

  DEFAULT_JAVA_TRUST_STORE=$JAVA_HOME/jre/lib/security/cacerts
  if [ -f $DEFAULT_JAVA_TRUST_STORE ]; then
    cp $DEFAULT_JAVA_TRUST_STORE $WSAGENT_JAVA_TRUST_STORE
    echo "Found a self-signed cert. Workspace Agent Java trust store will be based ${DEFAULT_JAVA_TRUST_STORE}"
  else
    DEFAULT_JAVA_TRUST_STORE=$JAVA_HOME/lib/security/cacerts
    if [ -f $DEFAULT_JAVA_TRUST_STORE ]; then
      cp $DEFAULT_JAVA_TRUST_STORE $WSAGENT_JAVA_TRUST_STORE
      echo "Found a self-signed cert. Workspace Agent Java trust store will be based ${DEFAULT_JAVA_TRUST_STORE}"
    fi
  fi

  DEFAULT_JAVA_TRUST_STORE_PASS="changeit"
  JAVA_TRUST_STORE_PASS=${JAVA_TRUST_STORE_PASS:-${DEFAULT_JAVA_TRUST_STORE_PASS}}

  # make sure that owner has permissions to write and other groups have permissions to read
  chmod 644 $WSAGENT_JAVA_TRUST_STORE

  echo yes | keytool -keystore $WSAGENT_JAVA_TRUST_STORE -importcert -alias HOSTDOMAIN -file $SELF_SIGNED_CERT -storepass $JAVA_TRUST_STORE_PASS > /dev/null

  # allow only read by all groups
  chmod 444 $WSAGENT_JAVA_TRUST_STORE

  export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=$WSAGENT_JAVA_TRUST_STORE -Djavax.net.ssl.trustStorePassword=changeit"
}

add_cert_to_truststore

export JAVA_OPTS="$JAVA_OPTS  -Dche.logs.dir=${CHE_LOGS_DIR} -Dche.logs.level=${CHE_LOGS_LEVEL} -Djuli-logback.configurationFile=file:$CATALINA_HOME/conf/tomcat-logger.xml"

[ -z "${SERVER_PORT}" ]  && SERVER_PORT=8080
export SERVER_PORT
