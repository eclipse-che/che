#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

init_usage() {
  USAGE="
Usage: docker run -it --rm
                  -v /var/run/docker.sock:/var/run/docker.sock
                  -v <LOCAL_DATA_PATH>:${CHE_CONTAINER_ROOT}
                  ${CHE_IMAGE_FULLNAME} [COMMAND]

    help                                 This message
    version                              Installed version and upgrade paths
    init                                 Initializes a directory with a ${CHE_FORMAL_PRODUCT_NAME} install
         [--no-force                         Default - uses cached local Docker images
          --pull                             Checks for newer images from DockerHub
          --force                            Removes all images and re-pulls all images from DockerHub
          --offline                          Uses images saved to disk from the offline command
          --accept-license                   Auto accepts the ${CHE_FORMAL_PRODUCT_NAME} license during installation
          --reinit]                          Reinstalls using existing $CHE_MINI_PRODUCT_NAME.env configuration
    start [--pull | --force | --offline] Starts ${CHE_FORMAL_PRODUCT_NAME} services
    stop                                 Stops ${CHE_FORMAL_PRODUCT_NAME} services
    restart [--pull | --force]           Restart ${CHE_FORMAL_PRODUCT_NAME} services
    destroy                              Stops services, and deletes ${CHE_FORMAL_PRODUCT_NAME} instance data
            [--quiet                         Does not ask for confirmation before destroying instance data
             --cli]                          If :/cli is mounted, will destroy the cli.log
    rmi [--quiet]                        Removes the Docker images for <version>, forcing a repull
    config                               Generates a ${CHE_FORMAL_PRODUCT_NAME} config from vars; run on any start / restart
    upgrade                              Upgrades ${CHE_FORMAL_PRODUCT_NAME} from one version to another with migrations and backups
    download [--pull|--force|--offline]  Pulls Docker images for the current ${CHE_FORMAL_PRODUCT_NAME} version
    backup [--quiet | --skip-data]       Backups ${CHE_FORMAL_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
    restore [--quiet]                    Restores ${CHE_FORMAL_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
    offline                              Saves ${CHE_FORMAL_PRODUCT_NAME} Docker images into TAR files for offline install
    info                                 Displays info about ${CHE_FORMAL_PRODUCT_NAME} and the CLI
         [ --all                             Run all debugging tests
           --debug                           Displays system information
           --network]                        Test connectivity between ${CHE_FORMAL_PRODUCT_NAME} sub-systems
    ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
    mount <wksp-name>                    Synchronize workspace with current working directory
    action <action-name> [--help]        Start action on ${CHE_FORMAL_PRODUCT_NAME} instance
    test <test-name> [--help]            Start test on ${CHE_FORMAL_PRODUCT_NAME} instance

Variables:
    CHE_HOST                             IP address or hostname where ${CHE_FORMAL_PRODUCT_NAME} will serve its users
    CLI_DEBUG                            Default=false. Prints stack trace during execution
    CLI_INFO                             Default=true. Prints out INFO messages to standard out
    CLI_WARN                             Default=true. Prints WARN messages to standard out
    CLI_LOG                              Default=true. Prints messages to cli.log file
"
}

source /scripts/base/startup.sh
start "$@"
