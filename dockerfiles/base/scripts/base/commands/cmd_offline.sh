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
  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} images..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    save_image $SINGLE_IMAGE
  done

  info ""
  info "offline" "Saving workspace images (hint: --image:<name> to save single stack or --list to view all)..."
  STACK_SAVE="--all"
  while [ $# -gt 0 ]; do
    case $1 in
      --all)
        break
        shift ;;
      --list)
        STACK_SAVE="--list"
        shift ;;
      --image:*|-i:*)
        STACK_SAVE="${1#*:}"
        shift ;;
      *) error "Unknown parameter: $1" ; return 2 ;;
    esac
  done

  STACK_IMAGE_LIST[1]="codenvy/alpine_jdk8"
  STACK_IMAGE_LIST[2]="codenvy/aspnet"
  STACK_IMAGE_LIST[3]="codenvy/centos_jdk8"
  STACK_IMAGE_LIST[4]="codenvy/cpp_gcc"
  STACK_IMAGE_LIST[5]="codenvy/debian_jdk8"
  STACK_IMAGE_LIST[6]="codenvy/debian_jdk8_node"
  STACK_IMAGE_LIST[7]="codenvy/debian_jre"
  STACK_IMAGE_LIST[8]="codenvy/dotnet_core"
  STACK_IMAGE_LIST[9]="codenvy/hadoop-dev"
  STACK_IMAGE_LIST[10]="codenvy/meteor"
  STACK_IMAGE_LIST[11]="codenvy/node"
  STACK_IMAGE_LIST[12]="codenvy/php"
  STACK_IMAGE_LIST[13]="codenvy/platformio"
  STACK_IMAGE_LIST[14]="codenvy/ruby_rails"
  STACK_IMAGE_LIST[15]="codenvy/selenium"
  STACK_IMAGE_LIST[16]="codenvy/ubuntu_android"
  STACK_IMAGE_LIST[17]="codenvy/ubuntu_go"
  STACK_IMAGE_LIST[18]="codenvy/ubuntu_gradle"
  STACK_IMAGE_LIST[19]="codenvy/ubuntu_jdk8"
  STACK_IMAGE_LIST[20]="codenvy/ubuntu_jre"
  STACK_IMAGE_LIST[21]="codenvy/ubuntu_python"
  STACK_IMAGE_LIST[22]="codenvy/ubuntu_wildfly8"
  STACK_IMAGE_LIST[23]="codenvy/x11_vnc"

  if [[ "${STACK_SAVE}" = "--all" ]]; then

    for STACK in $(seq 1 ${#STACK_IMAGE_LIST[@]})
    do
      update_image_if_not_found ${STACK_IMAGE_LIST[$STACK]}
      save_image ${STACK_IMAGE_LIST[$STACK]}
    done

  elif [[ "${STACK_SAVE}" = "--list" ]]; then
    for STACK in $(seq 1 ${#STACK_IMAGE_LIST[@]})
    do
      info "offline" "Stack: ${STACK_IMAGE_LIST[$STACK]}"
    done
  else
    update_image_if_not_found ${STACK_SAVE}
    save_image ${STACK_SAVE}
  fi

  info "offline" "Done!"
}

save_image(){
  VALUE_IMAGE=$(echo $1 | cut -d'=' -f2)
  TAR_NAME=$(echo $VALUE_IMAGE | sed "s|\/|_|")

  if [ ! -f $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar ]; then
    info "offline" "Saving $CHE_HOST_OFFLINE_FOLDER/$TAR_NAME.tar..."
    if ! $(docker save $VALUE_IMAGE > $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar); then
      error "Docker was interrupted while saving $CHE_CONTAINER_OFFLINE_FOLDER/$TAR_NAME.tar"
      return 1;
    fi
  fi
}