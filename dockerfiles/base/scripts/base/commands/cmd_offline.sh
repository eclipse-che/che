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

help_cmd_offline() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} offline [PARAMETERS]\n"
  text "\n"
  text "Downloads and saves Docker images required to run ${CHE_MINI_PRODUCT_NAME} offline. Add the 
'--offline' global parameter command to execute ${CHE_MINI_PRODUCT_NAME} in offline mode. You can optionally 
download stack images used to start workspaces. Stack images are heavy and often larger than 1GB. You 
can save them all or selectively choose stacks.\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --all-stacks                      Saves all stack images\n"
  text "  --list                            Lists all images that will be downloaded and saved\n"
  text "  --image:<name>                    Downloads specific stack image\n"
  text "  --no-stacks                       Do not save any stack images\n"
  text "\n"
}


pre_cmd_offline() {
  :
}

post_cmd_offline() {
  :
}

cmd_offline() {
  # Read in optional stack images
  readarray -t STACK_IMAGE_LIST < /version/$CHE_VERSION/images-stacks
  BOOTSTRAP_IMAGE_LIST=$(cat /version/$CHE_VERSION/images-bootstrap)
  UTILITY_IMAGE_LIST=$(cat /version/$CHE_VERSION/images-utilities)

  # List all images to be saved
  if [[ $# -gt 0 ]] && [[ $1 = "--list" ]]; then
    # First display mandatory 
    info "offline" "Listing images to save for offline usage"
    info ""
    info "offline" "Always:"
    info "offline" "  CLI:        ${CHE_IMAGE_FULLNAME}"

    IFS=$'\n'
    for SINGLE_IMAGE in $BOOTSTRAP_IMAGE_LIST; do
      IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
      info "offline" "  BOOTSTRAP:  ${IMAGE_NAME}"
    done

    IFS=$'\n'
    for SINGLE_IMAGE in $IMAGE_LIST; do
      IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
      info "offline" "  SYSTEM:     ${IMAGE_NAME}"
    done
    
    IFS=$'\n'
    for SINGLE_IMAGE in $UTILITY_IMAGE_LIST; do
      IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
      info "offline" "  UTILITY:    ${IMAGE_NAME}"
    done

    info ""
    info "offline" "Optional: (repeat --image:<name> for stack, --all-stacks, or --no-stacks)"
    for STACK in $(seq 0 $((${#STACK_IMAGE_LIST[@]}-1)))
    do
      if [ ! -z ${STACK_IMAGE_LIST[$STACK]} ]; then
          info "offline" "  STACK: ${STACK_IMAGE_LIST[$STACK]}"
      fi
    done

    return 1
  fi

  # Make sure the images have been pulled and are in your local Docker registry
  cmd_lifecycle download

  mkdir -p $CHE_CONTAINER_OFFLINE_FOLDER

  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} cli image..."
  save_image ${CHE_IMAGE_FULLNAME}

  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} bootstrap images..."
  IFS=$'\n'
  for SINGLE_IMAGE in ${BOOTSTRAP_IMAGE_LIST}; do
    IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    save_image $IMAGE_NAME
  done

  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} system images..."
  for SINGLE_IMAGE in $IMAGE_LIST; do
    IMAGE_NAME=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    save_image $IMAGE_NAME
  done

  info "offline" "Saving utility images..."
  for SINGLE_IMAGE in ${UTILITY_IMAGE_LIST}; do
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
          if [ ! -z ${STACK_IMAGE_LIST[$STACK]} ]; then
              download_and_save_image ${STACK_IMAGE_LIST[$STACK]}
          fi
        done
        break
        shift ;;
      --no-stacks)
        info "offline" "--no-stacks indicated...skipping"
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
  TAR_NAME=$(echo $1 | sed "s|\/|_|g")

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