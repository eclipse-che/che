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

  # Determine the mount path to do the mount
  info "mount" "Starting sync process to ${SYNC_MOUNT}"

  # TODO: How to take connection parameters for Codenvy?
  
  # Only volume mount the unison profile if it is set
  if [[ "${UNISON_PROFILE_MOUNT}" = "not set" ]]; then
    docker_run --cap-add SYS_ADMIN \
               --device /dev/fuse \
               --name che-mount \
               -v ${HOME}/.ssh:${HOME}/.ssh \
               -v /etc/group:/etc/group:ro \
               -v /etc/passwd:/etc/passwd:ro \
               -u $(id -u ${USER}) \
               -v "${UNISON_PROFILE_MOUNT}":/profile \
               -v "${SYNC_MOUNT}":/mnthost \
                  eclipse/che-mount:nightly $*

  else  

    docker_run --cap-add SYS_ADMIN \
               --device /dev/fuse \
               --name che-mount \
               -v ${HOME}/.ssh:${HOME}/.ssh \
               -v /etc/group:/etc/group:ro \
               -v /etc/passwd:/etc/passwd:ro \
               -u $(id -u ${USER}) \
               -v "${SYNC_MOUNT}":/mnthost \
                  eclipse/che-mount:nightly $*

  fi

  # Docker doesn't seem to normally clean up this container
  docker rm -f che-mount

}
