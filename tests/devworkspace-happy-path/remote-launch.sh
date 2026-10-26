#!/bin/bash
#
# Copyright (c) 2012-2023 Red Hat, Inc.
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
CHE_REPO_BRANCH="${CHE_REPO_BRANCH:-main}"
CHE_REPO_ARCHIVE="${CHE_REPO_ARCHIVE:-https://github.com/eclipse/che/archive/refs/heads/${CHE_REPO_BRANCH}.zip}"

prepareAndCloneCodebase() {
  if [ -n "${CODE_BASE_DIR}" ]; then
    # custom dir is specified. Sanity-check it before wiping: it must be an
    # absolute path to a dedicated directory, not a filesystem root or a
    # top-level system directory
    if [[ "${CODE_BASE_DIR}" != /* ]]; then
      echo "[ERROR] CODE_BASE_DIR '${CODE_BASE_DIR}' must be an absolute path to a dedicated directory, e.g. /tmp/che-devworkspace-happy-path"
      exit 1
    fi
    # normalize '..' and symlinks, then reject filesystem roots and top-level directories
    CODE_BASE_DIR=$(realpath -m "${CODE_BASE_DIR}")
    if [[ "${CODE_BASE_DIR}" =~ ^/([^/]+)?$ ]]; then
      echo "[ERROR] CODE_BASE_DIR '${CODE_BASE_DIR}' resolves to a top-level directory; it must point to a dedicated directory, e.g. /tmp/che-devworkspace-happy-path"
      exit 1
    fi
    echo "Found custom CODE_BASE_DIR. Cleaning up $CODE_BASE_DIR/*"
    rm -rf "${CODE_BASE_DIR:?}"/*
  else
    # custom dir is not specified. Preparing the default location
    export CODE_BASE_DIR="/tmp/che-devworkspace-happy-path"
    echo "CODE_BASE_DIR is not set. Using $CODE_BASE_DIR"
    rm -rf "$CODE_BASE_DIR"
    mkdir "$CODE_BASE_DIR"
  fi

  echo "Downloading $CHE_REPO_ARCHIVE into $CODE_BASE_DIR"
  cd "${CODE_BASE_DIR}"
  curl -f -L -sS "${CHE_REPO_ARCHIVE}" > "${CODE_BASE_DIR}/archive.zip"
  unzip "${CODE_BASE_DIR}/archive.zip"
  rm archive.zip
  cd ./*
}

prepareAndCloneCodebase
echo "Launching DevWorkspace Happy path from $CODE_BASE_DIR"
./tests/devworkspace-happy-path/launch.sh
