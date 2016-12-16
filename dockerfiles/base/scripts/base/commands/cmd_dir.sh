#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

cmd_dir() {

  if [[ $# -eq 0 ]] ; then
    error "dir command is requiring a local folder to be given as argument"
    return 2;
  fi

  local HOST_FOLDER_TO_USE=${1}

  # Not loaded as part of the init process to save on download time
  update_image_if_not_found eclipse/che-dir:nightly
  docker_run -it -v ${HOST_FOLDER_TO_USE}:${HOST_FOLDER_TO_USE} eclipse/che-dir:nightly "$@"
}
