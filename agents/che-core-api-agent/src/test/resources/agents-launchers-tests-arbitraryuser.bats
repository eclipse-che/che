#!/usr/bin/env bats
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Mario
#
# How to run this script:
#   cd <root of che local git repository>
#   export CHE_BASE_DIR=$(pwd)
#   export LAUNCHER_SCRIPT_TO_TEST=wsagent/agent/src/main/resources/org.eclipse.che.ws-agent.script.sh
#   export BATS_TEST_SCRIPT=agents/che-core-api-agent/src/test/resources/agents-launchers-tests-arbitraryuser.bats
#   export DOCKER_IMAGE=rhche/centos_jdk8
#   docker run -ti --rm -e CHE_BASE_DIR -e LAUNCHER_SCRIPT_TO_TEST -e DOCKER_IMAGE \
#        -v ${CHE_BASE_DIR}/${BATS_TEST_SCRIPT}:/scripts/launcher_tests.bats \
#        -v ${CHE_BASE_DIR}/dockerfiles:/dockerfiles \
#        -v /var/run/docker.sock:/var/run/docker.sock \
#        eclipse/che-bats bats /scripts/launcher_tests.bats
#

load '/bats-support/load.bash'
load '/bats-assert/load.bash'
. /dockerfiles/cli/tests/test_base.sh

CONTAINER_NAME="test"

script_host_path=${CHE_BASE_DIR}/${LAUNCHER_SCRIPT_TO_TEST}

root_msg="I am root"
not_root_msg="I am a not root"
sudoer_msg="I am a sudoer"
not_sudoer_msg="I am a not a sudoer"
test_snippet="source <(grep -iE -A3 'is_current_user_root\(\)|is_current_user_sudoer\(\)|set_sudo_command\(\)' /launch.sh | grep -v -- "^--$"); is_current_user_root && echo -n '${root_msg} ' || echo -n '${not_root_msg} '; is_current_user_sudoer && echo '${sudoer_msg}' || echo -n '${not_sudoer_msg} '; set_sudo_command; echo SUDO=\${SUDO}"
user="100000"

# Kill running che server instance if there is any to be able to run tests
setup() {
  kill_running_named_container ${CONTAINER_NAME}
  remove_named_container ${CONTAINER_NAME}
  docker run --security-opt no-new-privileges --user=${user} --name="${CONTAINER_NAME}" -d -v ${script_host_path}:/launch.sh "${DOCKER_IMAGE}"
}

teardown() {
  kill_running_named_container "${CONTAINER_NAME}"
  remove_named_container ${CONTAINER_NAME}
}

@test "should deduce that's not a sudoer nor root when ${LAUNCHER_SCRIPT_TO_TEST} is run as an arbitrary user" {
  #GIVEN
  expected_msg="${not_root_msg} ${not_sudoer_msg} SUDO="

  #WHEN
  run docker exec --user=${user} "${CONTAINER_NAME}" bash -c "${test_snippet}"

  #THEN
  assert_success
  assert_output ${expected_msg}
}

