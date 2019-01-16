#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

init --name:theia "$@"
build
if ! skip_tests; then
  bash "${base_dir}"/e2e/build.sh "$@"
fi

mkdir -p ${base_dir}/theia_artifacts
${base_dir}/extract-for-cdn.sh $IMAGE_NAME ${base_dir}/theia_artifacts
LABEL_CONTENT=$(cat ${base_dir}/theia_artifacts/cdn.json)
if [ "${LABEL_CONTENT}" != "" ]; then
  BUILD_ARGS+="--label che-plugin.cdn.artifacts=$(echo ${LABEL_CONTENT} | sed 's/ //g') "
  echo "Rebuilding with CDN label..."
  build
  "${base_dir}"/push-cdn-files-to-akamai.sh
fi

