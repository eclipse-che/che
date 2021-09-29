#!/bin/bash
#
# Copyright (c) 2012-2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# This scripts helps you to set up temporary folder with everything
# needed for running Che DevWorkspace Happy Path test which is located
# remotely, like in another git repo

export HAPPY_PATH_REPO="${HAPPY_PATH_REPO:-https://github.com/eclipse/che.git}"
export HAPPY_PATH_REPO_BRANCH="${HAPPY_PATH_REPO:-main}"

if [ $WORKDIR != ""] {
    export WORKDIR="/tmp/che-devworkspace-happy-path"
    rm -rf $WORKDIR/*
}

# downoad git repo as arhive
# make scripts available in WORK_DIR
