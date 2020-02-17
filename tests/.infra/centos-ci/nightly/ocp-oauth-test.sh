#!/usr/bin/env bash
# Copyright (c) 2020 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -e
set +x
source tests/.infra/centos-ci/functional_tests_utils.sh
function prepareCustomResourceFile() {
  echo "Patch custom-resource.yaml"
  cd /tmp
  wget https://raw.githubusercontent.com/eclipse/che-operator/master/deploy/crds/org_v1_che_cr.yaml -O custom-resource.yaml
  sed -i "s@openShiftoAuth: false@openShiftoAuth: true@g" /tmp/custom-resource.yaml
  sed -i "s@server:@server:\n    customCheProperties:\n      CHE_LIMITS_USER_WORKSPACES_RUN_COUNT: '-1'@g" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}
setupEnvs
installKVM
installDependencies
installDockerCompose
installAndStartMinishift
loginToOpenshiftAndSetDevRole
prepareCustomResourceFile
installCheCtl
deployCheIntoCluster  --chenamespace=eclipse-che --che-operator-cr-yaml=/tmp/custom-resource.yaml
seleniumTestsSetup

bash tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
  --threads=1 \
  --host=${CHE_ROUTE} \
  --port=80 \
  --multiuser \
  --test=org.eclipse.che.selenium.site.ocpoauth.** \
  --fail-script-on-failed-tests \
  || IS_TESTS_FAILED=true

echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "nightly-ocp-oauth-test"

if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi