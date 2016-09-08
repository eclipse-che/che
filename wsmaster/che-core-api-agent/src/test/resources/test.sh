#!/bin/bash
#
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

. ./lib.sh


${CHE_PATH}/che.sh --debug start
sleep 20s

DOCKER_CONTENT=("FROM fedora:23\nCMD tail -f /dev/null"
                "FROM ubuntu:16.04\nCMD tail -f /dev/null"
                "FROM ubuntu:14.04\nCMD tail -f /dev/null"
                "FROM centos:7\nCMD tail -f /dev/null"
                "FROM opensuse:13.2\nCMD tail -f /dev/null"
                "FROM debian:8\nCMD tail -f /dev/null");

waitStatus() {
    WORKSPACE_ID=$1
    STATUS=$2
    ATTEMPTS=200

    for ((j = 0; j < ${ATTEMPTS}; j++))
    do
        doGet "http://"${HOST_URL}"/api/workspace/"${WORKSPACE_ID}
        if echo ${OUTPUT} | grep -qi "\"id\":\"${WORKSPACE_ID}\",\"status\":\"${STATUS}\""; then
            echo ${STATUS}
            break
        fi
        sleep 5s
    done
}

validateRunningProcess() {
    WORKSPACE_ID=$1
    PROCESS=$2

    CONTAINER_ID=$(docker ps -aqf "name="${WORKSPACE_ID})
    printAndLog "Container ID "${CONTAINER_ID}" validate "${PROCESS}

    PID=$(docker exec -ti ${CONTAINER_ID} ps -fC ${PROCESS})
    if [ "${PID}" = "" ]; then
        printAndLog "RESULT: FAILED. Process "${PROCESS}" not found"
    fi
}

deleteWorkspace() {
    WORKSPACE_ID=$1

    doDelete "http://"${HOST_URL}"/api/workspace/"${WORKSPACE_ID}"/runtime"
    STATUS=$(waitStatus ${WORKSPACE_ID} "STOPPED")
    if echo ${STATUS} | grep -qi "STOPPED"; then
        doDelete "http://"${HOST_URL}"/api/workspace/"${WORKSPACE_ID}
        sleep 1m
    else
        printAndLog "ERROR. Workspace "${WORKSPACE_ID}" can't be stopped"
    fi
}

for ((i = 0; i < ${#DOCKER_CONTENT[@]}; i++))
do
    CONTENT="${DOCKER_CONTENT[$i]}"
    printAndLog
    printAndLog "####################################################################"
    printAndLog "Creating workspace with recipe: \""${CONTENT}"\""
    printAndLog "####################################################################"

    NAME=$(date +%s | sha256sum | base64 | head -c 5)
    doPost "application/json" "{\"name\":\"${NAME}\",\"projects\":[],\"defaultEnv\":\"${NAME}\",\"description\":null,\"environments\":[{\"name\":\"${NAME}\",\"recipe\":null,\"machineConfigs\":[{\"name\":\"ws-machine\",\"limits\":{\"ram\":1000},\"type\":\"docker\",\"source\":{\"type\":\"dockerfile\",\"content\":\"${CONTENT}\"},\"dev\":true}]}]}" "http://"${HOST_URL}"/api/workspace?account="
    fetchJsonParameter "id"
    WORKSPACE_ID=${OUTPUT}

    printAndLog "Starting workspace: "${WORKSPACE_ID}
    doPost "application/json" "{}" "http://"${HOST_URL}"/api/workspace/"${WORKSPACE_ID}"/runtime?environment="${NAME}

    STATUS=$(waitStatus ${WORKSPACE_ID} "RUNNING")
    if echo ${STATUS} | grep -qi "RUNNING"; then
        validateRunningProcess ${WORKSPACE_ID} "che-websocket-terminal"
        validateRunningProcess ${WORKSPACE_ID} "java"
        validateRunningProcess ${WORKSPACE_ID} "sshd"
    else
        printAndLog "RESULT: FAILED. Workspace not started."
    fi

    deleteWorkspace ${WORKSPACE_ID}
done
