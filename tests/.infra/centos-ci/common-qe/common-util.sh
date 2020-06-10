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
PATH_TO_CONFIGURATION_FILE=${PATH_TO_CONFIGURATION_FILE:="$SCRIPT_DIR/common-qe-configuration.conf"}

function printError(){
    >&2 echo ""
    >&2 echo "▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ !!! ERROR !!! ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼"
    >&2 echo ""
    >&2 echo "$1"
    >&2 echo ""
    >&2 echo "▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲"
    >&2 echo ""
}

function readConfigProperty(){
    if [ -z "$1" ]
    then
        printError "The 'readConfigProperty' function can't read property with the 'null' value."
        exit 1
    fi
    
    local propertyRow=$(cat $PATH_TO_CONFIGURATION_FILE | grep $1)
    
    if [ -z "$propertyRow" ]
    then
        printError "Can't read the '$1' property. Please revise config and correct the property name."
        exit 1
    fi
    
    local propertyValue=$(echo "$propertyRow" | sed s@$1=@''@)
    
    echo "$propertyValue"
}

function setConfigProperty(){
    # Check of the property existing
    readConfigProperty $1 > /dev/null
    
    # Set property value
    sed -i s@$1=.*@$1=$2@ $PATH_TO_CONFIGURATION_FILE
    
    echo "Property value has been changed '$1=$2'"
}

function getOpenshiftLogs() {
    echo "====== Che server logs ======"
    oc logs $(oc get pods --selector=component=che -o jsonpath="{.items[].metadata.name}")  || true
    echo "====== Keycloak logs ======"
    oc logs $(oc get pods --selector=component=keycloak -o jsonpath="{.items[].metadata.name}") || true
    echo "====== Che operator logs ======"
    oc logs $(oc get pods --selector=app=che-operator -o jsonpath="{.items[].metadata.name}") || true
}

function archiveArtifacts() {
    JOB_NAME=$1
    DATE=$(date +"%m-%d-%Y-%H-%M")
    echo "Archiving artifacts from ${DATE} for ${JOB_NAME}/${BUILD_NUMBER}"
    cd /root/payload
    ls -la ./artifacts.key
    chmod 600 ./artifacts.key
    chown $(whoami) ./artifacts.key
    mkdir -p ./che/${JOB_NAME}/${BUILD_NUMBER}
    cp -R ./report ./che/${JOB_NAME}/${BUILD_NUMBER}/ || true
    rsync --password-file=./artifacts.key -Hva --partial --relative ./che/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
}
