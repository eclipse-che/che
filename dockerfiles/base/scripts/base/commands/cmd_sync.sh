#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

cmd_sync() {
  debug $FUNCNAME

  if [[ "${SYNC_MOUNT}" = "not set" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
    info ""
    info "We could not detect a location to do the sync."
    info "Volume mount a local directory to ':/sync'."
    info ""
    info "  docker run .... -v <YOUR_LOCAL_SYNC_PATH>:/sync ...."
    return 2;
  fi

  update_image_if_not_found ${UTILITY_IMAGE_CHEMOUNT}

  # Determine the mount path to do the mount
  info "mount" "Starting sync process to ${SYNC_MOUNT}"

  docker_run --cap-add SYS_ADMIN \
             --device /dev/fuse \
             -e CHE_VERSION=${CHE_VERSION} \
             --name che-mount \
             -v "${SYNC_MOUNT}":/mnthost \
                  ${UTILITY_IMAGE_CHEMOUNT} $*

  # Docker doesn't seem to normally clean up this container
  docker rm -f che-mount

}
