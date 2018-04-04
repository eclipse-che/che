#!/bin/sh
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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
