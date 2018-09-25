#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

help_cmd_sync() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} sync WORKSPACE [PARAMETERS]\n"
  text "\n"
  text "Synchronizes a ${CHE_MINI_PRODUCT_NAME} workspace to a local path mounted to ':/sync'\n"
  text "\n"
  text "WORKSPACE:             Accepts workspace name, ID, or namespace:ws-name\n"
  text "                       List all workspaces with 'action list-workspaces'\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --url                Location of ${CHE_MINI_PRODUCT_NAME}\n"
  text "  --user               User name of ${CHE_MINI_PRODUCT_NAME} if accessing authenticated system\n"
  text "  --password           Password of ${CHE_MINI_PRODUCT_NAME} if accessing authenticated system\n"
  text "  --unison-verbose     Verbose output of unison sync\n"
}

pre_cmd_sync() {
  # Not loaded as part of the init process to save on download time
  load_utilities_images_if_not_done
}

post_cmd_sync() {
  :
}

cmd_sync() {
  if [[ "${SYNC_MOUNT}" = "not set" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
    info ""
    info "We could not detect a location to do the sync."
    info "Volume mount a local directory to ':/sync'."
    info ""
    info "  docker run ... -v <YOUR_LOCAL_SYNC_PATH>:/sync ..."
    return 2;
  fi

  # Determine the mount path to do the mount
  info "mount" "Starting sync process to ${SYNC_MOUNT}"

# grab docker run options to enable tty
DOCKER_RUN_OPTIONS=$(get_docker_run_terminal_options)

  docker_run ${DOCKER_RUN_OPTIONS} \
	     --cap-add SYS_ADMIN \
             --device /dev/fuse \
             --security-opt apparmor:unconfined \
             -e CHE_VERSION=${CHE_VERSION} \
             --name che-mount \
             -v "${SYNC_MOUNT}":/mnthost \
                  ${UTILITY_IMAGE_CHEMOUNT} $*

  # Docker doesn't seem to normally clean up this container
  docker rm -f che-mount
}
