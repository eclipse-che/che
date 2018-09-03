#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

help_cmd_dir() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} dir COMMAND [PARAMETERS]\n"
  text "\n"
  text "Create a workspace using a local directory mounted to ':\chedir'\n"
  text "\n"
  text "COMMANDS:\n"
  text "  init                              Initializes an empty local directory with a new Chefile\n"
  text "  down                              Stops the workspace and ${CHE_MINI_PRODUCT_NAME} representing this directory\n"
  text "  ssh                               SSH into the workspace that represents the local directory\n"
  text "  status                            Reports on the runtime status of ${CHE_MINI_PRODUCT_NAME} and the workspace runtime\n"
  text "  up                                Starts ${CHE_MINI_PRODUCT_NAME} and creates a new workspace with a project from your local dir\n"
  text "\n"
}

pre_cmd_dir() {
  # Not loaded as part of the init process to save on download time
  load_utilities_images_if_not_done
}

post_cmd_dir() {
  :
}

cmd_dir() {
  CHE_LOCAL_REPO=false
  if [[ "${CHEDIR_MOUNT}" != "not set" ]]; then
  	local HOST_FOLDER_TO_USE="${CHEDIR_MOUNT}"
  else
  	local HOST_FOLDER_TO_USE="${DATA_MOUNT}"

    warning "':/chedir' not mounted - using ${DATA_MOUNT} as source location"
  fi

  docker_run $(get_docker_run_terminal_options) -v ${HOST_FOLDER_TO_USE}:${HOST_FOLDER_TO_USE} \
            ${UTILITY_IMAGE_CHEDIR} ${HOST_FOLDER_TO_USE} "$@"
}
