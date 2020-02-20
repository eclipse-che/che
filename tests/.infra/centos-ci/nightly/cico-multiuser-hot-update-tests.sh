#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -e

echo "========Starting nigtly test job $(date)========"

source tests/.infra/centos-ci/functional_tests_utils.sh

setupEnvs
installKVM
installDependencies
installDockerCompose
installAndStartMinishift
loginToOpenshiftAndSetDevRole
installCheCtl
deployCheIntoCluster
seleniumTestsSetup

bash tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
--host=${CHE_ROUTE} \
 --port=80 \
 --multiuser \
 --threads=1 \
 --fail-script-on-failed-tests \
 --test=org.eclipse.che.selenium.hotupdate.rolling.** \
 || IS_TESTS_FAILED=true

echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "cico-nightly-hot-update-test"

if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
