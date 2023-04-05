#!/bin/bash
set -e

wget -O /tmp/api-utils.sh https://raw.githubusercontent.com/eclipse/che/main/tests/performance/api-tests/api-utils.sh
source /tmp/api-utils.sh

export TEST_DEVFILE_PATH="devfile-registry/devfiles/python__python-hello-world/devworkspace-che-code-latest.yaml"
export WORKSPACE_NAME="python-hello-world"
export projectName="python-hello-world"
export expectedCommandOutput="Hello, world!"
export containerName="python"
export commandToTest="cd /projects/$projectName; pwd && ls -la && python -m venv .venv && . .venv/bin/activate; python hello-world.py >> command_log.txt; cat command_log.txt; grep '$expectedCommandOutput' ./command_log.txt;"

oc login -u $OCP_USERNAME -p $OCP_PASSWORD --server=$OCP_SERVER_URL --insecure-skip-tls-verify
cd /tmp

startWorkspace ${BASE_URL}/${TEST_DEVFILE_PATH} ${WORKSPACE_NAME}

testProjectImported ${WORKSPACE_NAME} ${containerName} ${projectName}

testCommand ${WORKSPACE_NAME} ${containerName} "${commandToTest}" "${expectedCommandOutput}"

oc delete dw $WORKSPACE_NAME || true
