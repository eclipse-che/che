#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

COMMAND_DIR=$(dirname "$0") 

"$COMMAND_DIR"/deployPostgresOnly.sh
"$COMMAND_DIR"/wait_until_postgres_is_available.sh

oc create -f "$COMMAND_DIR"/keycloak/

IMAGE_KEYCLOACK=${IMAGE_KEYCLOACK:-"dfestal/keycloak-postgres-openshift:3.2.1.Final"}

oc create -f - <<-EOF

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
