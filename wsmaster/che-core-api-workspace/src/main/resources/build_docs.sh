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
    echo "Configure env var DEVFILE_DOCS_GITHUB_TOKEN or use --ssh argument if you have ssh keys configured"
    echo "Also you is able to use --no-deploy argument if you do not want to push Docs automatically"
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
  cd $RUN_DIR
  cp ../../../README-devfile.md ${TMP_DIR}/docs/index.md
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
   git clone -b 'v2.0.0' --single-branch https://github.com/adobe/jsonschema2md.git
   cd jsonschema2md
   npm install
   npm link
   cd ..
   jsonschema2md -d schema -o docs -n -e json
   echo "Building docs done."
}

deploy() {
   cd ${TMP_DIR}
   BRANCH_ARG=""
   if [[ ! -z "${BASE_BRANCH}" ]]; then
       BRANCH_ARG="-b ${BASE_BRANCH} --single-branch"
   fi

   if [[ "$USE_SSH" == "true" ]]; then
     DOCS_REPOSITORY_URL="git@github.com:${DEPLOY_ORGANIZATION}/devfile.git"
   else
     DOCS_REPOSITORY_URL=https://${GH_TOKEN}@github.com/${DEPLOY_ORGANIZATION}/devfile.git
   fi

   rm -rf devfile && git clone ${BRANCH_ARG} ${DOCS_REPOSITORY_URL}
   cp -f docs/* ./devfile/docs
   cd devfile
   if [[ $(git ls-remote origin ${DEPLOY_BRANCH}) ]]; then
       echo "Specified branch (${DEPLOY_BRANCH}) already exists on remote. That may means that PR of the previous script run is not merged or cleaned. Exiting now."
       exit 1
   fi
   if [[ `git status --porcelain` ]]; then
       git checkout -b "${DEPLOY_BRANCH}"
       git add -A
       git commit -am "${COMMIT_MESSAGE}"
       git push origin "${DEPLOY_BRANCH}"
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
   echo " Build related options"
   echo "   --docker      Build docs in docker container"
   echo "   --folder|-f   If specified then script will save docs files in the specified folder. Examples: -f=.|-f=/home/user"
   echo " Deploy related options"
   echo "   --message     Override default commit message. Example: --message=\"Update Devfile Docs\""
   echo "   --no-deploy   Skip deploy result to remote"
   echo "   --org         Specifies organization of devfile repository to which Docs should be pushed. Default value: redhat-developer"
   echo "   --branch|-b   Specifies branch to which Docs should be pushed. Default is 'docs_renewal'. If branch already exists on remote, error will be thrown."
   echo "   --base-branch Specifies base branch to checkout from. Otherwise repository default branch will be used"
   echo "   --ssh         Configures script to use ssh link to GitHub Docs repository. Default: false"
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
           --message=*)
               COMMIT_MESSAGE="${i#*=}"
               shift
           ;;
           -f=*| --folder=*)
               FOLDER="${i#*=}"
               shift
           ;;
           --org=*)
               DEPLOY_ORGANIZATION="${i#*=}"
               shift
           ;;
           --branch=*)
               DEPLOY_BRANCH="${i#*=}"
               shift
           ;;
           --base-branch=*)
               BASE_BRANCH="${i#*=}"
               shift
           ;;
           --ssh)
               USE_SSH="true"
               shift
           ;;
           -help|--help)
               print_help
               exit 0
               ;;
           *)
               echo "You've passed wrong arg '$i'."
               print_help
               exit 1
               ;;
        esac
     done
}

copyDocs() {
  cd $RUN_DIR
  cp -r ${TMP_DIR}/docs/. -t ${FOLDER}
  echo "Docs is saved into ${FOLDER}"
}

RUN_DIR=$(pwd)
TMP_DIR="/tmp/devfile"

DEFAULT_BUILD_DOCKER=false
IS_DOCKER=${IS_DOCKER:-${DEFAULT_BUILD_DOCKER}}

DEFAULT_DEPLOY=true
IS_DEPLOY=${IS_DEPLOY:-${DEFAULT_DEPLOY}}

DEFAULT_COMMIT_MESSAGE="Update devfile docs to the latest version"
COMMIT_MESSAGE=${COMMIT_MESSAGE:-${DEFAULT_COMMIT_MESSAGE}}

DEFAULT_DEPLOY_ORGANIZATION=redhat-developer
DEPLOY_ORGANIZATION=${DEPLOY_ORGANIZATION:-${DEFAULT_DEPLOY_ORGANIZATION}}

DEFAULT_DEPLOY_BRANCH="docs_renewal"
DEPLOY_BRANCH=${DEFAULT_DEPLOY_BRANCH:-${DEPLOY_BRANCH}}

DEFAULT_BASE_BRANCH="" # means use default branch of GitHub repository
BASE_BRANCH=${DEFAULT_BASE_BRANCH:-${BASE_BRANCH}}

DEFAULT_USE_SSH="false"
USE_SSH=${DEFAULT_USE_SSH:-${USE_SSH}}

parse_args "$@"

if [[ "$IS_DEPLOY" == "true" && "$USE_SSH" == "false" ]]; then
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
