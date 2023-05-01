#!/bin/bash
set -e

wget -O /tmp/api-utils.sh https://raw.githubusercontent.com/eclipse/che/main/tests/performance/api-tests/api-utils.sh
source /tmp/api-utils.sh

export TEST_DEVFILE_PATH="devfile-registry/devfiles/TP__cpp__c-plus-plus/devworkspace-che-code-latest.yaml"
export WORKSPACE_NAME="cpp"
export projectName="c-plus-plus/strings"
export expectedCommandOutput="Found"
export containerName="tools"
export commandToTest="cd /projects/$projectName && rm -f bin.out && g++ -g "knuth_morris_pratt.cpp" -o bin.out; ./bin.out >> command_log.txt; grep '$expectedCommandOutput' ./command_log.txt;"

oc login -u $OCP_USERNAME -p $OCP_PASSWORD --server=$OCP_SERVER_URL --insecure-skip-tls-verify
cd /tmp

startWorkspace ${BASE_URL} ${TEST_DEVFILE_PATH} ${WORKSPACE_NAME}

testProjectImported ${WORKSPACE_NAME} ${containerName} ${projectName}

testCommand ${WORKSPACE_NAME} ${containerName} "${commandToTest}" "${expectedCommandOutput}"

deleteWorkspace ${WORKSPACE_NAME}
