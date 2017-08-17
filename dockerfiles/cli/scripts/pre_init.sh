#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

pre_init() {
  ADDITIONAL_MANDATORY_PARAMETERS=""
  ADDITIONAL_OPTIONAL_DOCKER_PARAMETERS="
  -e CHE_HOST=<YOUR_HOST>              IP address or hostname where che will serve its users
  -e CHE_PORT=<YOUR_PORT>              Port where che will bind itself to
  -e CHE_CONTAINER=<YOUR_NAME>         Name for the che container
  -u <name|uid>[:<group|gid>]          (Linux Only) Runs che with specific user and group identity" 
  ADDITIONAL_OPTIONAL_DOCKER_MOUNTS=""
  ADDITIONAL_COMMANDS=""
  ADDITIONAL_GLOBAL_OPTIONS=""
 
  # This must be incremented when BASE is incremented by an API developer
  CHE_CLI_API_VERSION=2

  DEFAULT_CHE_PORT=8080
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}
  CHE_MIN_RAM=1.5
  CHE_MIN_DISK=100
}

