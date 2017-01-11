#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

cmd_dir() {
  debug $FUNCNAME

  CHE_LOCAL_REPO=false
  if [[ "${CHEDIR_MOUNT}" != "not set" ]]; then
  	local HOST_FOLDER_TO_USE="${CHEDIR_MOUNT}"
  else
  	local HOST_FOLDER_TO_USE="${DATA_MOUNT}"

    warning "':/chedir' not mounted - using ${DATA_MOUNT} as source location"
  fi
 
  # Not loaded as part of the init process to save on download time
  update_image_if_not_found ${UTILITY_IMAGE_CHEDIR}
  docker_run -it -v ${HOST_FOLDER_TO_USE}:${HOST_FOLDER_TO_USE} ${UTILITY_IMAGE_CHEDIR} ${HOST_FOLDER_TO_USE} "$@"
}
