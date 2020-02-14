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
  sed -i "s@server:@server:\n    customCheProperties:\n      CHE_LIMITS_USER_WORKSPACES_RUN_COUNT: '-1'@g" /tmp/custom-resource.yaml
  sed -i "s/customCheProperties:/customCheProperties:\n      CHE_WORKSPACE_AGENT_DEV_INACTIVE__STOP__TIMEOUT__MS: '300000'/" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}

installKVM
setupEnvs
installDependencies
prepareCustomResourceFile
installCheCtl
installAndStartMinishift
loginToOpenshiftAndSetDevRole
deployCheIntoCluster --chenamespace=eclipse-che --che-operator-cr-yaml=/tmp/custom-resource.yaml
createTestUserAndObtainUserToken
installDockerCompose
seleniumTestsSetup
createIndentityProvider

bash tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
  --threads=3 \
  --host=${CHE_ROUTE} \
  --port=80 \
  --multiuser \
  --include-tests-under-repair \
  --include-flaky-tests \
  || IS_TESTS_FAILED=true

echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "che-nigthly-multiuser-all-test"

if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
