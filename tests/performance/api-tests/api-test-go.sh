#!/bin/bash
set -e

wget -O /tmp/api-utils.sh https://raw.githubusercontent.com/eclipse/che/main/tests/performance/api-tests/api-utils.sh
source /tmp/api-utils.sh

export TEST_DEVFILE_PATH="devfile-registry/devfiles/TP__go__golang-health-check/devworkspace-che-code-latest.yaml"
export WORKSPACE_NAME="golang"
export projectName="golang-health-check"
export containerName="tools"
export expectedCommandOutput="golang-health-check"
export commandToTest="cd /projects/$projectName && go build && ls | grep golang-health-check"

oc login -u $OCP_USERNAME -p $OCP_PASSWORD --server=$OCP_SERVER_URL --insecure-skip-tls-verify
cd /tmp

startWorkspace ${BASE_URL} ${TEST_DEVFILE_PATH} ${WORKSPACE_NAME}

testProjectImported ${WORKSPACE_NAME} ${containerName} ${projectName}

echo "---- Test 'go build' command execution ----"
testCommand ${WORKSPACE_NAME} ${containerName} "${commandToTest}" "${expectedCommandOutput}"

deleteWorkspace ${WORKSPACE_NAME}
