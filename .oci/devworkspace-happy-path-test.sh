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
# uncomment to print each command before executing it
# set -x

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
TEST_SCRIPT_DIR="${SCRIPT_DIR%/*}/tests/devworkspace-happy-path"

# ENV used by PROW ci
export CI="openshift"
# Pod created by openshift ci don't have user. Using this envs should avoid errors with git user.
export GIT_COMMITTER_NAME="CI BOT"
export GIT_COMMITTER_EMAIL="ci_bot@notused.com"

deployChe() {
  /tmp/chectl/bin/chectl server:deploy \
    -p openshift \
    --batch \
    --telemetry=off \
    --installer=operator \
    --workspace-engine=dev-workspace
}

installChectl
deployChe
"${TEST_SCRIPT_DIR}"/che-devworkspace-happy-path.sh
