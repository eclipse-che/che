#!/bin/bash
#
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

function obtainUserToken() {
    local username=$(readConfigProperty test.username)
    local password=$(readConfigProperty test.password)
    KEYCLOAK_URL=$(oc get checluster eclipse-che -o jsonpath='{.status.keycloakURL}')
    KEYCLOAK_BASE_URL="${KEYCLOAK_URL}/auth"
    
    local userAccessToken=$(curl -k -v -X POST $KEYCLOAK_BASE_URL/realms/che/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=${username}" -d "password=${password}" -d "grant_type=password" -d "client_id=che-public" | jq -r .access_token)
    
    echo "$userAccessToken"
}

function installChectl(){
    bash <(curl -sL https://www.eclipse.org/che/chectl/) --channel=next
}

function createServerPatchFile(){
    if [ -z "$1" ]
    then
        echo "Patch template has not been provided"
        exit 1
    fi
    
    echo "$1" > /tmp/che-cr-patch.yaml
}

function startCheServer(){
    createServerPatchFile "$1"
    
    if chectl server:deploy --telemetry=off --listr-renderer=verbose -a operator -p openshift --k8spodreadytimeout=600000 --k8spodwaittimeout=600000 --k8spoddownloadimagetimeout=600000 --che-operator-cr-patch-yaml=/tmp/che-cr-patch.yaml --chenamespace=eclipse-che; then
        echo "Started successfully"
        oc get checluster -o yaml
    else
        echo "======== oc get events ========"
        oc get events
        echo "======== oc get all ========"
        oc get all
        getOpenshiftLogs
        oc get checluster -o yaml || true
        exit 133
    fi
}

function createTestWorkspace(){
    local devfile_url=$(readConfigProperty test.workspace.devfile.url)
    local userAccessToken=$(obtainUserToken)
    
    chectl workspace:create --start --access-token "$userAccessToken" --telemetry=off --chenamespace=eclipse-che --devfile="$devfile_url"
}

function runTest() {
    local username=$(readConfigProperty test.username)
    local password=$(readConfigProperty test.password)
    local suite=$(readConfigProperty test.suite)
    local multiuser=$(readConfigProperty test.multiuser)
    local default_timeout=$(readConfigProperty timeout.default)
    local workspace_status_polling=$(readConfigProperty timeout.workspace.status.polling)
    local load_page_timeout=$(readConfigProperty timeout.load.page)
    local additional_options=$(readConfigProperty test.additional.options)
    local che_url=$(oc get checluster eclipse-che -o jsonpath='{.status.cheURL}')
    
    # ### Create directory for report
    cd /root/payload
    mkdir report
    REPORT_FOLDER=$(pwd)/report
    
    ### Run tests
    docker run --shm-size=1g --net=host  --ipc=host -v $REPORT_FOLDER:/tmp/e2e/report:Z \
    -e TS_SELENIUM_BASE_URL="$che_url" \
    -e TS_SELENIUM_LOG_LEVEL=DEBUG \
    -e TS_SELENIUM_MULTIUSER="$multiuser" \
    -e TS_SELENIUM_USERNAME="$username" \
    -e TS_SELENIUM_PASSWORD="$password" \
    -e TS_SELENIUM_DEFAULT_TIMEOUT="$default_timeout" \
    -e TS_SELENIUM_WORKSPACE_STATUS_POLLING="$workspace_status_polling" \
    -e TS_SELENIUM_LOAD_PAGE_TIMEOUT="$load_page_timeout" \
    -e TEST_SUITE="$suite" \
    -e NODE_TLS_REJECT_UNAUTHORIZED=0 \
    $additional_options quay.io/eclipse/che-e2e:next || IS_TESTS_FAILED=true
    
    export IS_TESTS_FAILED
}
