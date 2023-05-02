#!/bin/bash
set -e

wget -O /tmp/api-utils.sh https://raw.githubusercontent.com/eclipse/che/main/tests/performance/api-tests/api-utils.sh
source /tmp/api-utils.sh

export TEST_DEVFILE_PATH="devfile-registry/devfiles/nodejs__nodejs-mongodb-sample/devworkspace-che-code-latest.yaml"
export WORKSPACE_NAME="nodejs-mongodb"
export projectName="nodejs-mongodb-sample"
export containerName="nodejs"

oc login -u $OCP_USERNAME -p $OCP_PASSWORD --server=$OCP_SERVER_URL --insecure-skip-tls-verify
cd /tmp

startWorkspace ${BASE_URL} ${TEST_DEVFILE_PATH} ${WORKSPACE_NAME}

testProjectImported ${WORKSPACE_NAME} ${containerName} ${projectName}

deleteWorkspace ${WORKSPACE_NAME}
