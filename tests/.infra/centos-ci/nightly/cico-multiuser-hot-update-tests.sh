#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -e

echo "========Starting nigtly test job $(date)========"

source tests/.infra/centos-ci/functional_tests_utils.sh

function prepareCustomResourceFile() {
  cd /tmp
  wget https://raw.githubusercontent.com/eclipse/che-operator/master/deploy/crds/org_v1_che_cr.yaml -O custom-resource.yaml
  sed -i "s@server:@server:\n    customCheProperties:\n      CHE_LIMITS_USER_WORKSPACES_RUN_COUNT: '-1'@g" /tmp/custom-resource.yaml
  sed -i "s@tlsSupport: true@tlsSupport: false@g" /tmp/custom-resource.yaml
  sed -i "s@identityProviderPassword: ''@identityProviderPassword: 'admin'@g" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}

setupEnvs
installKVM
installDependencies
installDockerCompose
installAndStartMinishift
loginToOpenshiftAndSetDevRole
installCheCtl
prepareCustomResourceFile
deployCheIntoCluster --chenamespace=eclipse-che --che-operator-cr-yaml=/tmp/custom-resource.yaml
seleniumTestsSetup

bash tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
--host=${CHE_ROUTE} \
 --port=80 \
 --multiuser \
 --threads=1 \
 --fail-script-on-failed-tests \
 --test=org.eclipse.che.selenium.hotupdate.rolling.** \
 || IS_TESTS_FAILED=true

echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "cico-nightly-hot-update-test"

if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
