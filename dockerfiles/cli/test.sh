#!/bin/sh
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Marian Labuda - Initial Implementation

BATS_BASE_DIR=$(cd "$(dirname "$0")"/..; pwd)
. "${BATS_BASE_DIR}"/build.include
BATS_BASE_DIR=$(get_mount_path "${BATS_BASE_DIR}")

init --name:bats "$@"

DOCKER_RUN_OPTIONS=""
BATS_OPTIONS=""
# run bats with terminal mode (pretty print) if supported by current shell
if [ -t 1 ]; then
  DOCKER_RUN_OPTIONS="-t"
  BATS_OPTIONS="--pretty"
else
  BATS_OPTIONS="--tap"
fi

# Runs functional CLI tests in a docker container.
# Pass a file name of functional bats tests as an argument.
#   The file has to be placed in tests folder in directory containing this script
# (Optional) second argument is options for a docker run command.
run_test_in_docker_container() {
  docker_exec run --rm ${DOCKER_RUN_OPTIONS} $2 \
       -v "${BATS_BASE_DIR}":/dockerfiles \
       -e CLI_IMAGE="$ORGANIZATION/$PREFIX-cli:$TAG" \
       -e BATS_BASE_DIR="${BATS_BASE_DIR}" \
       -v /var/run/docker.sock:/var/run/docker.sock \
           $IMAGE_NAME bats ${BATS_OPTIONS} /dockerfiles/cli/tests/$1
}

echo "Running tests in container from image $IMAGE_NAME"
echo "Running functionals bats tests for overriding images"
run_test_in_docker_container override_image_tests.bats ""
echo "Running functional bats tests for CLI prompts and usage"
run_test_in_docker_container cli_prompts_usage_tests.bats ""
echo "Running functionals bats tests for config command"
run_test_in_docker_container cmd_config_tests.bats ""
echo "Running functionals bats tests for info command"
run_test_in_docker_container cmd_info_tests.bats ""
echo "Running functional bats tests for init and destroy commands"
run_test_in_docker_container cmd_init_destroy_tests.bats ""
echo "Running functionals bats tests for start, stop, restart command"
run_test_in_docker_container cmd_start_stop_restart_tests.bats --net=host
echo "Running functionals bats tests for backup / restore commands"
run_test_in_docker_container cmd_backup_restore_tests.bats ""
echo "Running functionals bats tests for offline command"
run_test_in_docker_container cmd_offline_tests.bats ""
run_test_in_docker_container startup_03_pre_networking_tests.bats ""
