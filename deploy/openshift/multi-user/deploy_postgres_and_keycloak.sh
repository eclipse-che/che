#!/bin/bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

COMMAND_DIR=$(dirname "$0")

export CHE_EPHEMERAL=${CHE_EPHEMERAL:-false}

"$COMMAND_DIR"/deploy_postgres_only.sh

# append_before_match allows to append content before matching line
# this is needed to append content of yaml files
# first arg is mathing string, second string to insert before match
append_before_match() {
    while IFS= read -r line
    do
      if [[ "$line" == *"$1"* ]];then
          printf '%s\n' "$2"
      fi
      printf '%s\n' "$line"
    done < /dev/stdin
}

if [ "${CHE_SERVER_URL}" == "" ]; then
  CHE_SERVER_ROUTE_HOST=$(oc get route che -o jsonpath='{.spec.host}' || echo "")
  if [ "${CHE_SERVER_ROUTE_HOST}" == "" ]; then
    echo "[CHE] **ERROR**: The Che server route should exist before configuring the Keycloak web origins"
    exit 1
  fi
  if [ "${CHE_SERVER_ROUTE_TLS}" == "" ]; then
    CHE_SERVER_URL="http://${CHE_SERVER_ROUTE_HOST}"
  else
    CHE_SERVER_URL="https://${CHE_SERVER_ROUTE_HOST}"
  fi
fi

# apply all yaml files from "$COMMAND_DIR"/keycloak/

if [ -z "${IMAGE_KEYCLOAK+x}" ]; then echo "[CHE] **ERROR**Env var IMAGE_KEYCLOAK is unset. You need to set it to continue. Aborting"; exit 1; fi

for i in $(ls "$COMMAND_DIR"/keycloak ); do
    cat "${COMMAND_DIR}"/keycloak/"${i}" | sed "s#\${IMAGE_KEYCLOAK}#${IMAGE_KEYCLOAK}#" | oc apply -f -
done


if [ "${CHE_EPHEMERAL}" == "true" ]; then
  oc volume dc/keycloak --remove --confirm
  oc delete pvc/keycloak-log
  oc delete pvc/keycloak-data
fi

TLS_SETTINGS="  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Allow"

CHE_SERVER_ROUTE_TLS=$(oc get route che -o jsonpath='{.spec.tls}' || echo "")
if [ "${CHE_SERVER_ROUTE_TLS}" != "" ]; then
  oc get route/keycloak -o yaml | \
  append_before_match "wildcardPolicy:" "${TLS_SETTINGS}" | \
  oc replace -f -
fi

"$COMMAND_DIR"/wait_until_keycloak_is_available.sh
