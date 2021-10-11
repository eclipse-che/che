#!/bin/bash
#
# Copyright (c) 2012-2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

set -e

# Evaluate default and prepare artifacts directory
export ARTIFACT_DIR=${ARTIFACT_DIR:-"/tmp/dwo-e2e-artifacts"}
mkdir -p "${ARTIFACT_DIR}"

# Collect logs from Che and DevWorkspace Operator
# which is supposed to be executed after test finishes
function collectLogs() {
    bumpPodsInfo "devworkspace-controller"
    bumpPodsInfo "eclipse-che"
    USERS_CHE_NS="che-user-che"
    bumpPodsInfo $USERS_CHE_NS
    # Fetch DW related CRs but do not fail when CRDs are not installed yet
    oc get devworkspace -n $USERS_CHE_NS -o=yaml > ${ARTIFACT_DIR}/devworkspaces.yaml || true
    oc get devworkspacetemplate -n $USERS_CHE_NS -o=yaml > ${ARTIFACT_DIR}/devworkspace-templates.yaml || true
    oc get devworkspacerouting -n $USERS_CHE_NS -o=yaml > ${ARTIFACT_DIR}/devworkspace-routings.yaml || true
    /tmp/chectl/bin/chectl server:logs --directory=${ARTIFACT_DIR}/chectl-server-logs --telemetry=off
}

function bumpPodsInfo() {
    NS=$1
    TARGET_DIR="${ARTIFACT_DIR}/${NS}-info"
    mkdir -p "$TARGET_DIR"

    oc get pods -n ${NS}

    for POD in $(oc get pods -o name -n ${NS}); do
        for CONTAINER in $(oc get -n ${NS} ${POD} -o jsonpath="{.spec.containers[*].name}"); do
            echo ""
            echo "======== Getting logs from container $POD/$CONTAINER in $NS"
            echo ""
            # container name includes `pod/` prefix. remove it
            LOGS_FILE=$TARGET_DIR/$(echo ${POD}-${CONTAINER}.log | sed 's|pod/||g')
            oc logs ${POD} -c ${CONTAINER} -n ${NS} > $LOGS_FILE || true
        done
    done
    echo "======== Bumping events -n ${NS} ========"
    oc get events -n $NS -o=yaml > $TARGET_DIR/events.log || true
}

installChectl() {
  wget $(curl https://che-incubator.github.io/chectl/download-link/next-linux-x64)
  tar -xzf chectl-linux-x64.tar.gz
  mv chectl /tmp
  /tmp/chectl/bin/chectl --version
}
