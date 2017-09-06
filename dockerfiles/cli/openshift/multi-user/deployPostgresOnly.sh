#!/bin/bash

oc create -f $(dirname "$0")/che-init-image-stream.yaml

oc create -f $(dirname "$0")/postgres/

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
