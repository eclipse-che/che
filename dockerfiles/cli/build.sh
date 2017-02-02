#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Primary name of the docker image
IMAGE_NAME="eclipse/che"

# Define space separated list of aliases for the docker image
IMAGE_ALIASES="eclipse/che-cli"

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

init "$@"
build


if [ $(skip_tests "$@") = false ]; then
  sh "${base_dir}"/test.sh $TAG
fi
