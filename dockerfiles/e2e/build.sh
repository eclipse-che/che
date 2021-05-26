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

E2E_DIR="${base_dir}/../../tests/e2e"
LOCAL_E2E_DIR="${base_dir}/e2e"

if [ -d $LOCAL_E2E_DIR ]; then
    rm -rf $LOCAL_E2E_DIR
fi

echo "Copying source code ${E2E_DIR} --> ${LOCAL_E2E_DIR}"
cp -r "${E2E_DIR}" "${LOCAL_E2E_DIR}"

init --name:e2e "$@"
build

# cleanup
rm -rf $LOCAL_E2E_DIR
