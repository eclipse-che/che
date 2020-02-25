#!/usr/bin/env bash
# Copyright (c) 2020 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

set -e
set +x

source tests/.infra/centos-ci/functional_tests_utils.sh

eval "$(./env-toolkit load -f jenkins-env.json -r ^ghprbPullId)"

export PULL_REQUEST_ID="${ghprbPullId}"
export TAG=PR-${PULL_REQUEST_ID}

function prepareCustomResourceFile() {
  cd /tmp
  wget https://raw.githubusercontent.com/eclipse/che-operator/master/deploy/crds/org_v1_che_cr.yaml -O custom-resource.yaml
  sed -i "s@server:@server:\n    customCheProperties:\n      CHE_LIMITS_USER_WORKSPACES_RUN_COUNT: '-1'@g" /tmp/custom-resource.yaml
  sed -i "s/customCheProperties:/customCheProperties:\n      CHE_WORKSPACE_AGENT_DEV_INACTIVE__STOP__TIMEOUT__MS: '300000'/" /tmp/custom-resource.yaml
  sed -i "s@cheImage: ''@cheImage: 'quay.io/eclipse/che-server'@g" /tmp/custom-resource.yaml
  sed -i "s@cheImageTag: 'nightly'@cheImageTag: '${TAG}'@g" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}

function buidCheServer() {
  mvn clean install -Pintegration
  bash dockerfiles/che/build.sh --organization:quay.io/eclipse --tag:${TAG} --dockerfile:Dockerfile
}

function pushImageToRegistry() {
  docker login -u "${QUAY_ECLIPSE_CHE_USERNAME}" -p "${QUAY_ECLIPSE_CHE_PASSWORD}" "quay.io"
  docker push "quay.io/eclipse/che-server:${TAG}"
}

setupEnvs
installDependencies
installDockerCompose
buidCheServer
pushImageToRegistry
installKVM
installAndStartMinishift
loginToOpenshiftAndSetDevRole
prepareCustomResourceFile
installCheCtl

deployCheIntoCluster  --chenamespace=eclipse-che --che-operator-cr-yaml=/tmp/custom-resource.yaml
seleniumTestsSetup
createIndentityProvider

bash /root/payload/tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
  --threads=3 \
  --host=${CHE_ROUTE} \
  --port=80 \
  --multiuser \
  --fail-script-on-failed-tests \
  || IS_TESTS_FAILED=true


echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "che-pullrequests-java-selenium-tests"

if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
