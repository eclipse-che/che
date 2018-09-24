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

help_cmd_upgrade() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} upgrade [PARAMETERS]\n"
  text "\n"
  text "Upgrades ${CHE_MINI_PRODUCT_NAME} from one version to another while protecting user workspace data"
  text "\n"
  text "PARAMETERS:\n"
  text "  --skip-backup        Skip backup of user data before performing upgrade\n"
}

pre_cmd_upgrade() {
  :
}

post_cmd_upgrade() {
  :
}

cmd_upgrade() {
  CHE_IMAGE_VERSION=$(get_image_version)
  DO_BACKUP="true"
  ARGS=""

  for var in $@; do
    if [[ "$var" == *"--skip-backup"* ]]; then
              DO_BACKUP="false"
              continue
    fi
    ARGS+="$var "
  done

  # If we got here, this means:
  #   image version > configured & installed version
  #   configured version = installed version
  # 
  # We can now upgrade using the information contained in the CLI image

  ## Download version images
  info "upgrade" "Downloading $CHE_MINI_PRODUCT_NAME images for version $CHE_IMAGE_VERSION..."
  get_image_manifest $CHE_IMAGE_VERSION
  SAVEIFS=$IFS
  IFS=$'\n'
  for SINGLE_IMAGE in ${IMAGE_LIST}; do
    VALUE_IMAGE=$(echo ${SINGLE_IMAGE} | cut -d'=' -f2)
    update_image_if_not_found ${VALUE_IMAGE}
  done
  IFS=$SAVEIFS
  info "upgrade" "Downloading done."

  if get_server_container_id "${CHE_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "$CHE_MINI_PRODUCT_NAME is running. Stop before performing an upgrade."
    return 2;
  fi

  if [[ "${DO_BACKUP}" == "true" ]]; then
    info "upgrade" "Preparing backup..."
    cmd_lifecycle backup
  else
    info "upgrade" "Skipping backup"
  fi

  info "upgrade" "Reinitializing the system with your configuration..."
  cmd_lifecycle init --accept-license --reinit

  cmd_lifecycle start ${ARGS}
}
