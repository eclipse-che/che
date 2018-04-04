#!/bin/sh
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Florent Benoit - Initial Implementation
#

# Name of the image to use to run the tests
BASE_DIR=$(cd "$(dirname "$0")"/..; pwd)
. $BASE_DIR/build.include

init --name:bats "$@"

echo "Launching unit tests of $BASE_DIR/ip with docker image ${IMAGE_NAME}:${TAG}"

DOCKER_RUN_OPTIONS=""
BATS_OPTIONS=""
# run bats with terminal mode (pretty print) if supported by current shell
if [ -t 1 ]; then
  DOCKER_RUN_OPTIONS="-t"
  BATS_OPTIONS="--pretty"
else
  BATS_OPTIONS="--tap"
fi

set -x
docker run --rm ${DOCKER_RUN_OPTIONS} -v /var/run/docker.sock:/var/run/docker.sock -v $BASE_DIR/ip:/ip $IMAGE_NAME bats ${BATS_OPTIONS} /ip/tests/library_tests.bats
