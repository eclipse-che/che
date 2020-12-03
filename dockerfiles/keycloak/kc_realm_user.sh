#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

echo "Configuring Keycloak by modifying realm and user templates..."

cat /scripts/che-users-0.json.erb | \
                                  sed -e "/<% if scope.lookupvar('keycloak::che_keycloak_admin_require_update_password') == 'true' -%>/d" | \
                                  sed -e "/<% else -%>/d" | \
                                  sed -e "/<% end -%>/d" | \
                                  sed -e "/\"requiredActions\" : \[ \],/d" > /scripts/che-users-0.json

if [ "${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}" == "false" ]; then
    sed -i -e "s#\"UPDATE_PASSWORD\"##" /scripts/che-users-0.json
fi

DEFAULT_CHE_HOST="che-${NAMESPACE}.${ROUTING_SUFFIX}"
CHE_HOST=${CHE_HOST:-${DEFAULT_CHE_HOST}}

cat /scripts/che-realm.json.erb | \
                                sed -e "s@<%= scope\.lookupvar('che::che_server_url') %>@${PROTOCOL}://${CHE_HOST}@" \
                                > /scripts/che-realm.json

echo "Creating Admin user..."

if [ $KEYCLOAK_USER ] && [ $KEYCLOAK_PASSWORD ]; then
    /opt/jboss/keycloak/bin/add-user-keycloak.sh --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
fi

# Handle CA certificates
KEYSTORE_PATH=/scripts/openshift.jks
TRUST_STORE_PASSWORD=${TRUSTPASS:-openshift}
CUSTOM_CERTS_DIR=/public-certs

# Check for additional CA certificates propagated to Keycloak
if [[ -d $CUSTOM_CERTS_DIR && -n $(find ${CUSTOM_CERTS_DIR} -type f) ]]; then
    for certfile in ${CUSTOM_CERTS_DIR}/* ; do
        keytool -importcert -alias CERT_$(basename $certfile) -keystore $KEYSTORE_PATH -file $certfile -storepass $TRUST_STORE_PASSWORD  -noprompt;
    done
fi

# Check for self-sighed certificate
if [ "${CHE_SELF__SIGNED__CERT}" != "" ]; then
    echo "${CHE_SELF__SIGNED__CERT}" > /scripts/openshift.cer
    keytool -importcert -alias HOSTDOMAIN -keystore $KEYSTORE_PATH -file /scripts/openshift.cer -storepass $TRUST_STORE_PASSWORD -noprompt
fi

# Export Java trust store into one that is propagated to Keycloak
if [ -f "$KEYSTORE_PATH" ]; then
    keytool -importkeystore -srckeystore $JAVA_HOME/jre/lib/security/cacerts -destkeystore $KEYSTORE_PATH -srcstorepass changeit -deststorepass $TRUST_STORE_PASSWORD
    /opt/jboss/keycloak/bin/jboss-cli.sh --file=/scripts/cli/add_openshift_certificate.cli && rm -rf /opt/jboss/keycloak/standalone/configuration/standalone_xml_history
fi

# POSTGRES_PORT is assigned by Kubernetes controller
# and it isn't fit to docker-entrypoin.sh.
unset POSTGRES_PORT

echo "Starting Keycloak server..."

exec /opt/jboss/docker-entrypoint.sh -Dkeycloak.migration.action=import \
                                     -Dkeycloak.migration.provider=dir \
                                     -Dkeycloak.migration.strategy=IGNORE_EXISTING \
                                     -Dkeycloak.migration.dir=/scripts/ \
                                     -Djboss.bind.address=0.0.0.0
