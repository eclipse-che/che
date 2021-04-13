#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -x

echo "========Starting nigtly test job $(date)========"

source tests/.infra/centos-ci/functional_tests_utils.sh

function runOpenshiftConnectorTest(){
    
    ### Create directory for report
    cd /root/payload
    mkdir report
    REPORT_FOLDER=$(pwd)/report
    ### Run tests
    docker run --net=host  --ipc=host -v $REPORT_FOLDER:/tmp/e2e/report:Z --shm-size=1g \
    -e TS_SELENIUM_LOAD_PAGE_TIMEOUT=420000 \
    -e TS_SELENIUM_WORKSPACE_STATUS_POLLING=20000 \
    -e TS_SELENIUM_BASE_URL="https://$CHE_ROUTE" \
    -e TS_SELENIUM_USERNAME=${TEST_USERNAME} \
    -e TS_SELENIUM_PASSWORD=${TEST_USERNAME} \
    -e TS_TEST_OPENSHIFT_PLUGIN_USERNAME=developer \
    -e TS_TEST_OPENSHIFT_PLUGIN_PASSWORD=pass \
    -e TS_TEST_OPENSHIFT_PLUGIN_PROJECT=eclipse-che \
    -e TS_TEST_OPENSHIFT_PLUGIN_COMPONENT_TYPE="nodejs (s2i)" \
    -e TS_TEST_OPENSHIFT_PLUGIN_COMPONENT_VERSION=latest \
    -e TS_SELENIUM_MULTIUSER=true \
    -e NODE_TLS_REJECT_UNAUTHORIZED=0 \
    -e DELETE_WORKSPACE_ON_FAILED_TEST=true \
    -e TS_SELENIUM_LOG_LEVEL=TRACE \
    -e TS_SELENIUM_START_WORKSPACE_TIMEOUT=720000 \
    -e TEST_SUITE=test-openshift-connector \
     quay.io/eclipse/che-e2e:nightly || IS_TESTS_FAILED=true
    
}


function prepareCustomResourcePatchFile() {
  cat > /tmp/custom-resource-patch.yaml <<EOL
spec:
  server:
    customCheProperties:
      CHE_INFRA_KUBERNETES_WORKSPACE_START_TIMEOUT_MIN: '12'
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
defineCheRoute
createTestUserAndObtainUserToken
runOpenshiftConnectorTest
echo "=========================== THIS IS POST TEST ACTIONS =============================="
getOpenshiftLogs
archiveArtifacts "che-nightly-openshift-connector"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
