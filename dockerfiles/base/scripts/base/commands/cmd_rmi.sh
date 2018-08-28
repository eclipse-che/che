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

help_cmd_rmi() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} rmi\n"
  text "\n"
  text "Removes bootstrap, utility, and system Docker images used to run ${CHE_MINI_PRODUCT_NAME}\n"
  text "\n"
}

pre_cmd_rmi() {
  :
}

post_cmd_rmi() {
  :
}

cmd_rmi() {
  info "rmi" "Checking registry for version '$CHE_VERSION' images"
  if ! has_version_registry $CHE_VERSION; then
    version_error $CHE_VERSION
    return 1;
  fi

  WARNING="rmi !!! Removing images disables ${CHE_FORMAL_PRODUCT_NAME} and forces a pull !!!"
  if ! confirm_operation "${WARNING}" "$@"; then
    return;
  fi

  IMAGE_LIST=$(cat "$CHE_MANIFEST_DIR"/$CHE_VERSION/images)
  IMAGE_LIST+=$'\n'${BOOTSTRAP_IMAGE_LIST}
  IMAGE_LIST+=$'\n'${UTILITY_IMAGE_LIST}

  IFS=$'\n'
  info "rmi" "Removing ${CHE_MINI_PRODUCT_NAME} Docker images..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    info "rmi" "Removing $VALUE_IMAGE..."
    log "docker rmi -f ${VALUE_IMAGE} >> \"${LOGS}\" 2>&1 || true"
    docker rmi -f $VALUE_IMAGE >> "${LOGS}" 2>&1 || true
  done
}
