#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

# HAPPY_PATH_DIR="${base_dir}/../../e2e"
# MVN_DEP_DIR="${base_dir}/e2e"

# if [ -d $MVN_DEP_DIR ]; then
#     rm -rf $MVN_DEP_DIR
# fi

# echo "Copying source code ${HAPPY_PATH_DIR} --> ${MVN_DEP_DIR}"
# cp -r "${HAPPY_PATH_DIR}" "${MVN_DEP_DIR}"

init --name:happy-path "$@"
build
