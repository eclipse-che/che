#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Define space separated list of aliases for the docker image
IMAGE_ALIASES="eclipse/che-cli"

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

init --name:cli "$@"

if [[ ! -f "version/$TAG/images" ]]; then
	mkdir -p version/$TAG
fi

echo "IMAGE_INIT=$ORGANIZATION/$PREFIX-init:$TAG" > version/$TAG/images
echo "IMAGE_CHE=$ORGANIZATION/$PREFIX-server:$TAG" >> version/$TAG/images
echo "IMAGE_COMPOSE=docker/compose:1.8.1" >> version/$TAG/images

build

if ! skip_tests; then
  sh "${base_dir}"/test.sh "$@"
fi
