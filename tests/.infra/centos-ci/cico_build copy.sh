#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Just a script to get and build eclipse-che locally
# please send PRs to github.com/kbsingh/build-run-che

# update machine, get required deps in place
# this script assumes its being run on CentOS Linux 7/x86_64

currentDir=$(pwd)
ciDir=$(dirname "$0")
ABSOLUTE_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ "$DeveloperBuild" != "true" && "$PR_CHECK_BUILD" != "true" && "$USE_CHE_LATEST_SNAPSHOT" != "true" ]]; then
  set +x

  eval "$(./env-toolkit load -f jenkins-env.json -r PASS DEVSHIFT ^QUAY)"

  if [ -z "${QUAY_USERNAME}" ]; then
    echo "WARNING: failed to get QUAY_USERNAME from jenkins-env file in centos-ci job."
  fi

  if [ -z "${QUAY_PASSWORD}" ]; then
    echo "WARNING: failed to get QUAY_PASSWORD from jenkins-env file in centos-ci job."
  fi

  set -x
  yum -y update
  yum -y install centos-release-scl java-1.8.0-openjdk-devel git patch bzip2 golang docker
  yum -y install rh-maven33 rh-nodejs8

  BuildUser="chebuilder"

  useradd ${BuildUser}
  groupadd docker
  gpasswd -a ${BuildUser} docker

  systemctl start docker

  chmod a+x ..
  chown -R ${BuildUser}:${BuildUser} ${currentDir}

  runBuild() {
    runuser - ${BuildUser} -c "$*"
  }
else
  runBuild() {
    eval $*
  }
fi

source ${ABSOLUTE_PATH}/../config

runBuild "cd ${ABSOLUTE_PATH} && bash ./cico_do_build_che.sh $*"
if [ $? -eq 0 ]; then
  bash ${ABSOLUTE_PATH}/cico_do_docker_build_tag_push.sh
else
  echo 'Build Failed!'
  exit 1
fi
