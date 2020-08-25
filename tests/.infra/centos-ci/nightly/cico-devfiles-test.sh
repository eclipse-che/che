#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -x

echo "========Starting nigtly test job $(date)========"

source tests/.infra/centos-ci/functional_tests_utils.sh

function prepareCustomResourcePatchFile() {
  cat > /tmp/custom-resource-patch.yaml <<EOL
spec:
  auth:
    updateAdminPassword: false
    openShiftoAuth: false
EOL

  cat /tmp/custom-resource-patch.yaml
}

setupEnvs
installKVM
installDependencies
prepareCustomResourcePatchFile
installCheCtl
installAndStartMinishift
deployCheIntoCluster --che-operator-cr-patch-yaml=/tmp/custom-resource-patch.yaml
runDevfileTestSuite
echo "=========================== THIS IS POST TEST ACTIONS =============================="
getOpenshiftLogs
archiveArtifacts "che-devfile-test"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
