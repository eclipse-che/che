#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -x

echo "========Starting nigtly test job $(date)========"
./tests/.infra/centos-ci/nightly/
source tests/.infra/centos-ci/functional_tests_utils.sh

function prepareCustomResourcePatchFile() {
  cat > /tmp/custom-resource-patch.yaml <<EOL
spec:
  server:
    customCheProperties:
      CHE_LIMITS_USER_WORKSPACES_RUN_COUNT: '-1'
      CHE_WORKSPACE_AGENT_DEV_INACTIVE__STOP__TIMEOUT__MS: '300000'
  auth:
    updateAdminPassword: false
    identityProviderPassword: admin
EOL

  cat /tmp/custom-resource-patch.yaml
}

setupEnvs
installKVM
installDependencies
installCheCtl
installAndStartMinishift
prepareCustomResourcePatchFile
deployCheIntoCluster --che-operator-cr-patch-yaml=/tmp/custom-resource-patch.yaml
createTestUserAndObtainUserToken
installDockerCompose
seleniumTestsSetup
createIndentityProvider
bash tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
  --threads=3 \
  --host=${CHE_ROUTE} \
  --https \
  --port=443 \
  --multiuser \
  --fail-script-on-failed-tests \
  || IS_TESTS_FAILED=true

echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "che-nigthly-multiuser-stable-test"

if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
