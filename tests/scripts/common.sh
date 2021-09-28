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
set -x

# Evaluate default and prepare artifacts directory
export ARTIFACT_DIR=${ARTIFACT_DIR:-"/tmp/dwo-e2e-artifacts"}
mkdir -p "${ARTIFACT_DIR}"

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
