#!/bin/bash
#
# Copyright (c) 2012-2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# This is the script that is run as PR check to make sure test changes
# don't break DevWorkspace Happy Path E2E test

#!/usr/bin/env bash
# exit immediately when a command fails
set -e
# only exit with zero if all commands of the pipeline exit successfully
set -o pipefail
# error on unset variables
set -u
# print each command before executing it
set -x

SCRIPT_DIR=$(dirname $(readlink -f "$0"))
source "${SCRIPT_DIR}"/common.sh
# Catch the finish of the job and write logs in artifacts.

function Catch_Finish() {
    bumpPodsInfo "devworkspace-controller"
    bumpPodsInfo "eclipse-che"
    USERS_CHE_NS="che-user-che"
    bumpPodsInfo $USERS_CHE_NS
    # Fetch DW related CRs but do not fail when CRDs are not installed yet
    oc get devworkspace -n $USERS_CHE_NS -o=yaml > ${ARTIFACT_DIR}/devworkspaces.yaml || true
    oc get devworkspacetemplate -n $USERS_CHE_NS -o=yaml > ${ARTIFACT_DIR}/devworkspace-templates.yaml || true
    oc get devworkspacerouting -n $USERS_CHE_NS -o=yaml > ${ARTIFACT_DIR}/devworkspace-routings.yaml || true
    /tmp/chectl/bin/chectl server:logs --chenamespace=eclipse-che --directory=${ARTIFACT_DIR}/chectl-server-logs --telemetry=off
}

trap 'Catch_Finish $?' EXIT SIGINT

# ENV used by PROW ci
export CI="openshift"
# Pod created by openshift ci don't have user. Using this envs should avoid errors with git user.
export GIT_COMMITTER_NAME="CI BOT"
export GIT_COMMITTER_EMAIL="ci_bot@notused.com"

deployChe() {
  # create fake DWO CSV to prevent Che Operator getting
  # ownerships of DWO resources
  oc new-project eclipse-che || true
  kubectl apply -f ${SCRIPT_DIR}/resources/fake-dwo-csv.yaml

  /tmp/chectl/bin/chectl server:deploy \
    -p openshift \
    --batch \
    --telemetry=off \
    --installer=operator \
    --workspace-engine=dev-workspace"
  exit 1
}

installChectl
deployChe
"${SCRIPT_DIR}/che-happy-path.sh"
