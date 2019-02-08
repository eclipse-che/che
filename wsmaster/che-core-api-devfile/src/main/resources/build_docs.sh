#!/bin/bash
#
# Copyright (c) 2012-2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
set -e

build_with_docker() {
    echo "Building docs using docker."
    check_packages docker tar git
    DOCKER_IMAGE_NAME="eclipse-che-devfile-docs"
    docker build -t ${DOCKER_IMAGE_NAME} .
    cd ../../../target
    mkdir -p docs
    docker run --rm ${DOCKER_IMAGE_NAME} | tar -C docs/ -xf -
    echo "Building docs done."
}


build_native() {
   echo "Building docs using native way."
   check_packages git npm
   cd ../../../target
   rm -rf jsonschema2md && git clone git@github.com:adobe/jsonschema2md.git
   cd jsonschema2md
   npm install
   npm link
   cd ..
   jsonschema2md -d ../src/main/resources/schema -o docs -n -e json
   rm -rf jsonschema2md
   echo "Building docs done."
}

upload() {
   rm -rf devfile && git clone git@github.com:redhat-developer/devfile.git
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

if [[ "$1" == "--docker" ]]; 
then
    build_with_docker
else
    build_native
fi
upload
exit 0
