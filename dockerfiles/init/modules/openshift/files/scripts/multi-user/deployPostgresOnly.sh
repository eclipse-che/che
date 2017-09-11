#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

COMMAND_DIR=$(dirname "$0") 

oc create -f "$COMMAND_DIR"/che-init-image-stream.yaml

oc create -f "$COMMAND_DIR"/postgres/

IMAGE_INIT=${IMAGE_INIT:-"eclipse/che-init:nightly"}

oc create -f - <<-EOF

apiVersion: v1
kind: BuildConfig
metadata:
  name: che-init-image-stream-build
spec:
  nodeSelector: null
  output:
    to:
      kind: ImageStreamTag
      name: 'che-init:latest'
  runPolicy: Serial
  source:
    dockerfile: |
      FROM ${IMAGE_INIT}
    type: Dockerfile
  strategy:
    dockerStrategy:
      from:
        kind: DockerImage
        name: '${IMAGE_INIT}'
    type: Docker
  triggers:
    - type: ConfigChange
    - type: "ImageChange" 
      imageChange: {}
status:

EOF

IMAGE_POSTGRES=${IMAGE_POSTGRES:-centos/postgresql-95-centos7}

oc create -f - <<-EOF

apiVersion: v1
kind: ImageStream
metadata:
  name: postgres-source
spec:
  tags:
  - from:
      kind: DockerImage
      name: ${IMAGE_POSTGRES}
    name: latest
    importPolicy:
      scheduled: true
      
EOF
