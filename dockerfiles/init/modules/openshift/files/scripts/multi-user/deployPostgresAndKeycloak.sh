#!/bin/bash

$(dirname "$0")/deployPostgresOnly.sh
$(dirname "$0")/wait_until_postgres_is_available.sh

oc create -f $(dirname "$0")/keycloak/

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
