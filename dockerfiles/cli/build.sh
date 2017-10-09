#!/bin/sh
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

init --name:cli "$@"

# Define space separated list of aliases for the docker image
IMAGE_ALIASES="${ORGANIZATION}/${PREFIX}"

if [[ ! -f "${base_dir}/version/$TAG/images" ]]; then
    mkdir -p ${base_dir}/version/$TAG/
    cat ${base_dir}/images.template | sed s/\$\{BUILD_ORGANIZATION\}/${ORGANIZATION}/ | sed s/\$\{BUILD_PREFIX\}/${PREFIX}/ | sed s/\$\{BUILD_TAG\}/${TAG}/ > ${base_dir}/version/$TAG/images
fi


build

if ! skip_tests; then
  sh "${base_dir}"/test.sh "$@"
fi
