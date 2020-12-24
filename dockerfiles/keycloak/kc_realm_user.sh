#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

function jks_import_ca_bundle {
  CA_FILE=$1
  KEYSTORE_PATH=$2
  KEYSTORE_PASSWORD=$3

  if [ ! -f "$CA_FILE" ]; then
    # CA bundle file doesn't exist, skip it
    echo "Failed to import CA certificates from ${CA_FILE}. File doesn't exist"
    return
  fi

  bundle_name=$(basename "$CA_FILE")
  certs_imported=0
  cert_index=0
  tmp_file=/tmp/cert.pem
  is_cert=false
  while IFS= read -r line; do
    if [ "$line" == "-----BEGIN CERTIFICATE-----" ]; then
      # Start copying a new certificate
      is_cert=true
      cert_index=$((cert_index+1))
      # Reset destination file and add header line
      echo "$line" > ${tmp_file}
    elif [ "$line" == "-----END CERTIFICATE-----" ]; then
      # End of the certificate is reached, add it to trust store
      is_cert=false
      echo "$line" >> ${tmp_file}
      keytool -importcert -alias "${bundle_name}_${cert_index}" -keystore "$KEYSTORE_PATH" -file $tmp_file -storepass "$KEYSTORE_PASSWORD" -noprompt && \
      certs_imported=$((certs_imported+1))
    elif [ "$is_cert" == true ]; then
      # In the middle of a certificate, copy line to target file
      echo "$line" >> ${tmp_file}
    fi
  done < "$CA_FILE"
  echo "Imported ${certs_imported} certificates from ${CA_FILE}"
  # Clean up
  rm -f $tmp_file
}

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
if [[ -d $CUSTOM_CERTS_DIR && -n $(find "${CUSTOM_CERTS_DIR}" -type f) ]]; then
    for certfile in ${CUSTOM_CERTS_DIR}/* ; do
        jks_import_ca_bundle "$certfile" "$KEYSTORE_PATH" "$TRUST_STORE_PASSWORD"
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
