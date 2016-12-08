#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

init_usage() {
  USAGE="
USAGE: 
  docker run -it --rm <DOCKER_PARAMETERS> ${CHE_IMAGE_FULLNAME} [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}                Where user, instance, and log data saved

OPTIONAL DOCKER PARAMETERS:
  -e CHE_HOST=<YOUR_HOST>              IP address or hostname where ${CHE_MINI_PRODUCT_NAME} will serve its users
  -e CHE_PORT=<YOUR_PORT>              Port where ${CHE_MINI_PRODUCT_NAME} will bind itself to
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/repo                ${CHE_MINI_PRODUCT_NAME} git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides
    
COMMANDS:
  action <action-name>                 Start action on ${CHE_MINI_PRODUCT_NAME} instance
  backup                               Backups ${CHE_MINI_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
  config                               Generates a ${CHE_MINI_PRODUCT_NAME} config from vars; run on any start / restart
  destroy                              Stops services, and deletes ${CHE_MINI_PRODUCT_NAME} instance data
  download                             Pulls Docker images for the current ${CHE_MINI_PRODUCT_NAME} version
  help                                 This message
  info                                 Displays info about ${CHE_MINI_PRODUCT_NAME} and the CLI
  init                                 Initializes a directory with a ${CHE_MINI_PRODUCT_NAME} install
  offline                              Saves ${CHE_MINI_PRODUCT_NAME} Docker images into TAR files for offline install
  restart                              Restart ${CHE_MINI_PRODUCT_NAME} services
  restore                              Restores ${CHE_MINI_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
  rmi                                  Removes the Docker images for <version>, forcing a repull
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  start                                Starts ${CHE_MINI_PRODUCT_NAME} services
  stop                                 Stops ${CHE_MINI_PRODUCT_NAME} services
  sync <wksp-name>                     Synchronize workspace with current working directory
  test <test-name>                     Start test on ${CHE_MINI_PRODUCT_NAME} instance
  upgrade                              Upgrades ${CHE_MINI_PRODUCT_NAME} from one version to another with migrations and backups
  version                              Installed version and upgrade paths
"
}

source /scripts/base/startup.sh
start "$@"
