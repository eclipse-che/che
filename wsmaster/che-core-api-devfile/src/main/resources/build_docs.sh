#!/bin/bash
#
# Copyright (c) 2012-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

set -e

TMP_DIR="/tmp/devfile"

if [[ -z "${DEVFILE_DOCS_GITHUB_TOKEN}" ]]; then
  echo "GitHub token not found, exiting now..."
  exit 1
else
  GH_TOKEN="${DEVFILE_DOCS_GITHUB_TOKEN}"
fi

build_with_docker() {
    echo "Building docs using docker."
    check_packages docker tar git
    DOCKER_IMAGE_NAME="eclipse-che-devfile-docs"
    docker build -t ${DOCKER_IMAGE_NAME} .
    mkdir -p ${TMP_DIR} && cd ${TMP_DIR}
    mkdir -p docs
    docker run --rm ${DOCKER_IMAGE_NAME} | tar -C docs/ -xf -
    echo "Building docs done."
}


build_native() {
   echo "Building docs using native way."
   check_packages git npm
   mkdir -p ${TMP_DIR}/schema
   cp -f schema/* ${TMP_DIR}/schema
   cd ${TMP_DIR}
   git clone git@github.com:adobe/jsonschema2md.git
   cd jsonschema2md
   npm install
   npm link
   cd ..
   jsonschema2md -d schema -o docs -n -e json
   mv ./docs/devfile.md ./docs/index.md
   echo "Building docs done."
}

upload() {
   rm -rf devfile && git clone https://${GH_TOKEN}@github.com/redhat-developer/devfile.git
   cp -f docs/* ./devfile/docs
   cd devfile
   if [[ `git status --porcelain` ]]; then
       git commit -am "Update devfile docs"
       git push
   else
       echo "No changes in docs."
   fi
}

check_packages() {
   for var in "$@"
   do
      if rpm -q $var >> /dev/null
      then
        echo "Package $var is installed, continue..."
      else
        echo "Required package $var is NOT installed. Exiting now."
        exit 1
      fi
   done
}

cleanup() {
   rm -rf ${TMP_DIR}
}

cleanup
if [[ "$1" == "--docker" ]];
then
    build_with_docker
else
    build_native
fi
upload
cleanup
exit 0
