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

  # Read in core system images
  IMAGE_LIST=$(cat "$CHE_MANIFEST_DIR"/$CHE_VERSION/images)

  # Read in optional stack images
  readarray -t STACK_IMAGE_LIST < /version/$CHE_VERSION/images-stacks

  # List all images to be saved
  if [[ $# -gt 0 ]] && [[ $1 = "--list" ]]; then
    # First display mandatory 
    info "offline" "Listing images to save for offline usage"
    info ""
    info "offline" "Always:"
    info "offline" "  CLI:   ${CHE_IMAGE_FULLNAME}"

    IFS=$'\n'
    for SINGLE_IMAGE in $IMAGE_LIST; do
      IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
      info "offline" "  CORE:  ${IMAGE_NAME}"
    done
    
    info ""
    info "offline" "Optional: (repeat --image:<name> for stack, --all-stacks, or --no-stacks)"
    for STACK in $(seq 0 $((${#STACK_IMAGE_LIST[@]}-1)))
    do
      info "offline" "  STACK: ${STACK_IMAGE_LIST[$STACK]}"
    done

    return 1
  fi

  # Make sure the images have been pulled and are in your local Docker registry
  cmd_download

  mkdir -p $CHE_CONTAINER_OFFLINE_FOLDER

  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} cli image..."
  save_image ${CHE_IMAGE_FULLNAME}

  info "offline" "Saving utility images..."
  download_and_save_image "${UTILITY_IMAGE_CHEIP}"
  download_and_save_image "${UTILITY_IMAGE_ALPINE}"
  download_and_save_image "${UTILITY_IMAGE_CHEACTION}"
  download_and_save_image "${UTILITY_IMAGE_CHEDIR}"
  download_and_save_image "${UTILITY_IMAGE_CHETEST}"
  download_and_save_image "${UTILITY_IMAGE_CHEMOUNT}"

  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} system images..."
  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    save_image $IMAGE_NAME
  done

  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} stack images..."
  STACK_SAVE="--no-stacks"
  while [ $# -gt 0 ]; do
    case $1 in
      --all-stacks)
        for STACK in $(seq 0 $((${#STACK_IMAGE_LIST[@]}-1)))
        do
          download_and_save_image ${STACK_IMAGE_LIST[$STACK]}
        done
        break
        shift ;;
      --no-stacks)
        info "offline" "  --no-stacks indicated...skipping"
        break
        shift ;;
      --image:*|-i:*)
        download_and_save_image "${1#*:}"
        shift ;;
      *) error "Unknown parameter: $1" ; return 2 ;;
    esac
  done

  info "offline" "Done!"
}

download_and_save_image() {
  update_image_if_not_found ${1}
  save_image ${1}
}

save_image(){
  TAR_NAME=$(echo $1 | sed "s|\/|_|")

  if [ ! -f $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar ]; then
    info "offline" "Saving $CHE_HOST_OFFLINE_FOLDER/$TAR_NAME.tar..."
    if ! $(docker save $1 > $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar); then
      error "Docker was interrupted while saving $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar"
      return 1;
    fi
  else
    info "offline" "  Image $1 already saved...skipping"
  fi
}