#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Florent Benoit - Initial Implementation
#

IMAGE_NAME="eclipse/che-ip"
. $(cd "$(dirname "$0")"; pwd)/../build.include

init "$@"

# use -x to display run command
set -x
docker run --rm --net host ${IMAGE_NAME}:${TAG}
