#!/bin/sh
#
# Copyright (c) 2018-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

set -e
set -u

# define in env variable GITHUB_TOKEN only if it is defined
# else check if github rate limit is enough, else will abort requiring to set GITHUB_TOKEN value

if [ ! -z "${GITHUB_TOKEN-}" ]; then
    export GITHUB_TOKEN=$GITHUB_TOKEN;
    echo "Setting GITHUB_TOKEN value as provided";
else
    export GITHUB_LIMIT=$(curl -s 'https://api.github.com/rate_limit' | jq '.rate .remaining');
    echo "Current API rate limit https://api.github.com is ${GITHUB_LIMIT}";
    if [ "${GITHUB_LIMIT}" -lt 10 ]; then
        printf "\033[0;31m\n\n\nRate limit on https://api.github.com is reached so in order to build this image, ";
        printf "the build argument GITHUB_TOKEN needs to be provided so build will not fail.\n\n\n\033[0m";
        exit 1;
    else
        echo "GITHUB_TOKEN variable not set but https://api.github.com rate limit has enough slots";
    fi
fi

