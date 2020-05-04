#!/usr/bin/env bash
# Copyright (c) 2020 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -e
set +x

source tests/.infra/centos-ci/functional_tests_utils.sh
source tests/.infra/centos-ci/rc/rc_function_util.sh

setupEnvs
installDependencies
installDockerCompose
installKVM
installAndStartMinishift
loginToOpenshiftAndSetDevRole
prepareCustomResourceFile false
installReleaseCheCtl
deployCheIntoCluster  --chenamespace=eclipse-che --che-operator-cr-yaml=/tmp/custom-resource.yaml
createIndentityProvider
seleniumTestsSetup

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
archiveArtifacts "rc-multiuser-integration-tests"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
