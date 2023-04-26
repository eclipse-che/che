#!/bin/bash
set -e

wget -O /tmp/api-utils.sh https://raw.githubusercontent.com/eclipse/che/main/tests/performance/api-tests/api-utils.sh
source /tmp/api-utils.sh

export TEST_DEVFILE_PATH="devfile-registry/devfiles/java11-maven-lombok__lombok-project-sample/devworkspace-che-code-latest.yaml"
export WORKSPACE_NAME="java-lombok"
export projectName="lombok-project-sample"
export expectedCommandOutput="BUILD SUCCESS"
export containerName="tools"
export commandToTest="cd '$projectName'; mvn clean install >> command_log.txt; grep '$expectedCommandOutput' ./command_log.txt;"

oc login -u $OCP_USERNAME -p $OCP_PASSWORD --server=$OCP_SERVER_URL --insecure-skip-tls-verify
cd /tmp

startWorkspace ${BASE_URL}/${TEST_DEVFILE_PATH} ${WORKSPACE_NAME}

testProjectImported ${WORKSPACE_NAME} ${containerName} ${projectName}

testCommand ${WORKSPACE_NAME} ${containerName} "${commandToTest}" "${expectedCommandOutput}"

deleteWorkspace ${WORKSPACE_NAME}
