#!/bin/bash
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

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
  bash "${base_dir}"/test.sh "$@"
fi
