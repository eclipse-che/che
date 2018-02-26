#!/bin/sh
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

DIR=$(cd "$(dirname "$0")"; pwd)
# copy che theme
cp -r "${DIR}"/../init/modules/keycloak/files/che "${DIR}"
# copy user and realm json templates
cp -r "${DIR}"/../init/modules/keycloak/templates/* "${DIR}"

init --name:keycloak "$@"
build

# remove files
rm -rf "${DIR}"/che
rm -rf "${DIR}"/*.erb
