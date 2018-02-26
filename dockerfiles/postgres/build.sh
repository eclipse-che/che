#!/bin/sh
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

DIR=$(cd "$(dirname "$0")"; pwd)
# copy user and realm json templates
cp -r "${DIR}"/../init/modules/postgres/templates/* "${DIR}"

init --name:postgres "$@"
build

# remove jsons
rm "${DIR}"/*.erb
