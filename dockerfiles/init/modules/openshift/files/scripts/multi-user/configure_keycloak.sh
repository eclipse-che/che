#!/bin/bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

if [ -z "${IMAGE_KEYCLOAK+x}" ]; then echo "[CHE] **ERROR**Env var IMAGE_KEYCLOAK is unset. You need to set it to continue. Aborting"; exit 1; fi

COMMAND_DIR=$(dirname "$0")
CHE_HOST=$(oc get route che -o jsonpath='{.spec.host}')
KC_HOST=$(oc get route keycloak -o jsonpath='{.spec.host}')
CHE_SERVER_ROUTE_TLS=$(oc get route che -o jsonpath='{.spec.tls}' || echo "")

if [ "${CHE_SERVER_ROUTE_TLS}" != "" ]; then
    HTTP_PROTOCOL="https"
else
    HTTP_PROTOCOL="http"
fi

CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD=${DEFAULT_CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD:-${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}}

echo "[CHE] Configuring Keycloak realm, client and user..."

cat "${COMMAND_DIR}"/keycloak-config/keycloak-config-pod-deployment.yaml | sed "s/\${CHE_HOST}/${CHE_HOST}/" | \
                                                           sed "s/\${KC_HOST}/${KC_HOST}/" | \
                                                           sed "s/\${HTTP_PROTOCOL}/${HTTP_PROTOCOL}/" | \
                                                           sed "s#\${IMAGE_KEYCLOAK}#${IMAGE_KEYCLOAK}#" | \
                                                           sed "s/\${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}/${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}/" | \
                                                           oc apply -f -

echo "[CHE] Keycloak configuration initiated. It takes ~10 seconds to complete"
KC_UTIL_POD=$(oc get pods -l="app=keycloak-util" -o jsonpath='{.items[].metadata.name}')
sleep 5
DEPLOYMENT_TIMEOUT_SEC=1200
POLLING_INTERVAL_SEC=5
end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
available=$(oc get pods keycloak-util -o json | jq '.status.containerStatuses[].state | to_entries[].key')
while [[ "${available}" != "\"terminated\"" ]] && [ ${SECONDS} -lt ${end} ]; do
    timeout_in=$((end-SECONDS))
    echo "[CHE] Watching Keycloak config pod status. Current status=${available}, Timeout in ${timeout_in}s)"
    sleep ${POLLING_INTERVAL_SEC}
    available=$(oc get pods keycloak-util -o json | jq '.status.containerStatuses[].state | to_entries[].key')
done
oc logs -f "${KC_UTIL_POD}"
oc delete pod "${KC_UTIL_POD}"
echo "[CHE] Keycloak configuration completed"
