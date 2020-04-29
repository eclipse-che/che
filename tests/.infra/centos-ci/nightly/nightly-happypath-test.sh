#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -e

echo "========Starting nigtly test job $(date)========"

source tests/.infra/centos-ci/functional_tests_utils.sh
source .ci/cico_common.sh

function prepareCustomResourceFile() {
  cd /tmp
  wget https://raw.githubusercontent.com/eclipse/che-operator/master/deploy/crds/org_v1_che_cr.yaml -O custom-resource.yaml
  sed -i "s@tlsSupport: true@tlsSupport: false@g" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}

setupEnvs
installKVM
installDependencies
installCheCtl
installAndStartMinishift
loginToOpenshiftAndSetDevRole
prepareCustomResourceFile
deployCheIntoCluster --chenamespace=che --che-operator-cr-yaml=/tmp/custom-resource.yaml
createTestUserAndObtainUserToken
createTestWorkspaceAndRunTest
echo "=========================== THIS IS POST TEST ACTIONS =============================="
getOpenshiftLogs
archiveArtifacts "che-nightly-happy-path"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
