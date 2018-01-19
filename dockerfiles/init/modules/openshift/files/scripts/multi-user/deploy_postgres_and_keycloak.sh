#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
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
DEFAULT_CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD=true

# apply KC build config
oc apply -f - <<-EOF

apiVersion: v1
kind: BuildConfig
metadata:
  name: keycloak-for-che
spec:
  nodeSelector: null
  output:
    to:
      kind: ImageStreamTag
      name: 'keycloak:latest'
  postCommit: {}
  resources: {}
  runPolicy: Serial
  source:
    images:
      - from:
          kind: ImageStreamTag
          name: 'che-init:latest'
        paths:
          - destinationDir: ./themes/
            sourcePath: /etc/puppet/modules/keycloak/files/che/
          - destinationDir: ./realms/
            sourcePath: /etc/puppet/modules/keycloak/templates/.
          - destinationDir: .s2i/bin/
            sourcePath: /files/s2i/keycloak/assemble
          - destinationDir: .s2i/bin/
            sourcePath: /files/s2i/keycloak/run
    type: Image
  strategy:
    sourceStrategy:
      env:
          - name: "CHE_SERVER_URL"
            value: "${CHE_SERVER_URL}"
          - name: "CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD"
            value: "${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD:-${DEFAULT_CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}}"
      from:
        kind: ImageStreamTag
        name: 'keycloak-source:latest'
    type: Source
  triggers:
  - type: "ImageChange"
    imageChange: {}
  - type: "ImageChange"
    imageChange:
      from:
        kind: "ImageStreamTag"
        name: "che-init:latest"
status:

EOF

# apply all yaml files from "$COMMAND_DIR"/keycloak/
oc apply -f "$COMMAND_DIR"/keycloak/

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

IMAGE_KEYCLOACK=${IMAGE_KEYCLOACK:-"jboss/keycloak-openshift:3.3.0.CR2-3"}

oc apply -f - <<-EOF

apiVersion: v1
kind: ImageStream
metadata:
  name: keycloak-source
spec:
  tags:
  - from:
      kind: DockerImage
      name: ${IMAGE_KEYCLOACK}
    name: latest
    importPolicy:
      scheduled: true

EOF

"$COMMAND_DIR"/wait_until_keycloak_is_available.sh
