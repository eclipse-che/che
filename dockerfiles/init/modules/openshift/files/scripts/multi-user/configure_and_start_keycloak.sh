#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

set -e

COMMAND_DIR=$(dirname "$0")

if [ "${CHE_SERVER_URL}" == "" ]; then
  CHE_SERVER_ROUTE_HOST=$(oc get route che -o jsonpath='{.spec.host}' || echo "")
  if [ "${CHE_SERVER_ROUTE_HOST}" == "" ]; then
    echo "[CHE] **ERROR**: The Che server route should exist before configuring the Keycloak web origins"
    exit 1
  fi
  PROTOCOL=CHE_SERVER_ROUTE_TLS=$(oc get route che -o jsonpath='{.spec.tls}' || echo "")
  if [ "${CHE_SERVER_ROUTE_TLS}" == "" ]; then
    CHE_SERVER_URL="http://${CHE_SERVER_ROUTE_HOST}"
  else 
    CHE_SERVER_URL="https://${CHE_SERVER_ROUTE_HOST}"
  fi
fi

oc set env buildconfig/keycloak-for-che CHE_SERVER_URL=${CHE_SERVER_URL}
oc start-build keycloak-for-che
