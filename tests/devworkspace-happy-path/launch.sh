#!/bin/bash
#
# Copyright (c) 2012-2023 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# exit immediately when a command fails
set -e
# only exit with zero if all commands of the pipeline exit successfully
set -o pipefail
# error on unset variables
set -u
# uncomment to print each command before executing it
# set -x

START=$(date +%s.%N)
SCRIPT_DIR=$(dirname $(readlink -f "$0"))

export CHE_NAMESPACE="${CHE_NAMESPACE:-eclipse-che}"
export HAPPY_PATH_POD_NAME=happy-path-che

source "${SCRIPT_DIR}/common.sh"

# Catch the finish of the job and write logs in artifacts.
trap 'collectLogs $?' EXIT SIGINT

. "${SCRIPT_DIR}/provision-openshift-oauth-user.sh"
. "${SCRIPT_DIR}/start-happypath-tests.sh"

END=$(date +%s.%N)
echo "[INFO] Happy-path execution took $(echo "$END - $START" | bc) seconds."
