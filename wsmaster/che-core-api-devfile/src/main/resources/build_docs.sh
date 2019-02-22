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

check_github_token_is_set() {
  if [[ -z "${DEVFILE_DOCS_GITHUB_TOKEN}" ]]; then
    echo "GitHub token not found."
    echo "Specify env var DEVFILE_DOCS_GITHUB_TOKEN or use --no-deploy argument if you do not want to push Docs automatically"
    echo "Exiting now..."
    exit 1
  else
    GH_TOKEN="${DEVFILE_DOCS_GITHUB_TOKEN}"
  fi
}

build() {
  if [[ "$IS_DOCKER" == "true" ]]; then
    build_with_docker
  else
    build_native
  fi
}

build_with_docker() {
    echo "Building docs using docker."
    check_packages docker tar git
    DOCKER_IMAGE_NAME="eclipse-che-devfile-docs"
    docker build -t ${DOCKER_IMAGE_NAME} .
    mkdir -p ${TMP_DIR}/docs && cd ${TMP_DIR}
    docker run --rm ${DOCKER_IMAGE_NAME} | tar -C docs/ -xf -
    echo "Building docs done."
}

build_native() {
   echo "Building docs using native way."
   check_packages git npm
   mkdir -p ${TMP_DIR}/schema
   cp -f schema/* ${TMP_DIR}/schema
   cd ${TMP_DIR}
   git clone -b '1.1.0' --single-branch https://github.com/adobe/jsonschema2md.git
   cd jsonschema2md
   npm install
   npm link
   cd ..
   jsonschema2md -d schema -o docs -n -e json
   mv ./docs/devfile.md ./docs/index.md
   echo "Building docs done."
}

deploy() {
   rm -rf devfile && git clone https://${GH_TOKEN}@github.com/redhat-developer/devfile.git
   cp -f docs/* ./devfile/docs
   cd devfile
   if [[ `git status --porcelain` ]]; then
       git commit -am "${COMMIT_MESSAGE}"
       git push
   else
       echo "No changes in docs."
   fi
}

check_packages() {
   for var in "$@"
   do
      if command -v $var >> /dev/null
      then
        echo "Package $var is installed, continue..."
      else
        echo "Required package $var is NOT installed. Exiting now."
        exit 1
      fi
   done
}

cleanupTmpDir() {
   rm -rf ${TMP_DIR}
}

print_help() {
   echo "This script builds and deploys documentation in markdown format from devfile json schema."
   echo "Command line options:"
   echo "--docker      Build docs in docker container"
   echo "--no-deploy   Skip deploy result to remote"
   echo "--message     Override default commit message"
   echo "--folder|-f   If specified then script will save docs files in the specified folder. Examples: -f=.|-f=/home/user"
}

parse_args() {
    for i in "${@}"
    do
        case $i in
           --docker)
               IS_DOCKER=true
               shift
           ;;
           --no-deploy)
               IS_DEPLOY=false
               shift
           ;;
           --message)
               MESSAGE="${i#*=}"
               shift
           ;;
           -f=*| --folder=*)
               FOLDER="${i#*=}"
               shift
           ;;
            -help|--help)
               print_help
               exit 0
               ;;
            *)
                echo "You've passed wrong arg '$key'."
                echo -e "$HELP"
                exit 1
                ;;
        esac
     done
}

copyDocs() {
  cd $RUN_DIR
  cp -r ${TMP_DIR}/docs/. -t ${FOLDER}
  echo "Docs is saved as ${FOLDER}/index.md"
}

RUN_DIR=$(pwd)
TMP_DIR="/tmp/devfile"

DEFAULT_COMMIT_MESSAGE="Update devfile docs"
COMMIT_MESSAGE=${MESSAGE:-${DEFAULT_COMMIT_MESSAGE}}

DEFAULT_BUILD_DOCKER=false
IS_DOCKER=${IS_DOCKER:-${DEFAULT_BUILD_DOCKER}}

DEFAULT_DEPLOY=true
IS_DEPLOY=${IS_DEPLOY:-${DEFAULT_DEPLOY}}

DEFAULT_DO_CLEANUP=true
DO_CLEANUP=${DO_CLEANUP:-${DEFAULT_DO_CLEANUP}}

parse_args "$@"

if [[ "$IS_DEPLOY" == "true" ]]; then
  check_github_token_is_set
fi

cleanupTmpDir

build

if [[ "$IS_DEPLOY" == "true" ]]; then
    deploy
fi

if [[ "$FOLDER" ]]; then
    copyDocs
fi

echo "Working Dir where script results can be found is ${TMP_DIR}"

exit 0
