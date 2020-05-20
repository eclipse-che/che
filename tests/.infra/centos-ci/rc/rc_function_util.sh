#!/usr/bin/env bash

# Copyright (c) 2020 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

function prepareCustomResourceFile() {
  RELEASE_VERSION=7.12.1
  echo "======== Patch custom-resource.yaml ========"
  cd /tmp
  wget https://raw.githubusercontent.com/eclipse/che-operator/$RELEASE_VERSION/deploy/crds/org_v1_che_cr.yaml -O custom-resource.yaml
  setOpenShiftoAuth=$1 # sets the value of 'openShiftoAuth' parameter
  sed -i "s@openShiftoAuth: false@openShiftoAuth: $1@g" /tmp/custom-resource.yaml
  sed -i "s@server:@server:\n    customCheProperties:\n      CHE_LIMITS_USER_WORKSPACES_RUN_COUNT: '-1'@g" /tmp/custom-resource.yaml
  sed -i "s/customCheProperties:/customCheProperties:\n      CHE_WORKSPACE_AGENT_DEV_INACTIVE__STOP__TIMEOUT__MS: '300000'/" /tmp/custom-resource.yaml
  sed -i "s@identityProviderImage: 'quay.io/eclipse/che-keycloak:nightly'@identityProviderImage: 'quay.io/eclipse/che-keycloak:$RELEASE_VERSION'@g" /tmp/custom-resource.yaml
  sed -i "s@cheImage: ''@cheImage: 'quay.io/eclipse/che-server'@g" /tmp/custom-resource.yaml
  sed -i "s@cheImageTag: 'nightly'@cheImageTag: '$RELEASE_VERSION'@g" /tmp/custom-resource.yaml
  sed -i "s@devfileRegistryImage: 'quay.io/eclipse/che-devfile-registry:nightly'@devfileRegistryImage: 'quay.io/eclipse/che-devfile-registry:$RELEASE_VERSION'@g" /tmp/custom-resource.yaml
  sed -i "s@pluginRegistryImage: 'quay.io/eclipse/che-plugin-registry:nightly'@pluginRegistryImage: 'quay.io/eclipse/che-plugin-registry:$RELEASE_VERSION'@g " /tmp/custom-resource.yaml
  sed -i "s@tlsSupport: true@tlsSupport: false@g" /tmp/custom-resource.yaml
  sed -i "s@identityProviderPassword: ''@identityProviderPassword: 'admin'@g" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}

function installReleaseCheCtl() {
  # 7.12.1 chectl
  releaseChectlPackageUrl=https://github.com/che-incubator/chectl/releases/download/20200501092240/chectl-linux-x64.tar.gz

  cd /tmp
  wget ${releaseChectlPackageUrl} -O chectl-linux-x64.tar.gz
  tar -xzf chectl-linux-x64.tar.gz
  export PATH=$PATH:/tmp/chectl/bin
}
