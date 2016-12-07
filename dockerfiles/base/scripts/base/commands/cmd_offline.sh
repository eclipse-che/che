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

cmd_offline() {
  info "offline" "Grabbing image manifest for version '$CHE_VERSION'"
  if ! has_version_registry $CHE_VERSION; then
    version_error $CHE_VERSION
    return 1;
  fi

  # Make sure the images have been pulled and are in your local Docker registry
  cmd_download

  mkdir -p $CHE_CONTAINER_OFFLINE_FOLDER

  IMAGE_LIST=$(cat "$CHE_MANIFEST_DIR"/$CHE_VERSION/images)
  IFS=$'\n'
  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} Docker images as tar files..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    TAR_NAME=$(echo $VALUE_IMAGE | sed "s|\/|_|")
    info "offline" "Saving $CHE_HOST_BACKUP/$TAR_NAME.tar..."
    if ! $(docker save $VALUE_IMAGE > $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar); then
      error "Docker was interrupted while saving $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar"
      return 1;
    fi
  done

  info "offline" "Done!"
}
