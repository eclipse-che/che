#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

cli_pre_init() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host ${UTILITY_IMAGE_CHEIP})}
  DEFAULT_CHE_HOST=$GLOBAL_HOST_IP
  CHE_HOST=${CHE_HOST:-${DEFAULT_CHE_HOST}}
  DEFAULT_CHE_PORT=8080
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}
}