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
set -e

export HAPPY_PATH_REPO="${HAPPY_PATH_REPO:-https://github.com/eclipse/che.git}"
export HAPPY_PATH_REPO_BRANCH="${HAPPY_PATH_REPO_BRANCH:-main}"

prepareAndCloneCodebase(){
  if [ -n "${CODE_BASE_DIR}" ]; then
    # custom dir is specified. Cleaning up that before using
    rm -rf "$CODE_BASE_DIR/*"
  else
    # custom dir is not specified. Preparing the default location
    export CODE_BASE_DIR="/tmp/che-devworkspace-happy-path"
    rm -rf "$CODE_BASE_DIR"
    mkdir "$CODE_BASE_DIR"
  fi

  cd $CODE_BASE_DIR
  git clone $HAPPY_PATH_REPO
  cd che
  git checkout $HAPPY_PATH_REPO_BRANCH
}

prepareAndCloneCodebase
./devworkspace-happy-path-test.sh
