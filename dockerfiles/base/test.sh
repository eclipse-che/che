#!/bin/sh
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Marian Labuda - Initial Implementation

BATS_BASE_DIR=$(cd "$(dirname "$0")"; pwd)
. $BATS_BASE_DIR/../build.include

init "$@"
IMAGE_NAME="eclipse/che-bats:$TAG"

# Runs functional CLI tests in a docker container.
# Pass a file name of functional bats tests as an argument.
#   The file has to be placed in tests folder in directory containing this script
# (Optional) second argument is options for a docker run command.
run_test_in_docker_container() {
  docker run $2 -v $BATS_BASE_DIR:$BATS_BASE_DIR -e CLI_IMAGE_TAG=$TAG -e BATS_BASE_DIR=$BATS_BASE_DIR -v /var/run/docker.sock:/var/run/docker.sock $IMAGE_NAME bats $BATS_BASE_DIR/tests/$1
}

echo "Running tests in container from image $IMAGE_NAME"
echo "Running functional bats tests for CLI prompts and usage"
run_test_in_docker_container cli_prompts_usage_tests.bats
echo "Running functional bats tests for init and destroy commands"
run_test_in_docker_container cmd_init_destroy_tests.bats
echo "Running functionals bats tests for start command"
run_test_in_docker_container cmd_start_tests.bats --net=host

