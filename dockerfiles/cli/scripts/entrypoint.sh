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
  docker run <DOCKER_PARAMETERS> ${CHE_IMAGE_FULLNAME} [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}         Where user, instance, and log data saved
  -v /var/run/docker.sock:/var/run/docker.sock
  -it

OPTIONAL DOCKER PARAMETERS:
  -e CHE_HOST=<YOUR_HOST>              IP address or hostname where ${CHE_FORMAL_PRODUCT_NAME} will serve its users
  -e CHE_PORT=<YOUR_PORT>              Port where ${CHE_FORMAL_PRODUCT_NAME} will bind itself to
  -v <LOCAL_PATH>:/data/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:/data/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/cli                 Where the CLI trace log is saved
  -v <LOCAL_PATH>:/repo                ${CHE_FORMAL_PRODUCT_NAME} git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides
    
COMMANDS:
  help                                 This message
  version                              Installed version and upgrade paths
  init                                 Initializes a directory with a ${CHE_FORMAL_PRODUCT_NAME} install
  start                                Starts ${CHE_FORMAL_PRODUCT_NAME} services
  stop                                 Stops ${CHE_FORMAL_PRODUCT_NAME} services
  restart                              Restart ${CHE_FORMAL_PRODUCT_NAME} services
  destroy                              Stops services, and deletes ${CHE_FORMAL_PRODUCT_NAME} instance data
  rmi                                  Removes the Docker images for <version>, forcing a repull
  config                               Generates a ${CHE_FORMAL_PRODUCT_NAME} config from vars; run on any start / restart
  upgrade                              Upgrades ${CHE_FORMAL_PRODUCT_NAME} from one version to another with migrations and backups
  download                             Pulls Docker images for the current ${CHE_FORMAL_PRODUCT_NAME} version
  backup                               Backups ${CHE_FORMAL_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
  restore                              Restores ${CHE_FORMAL_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
  offline                              Saves ${CHE_FORMAL_PRODUCT_NAME} Docker images into TAR files for offline install
  info                                 Displays info about ${CHE_FORMAL_PRODUCT_NAME} and the CLI
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  sync <wksp-name>                     Synchronize workspace with current working directory
  action <action-name>                 Start action on ${CHE_FORMAL_PRODUCT_NAME} instance
  test <test-name>                     Start test on ${CHE_FORMAL_PRODUCT_NAME} instance
"
}

source /scripts/base/startup.sh
start "$@"
