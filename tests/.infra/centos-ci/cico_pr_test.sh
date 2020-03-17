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
deployCheIntoCluster --chenamespace=eclipse-che --che-operator-cr-yaml=/tmp/custom-resource.yaml
createTestUserAndObtainUserToken
launchOpenshiftTest
echo "=========================== THIS IS POST TEST ACTIONS =============================="
archiveArtifacts "che-openshift-connector"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi



launchOpenshiftTest(){
  defineCheRoute
  ### Create workspace
  DEV_FILE_URL=$1
  if [[ ${DEV_FILE_URL} = "" ]]; then # by default it is used 'happy-path-devfile' yaml from CHE 'master' branch
    chectl workspace:start --access-token "$USER_ACCESS_TOKEN" --devfile=https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/happy-path/happy-path-workspace.yaml
  else
    chectl workspace:start --access-token "$USER_ACCESS_TOKEN" $1 # it can be directly indicated other URL to 'devfile' yaml
  fi

  ### Create directory for report
  mkdir report
  REPORT_FOLDER=$(pwd)/report
  ### Run tests
  docker run --shm-size=256m --network host -v $REPORT_FOLDER:/tmp/e2e/report:Z \
  -e TS_SELENIUM_BASE_URL="http://$CHE_ROUTE" \
  -e TS_SELENIUM_MULTIUSER="true" \
  -e TS_SELENIUM_USERNAME="${TEST_USERNAME}" \
  -e TS_SELENIUM_PASSWORD="${TEST_USERNAME}" \
  -e TS_SELENIUM_LOAD_PAGE_TIMEOUT=420000 \
  -e TS_SELENIUM_W3C_CHROME_OPTION=false \
  -e TS_SELENIUM_START_WORKSPACE_TIMEOUT=900000 \
  -e TEST_SUITE=openshift-connector \
  -e NODE_TLS_REJECT_UNAUTHORIZED=0
  quay.io/eclipse/che-e2e:nightly || IS_TESTS_FAILED=true
}