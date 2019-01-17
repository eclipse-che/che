#!/usr/bin/env sh
#
# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# See: https://sipb.mit.edu/doc/safe-shell/

set -e
set -u

#get the latest version of @eclipse-che/api
api_version=$(yarn -s info @eclipse-che/api version)

#publish only if latest version doesn't match current version
if [ "$api_version" != "$1" ];
then
    yarn publish  --registry=https://registry.npmjs.org/ --no-git-tag-version --new-version "$1"
fi
