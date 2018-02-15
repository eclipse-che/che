#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

COMMAND_DIR=$(dirname "$0")

CHE_HOST=$(oc get route che -o jsonpath='{.spec.host}')

KC_HOST=$(oc get route keycloak -o jsonpath='{.spec.host}')

CHE_SERVER_ROUTE_TLS=$(oc get route che -o jsonpath='{.spec.tls}' || echo "")

if [ "${CHE_SERVER_ROUTE_TLS}" != "" ]; then
    HTTP_PROTOCOL="https"
    else
    HTTP_PROTOCOL="http"
    fi

echo "[CHE] Configuring Keycloak realm, client and user..."

cat "${COMMAND_DIR}"/keycloak-config-pod-deployment.yaml | sed "s/\${CHE_HOST}/${CHE_HOST}/" | \
                                                    sed "s/\${KC_HOST}/${KC_HOST}/" | \
                                                    sed "s/\${HTTP_PROTOCOL}/${HTTP_PROTOCOL}/" | oc apply -f -

