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

 # It needs to implement the patch of 'devfile' yaml to appropriate release tags images

setupEnvs
installKVM
installDependencies
installAndStartMinishift
loginToOpenshiftAndSetDevRole
prepareCustomResourceFile false
installReleaseCheCtl
deployCheIntoCluster  --che-operator-cr-yaml=/tmp/custom-resource.yaml
createTestUserAndObtainUserToken
createTestWorkspaceAndRunTest  --devfile=https://raw.githubusercontent.com/eclipse/che/cico-rc-test/tests/e2e/files/happy-path/happy-path-workspace.yaml
getOpenshiftLogs
archiveArtifacts "rc-multiuser-happy-path-test"
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi