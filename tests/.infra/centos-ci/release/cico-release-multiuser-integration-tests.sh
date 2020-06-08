#!/usr/bin/env bash
# Copyright (c) 2020 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -e
set +x

source tests/.infra/centos-ci/functional_tests_utils.sh
source tests/.infra/centos-ci/release/release_function_util.sh

setupEnvs
installDependencies
installDockerCompose
installKVM
installAndStartMinishift
prepareCustomResourcePatchFile false
installCheCtl stable
deployCheIntoCluster --che-operator-cr-patch-yaml=/tmp/custom-resource-patch.yaml
createIndentityProvider
seleniumTestsSetup

bash /root/payload/tests/legacy-e2e/che-selenium-test/selenium-tests.sh \
   --threads=3 \
   --host=${CHE_ROUTE} \
   --https \
   --port=443 \
   --multiuser \
   --fail-script-on-failed-tests \
   || IS_TESTS_FAILED=true


echo "=========================== THIS IS POST TEST ACTIONS =============================="
saveSeleniumTestResult
getOpenshiftLogs
archiveArtifacts "release-multiuser-integration-tests"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
