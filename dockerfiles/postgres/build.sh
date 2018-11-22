#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

DIR=$(cd "$(dirname "$0")"; pwd)
# copy user and realm json templates
cp -r "${DIR}"/../init/modules/postgres/templates/* "${DIR}"

init --name:postgres "$@"
build

# remove jsons
rm "${DIR}"/*.erb
