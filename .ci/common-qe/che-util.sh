#!/bin/bash
# shellcheck disable=SC2046,SC2164,SC2086,SC1090,SC2154

# Copyright (c) 2012-2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
set -e

SCRIPT_PATH="${BASH_SOURCE[0]}"
SCRIPT_DIR="$(dirname $SCRIPT_PATH)"
. $SCRIPT_DIR/common-util.sh

USERNAME=$(readConfigProperty test.username)
PASSWORD=$(readConfigProperty test.password)
SUITE=$(readConfigProperty test.suite)
MULTIUSER=$(readConfigProperty test.multiuser)
DEFAULT_TIMEOUT=$(readConfigProperty timeout.default)
WORKSPACE_STATUS_POLLING=$(readConfigProperty timeout.workspace.status.polling)
LOAD_PAGE_TIMEOUT=$(readConfigProperty timeout.load.page)
DEVFILE_URL=$(readConfigProperty test.workspace.devfile.url)


function obtainUserToken() {
    KEYCLOAK_URL=$(oc get checluster eclipse-che -o jsonpath='{.status.keycloakURL}')
    KEYCLOAK_BASE_URL="${KEYCLOAK_URL}/auth"
    
    local userAccessToken=$(curl -k -v -X POST $KEYCLOAK_BASE_URL/realms/che/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=${USERNAME}" -d "password=${PASSWORD}" -d "grant_type=password" -d "client_id=che-public" | jq -r .access_token)
    
    echo "========User Access Token: $userAccessToken "
}

function createTestWorkspace(){
    local userAccessToken=$(obtainUserToken)

    chectl workspace:create --start --access-token "$userAccessToken" --devfile="$DEVFILE_URL"
}

function runTest() {
    CHE_URL=$(oc get checluster eclipse-che -o jsonpath='{.status.cheURL}')
    
    ### Create directory for report
    cd /root/payload
    mkdir report
    REPORT_FOLDER=$(pwd)/report
    ### Run tests
    docker run --shm-size=1g --net=host  --ipc=host -v $REPORT_FOLDER:/tmp/e2e/report:Z \
    -e TS_SELENIUM_BASE_URL="$CHE_URL" \
    -e TS_SELENIUM_LOG_LEVEL=DEBUG \
    -e TS_SELENIUM_MULTIUSER="$MULTIUSER" \
    -e TS_SELENIUM_USERNAME="$USERNAME" \
    -e TS_SELENIUM_PASSWORD="$PASSWORD" \
    -e TS_SELENIUM_DEFAULT_TIMEOUT="$DEFAULT_TIMEOUT" \
    -e TS_SELENIUM_WORKSPACE_STATUS_POLLING="$WORKSPACE_STATUS_POLLING" \
    -e TS_SELENIUM_LOAD_PAGE_TIMEOUT="$LOAD_PAGE_TIMEOUT" \
    -e TEST_SUITE="$SUITE" \
    -e NODE_TLS_REJECT_UNAUTHORIZED=0 \
    quay.io/eclipse/che-e2e:nightly || IS_TESTS_FAILED=true
    
    export IS_TESTS_FAILED
}
