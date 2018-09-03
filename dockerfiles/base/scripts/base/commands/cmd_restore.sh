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

help_cmd_restore() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} restore\n"
  text "\n"
  text "Restores user data and recovers a ${CHE_MINI_PRODUCT_NAME} configuration\n"
  text "\n"
}

pre_cmd_restore() {
  :
}

post_cmd_restore() {
  :
}

cmd_restore_pre_action() {
  true
}

cmd_restore_extra_args() {
  echo ""
}

cmd_restore() {
  debug $FUNCNAME

  if [[ -d "${CHE_CONTAINER_CONFIG}" ]]; then
    WARNING="Restoration overwrites existing configuration and data. Are you sure?"
    if ! confirm_operation "${WARNING}" "$@"; then
      return;
    fi
  fi

  if get_server_container_id "${CHE_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "${CHE_FORMAL_PRODUCT_NAME} is running. Stop before performing a restore."
    return;
  fi

  if [[ ! -f "${CHE_CONTAINER_BACKUP}/${CHE_BACKUP_FILE_NAME}" ]]; then
    error "Backup files not found. To do restore please do backup first."
    return;
  fi

  # remove config and instance folders
  log "docker_run -v \"${CHE_HOST_CONFIG}\":${CHE_CONTAINER_ROOT} \
                    ${BOOTSTRAP_IMAGE_ALPINE} sh -c \"rm -rf /root${CHE_CONTAINER_ROOT}/docs \
                                   ; rm -rf /root${CHE_CONTAINER_ROOT}/instance \
                                   ; rm -rf /root${CHE_CONTAINER_ROOT}/${CHE_MINI_PRODUCT_NAME}.env\""
  docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
                ${BOOTSTRAP_IMAGE_ALPINE} sh -c "rm -rf /root${CHE_CONTAINER_ROOT}/docs \
                              ; rm -rf /root${CHE_CONTAINER_ROOT}/instance \
                              ; rm -rf /root${CHE_CONTAINER_ROOT}/${CHE_MINI_PRODUCT_NAME}.env"

  info "restore" "Recovering ${CHE_FORMAL_PRODUCT_NAME} data..."

  cmd_restore_pre_action

  docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
             -v "${CHE_HOST_BACKUP}/${CHE_BACKUP_FILE_NAME}":"/root/backup/${CHE_BACKUP_FILE_NAME}" \
             $(cmd_restore_extra_args) \
             ${BOOTSTRAP_IMAGE_ALPINE} sh -c "tar xf /root/backup/${CHE_BACKUP_FILE_NAME} -C /root${CHE_CONTAINER_ROOT}"
}
