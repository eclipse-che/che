#!/bin/sh
# Copyright (c) 2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Florent Benoit - Initial Implementation
#

# Name of the image to use to run the tests
IMAGE_NAME="eclipse/che-bats"

BASE_DIR=$(cd "$(dirname "$0")"/..; pwd)
. $BASE_DIR/build.include

init "$@"

echo "Launching unit tests of $BASE_DIR/ip with docker image ${IMAGE_NAME}:${TAG}"

set -x
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v $BASE_DIR/ip:/ip $IMAGE_NAME:${TAG} bats /ip/tests/library_tests.bats
