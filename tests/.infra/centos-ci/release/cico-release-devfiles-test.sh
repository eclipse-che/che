#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
set -x

source tests/.infra/centos-ci/functional_tests_utils.sh
source tests/.infra/centos-ci/release/release_function_util.sh

setupEnvs
installKVM
installDependencies
installAndStartMinishift
prepareCustomResourcePatchFile false
installCheCtl stable
deployCheIntoCluster --che-operator-cr-patch-yaml=/tmp/custom-resource-patch.yaml
runDevfileTestSuite
echo "=========================== THIS IS POST TEST ACTIONS =============================="
getOpenshiftLogs
archiveArtifacts "release-devfile-test"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
