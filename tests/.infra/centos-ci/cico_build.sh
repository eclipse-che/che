#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

function archiveArtifacts1() {
        set +e
        JOB_NAME=che-nightly
        echo "Archiving artifacts from ${DATE} for ${JOB_NAME}/${BUILD_NUMBER}"
        ls -la ./artifacts.key
        chmod 600 ./artifacts.key
        chown $(whoami) ./artifacts.key
        mkdir -p ./che/${JOB_NAME}/${BUILD_NUMBER}
        cp -R ./report ./che/${JOB_NAME}/${BUILD_NUMBER}/ | true
        rsync --password-file=./artifacts.key -Hva --partial --relative ./che/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
        set -e
}

set -e

echo "========Starting nigtly test job $(date)========"

total_start_time=$(date +%s)

export PR_CHECK_BUILD="true"
export BASEDIR=$(pwd)

eval "$(./env-toolkit load -f jenkins-env.json \
        CHE_BOT_GITHUB_TOKEN \
        CHE_MAVEN_SETTINGS \
        CHE_GITHUB_SSH_KEY \
        CHE_OSS_SONATYPE_GPG_KEY \
        CHE_OSS_SONATYPE_PASSPHRASE \
        QUAY_ECLIPSE_CHE_USERNAME \
        QUAY_ECLIPSE_CHE_PASSWORD)"

source tests/.infra/centos-ci/functional_tests_utils.sh

checkAllCreds
installDependencies
installKVM
installAndStartMinishift
loginToOpenshiftAndSetDevRople
installCheCtl
deployCheIntoCluster
createTestUserAndObtainUserToken

CHE_ROUTE=$(oc get route che --template='{{ .spec.host }}')

curl -vL $CHE_ROUTE



### Create workspace

chectl workspace:start --access-token "$USER_ACCESS_TOKEN" -f https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/happy-path/happy-path-workspace.yaml

### Run tests
mkdir report
REPORT_FOLDER=$(pwd)/report

set +e
docker run --shm-size=256m --network host -v $REPORT_FOLDER:/tmp/e2e/report:Z -e TS_SELENIUM_BASE_URL="http://$CHE_ROUTE" -e TS_SELENIUM_MULTIUSER="true" -e TS_SELENIUM_USERNAME="${TEST_USERNAME}" -e TS_SELENIUM_PASSWORD="${TEST_USERNAME}" -e TS_SELENIUM_LOAD_PAGE_TIMEOUT=240000 eclipse/che-e2e:nightly
set -e

### Archive artifacts

archiveArtifacts1
