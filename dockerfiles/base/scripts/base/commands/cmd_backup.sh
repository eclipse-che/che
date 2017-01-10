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

cmd_backup() {
  debug $FUNCNAME

  # possibility to skip ${CHE_FORMAL_PRODUCT_NAME} projects backup
  SKIP_BACKUP_CHE_DATA=${1:-"--no-skip-data"}
  if [[ "${SKIP_BACKUP_CHE_DATA}" == "--skip-data" ]]; then
    TAR_EXTRA_EXCLUDE="--exclude=instance/data${CHE_CONTAINER_ROOT}"
  else
    TAR_EXTRA_EXCLUDE=""
  fi

  if [[ ! -d "${CHE_CONTAINER_CONFIG}" ]]; then
    error "Cannot find existing CHE_CONFIG or CHE_INSTANCE."
    return;
  fi

  if get_server_container_id "${CHE_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "$CHE_MINI_PRODUCT_NAME is running. Stop before performing a backup."
    return 2;
  fi

  if [[ ! -d "${CHE_CONTAINER_BACKUP}" ]]; then
    mkdir -p "${CHE_CONTAINER_BACKUP}"
  fi

  # check if backups already exist and if so we move it with time stamp in name
  if [[ -f "${CHE_CONTAINER_BACKUP}/${CHE_BACKUP_FILE_NAME}" ]]; then
    mv "${CHE_CONTAINER_BACKUP}/${CHE_BACKUP_FILE_NAME}" \
        "${CHE_CONTAINER_BACKUP}/moved-$(get_current_date)-${CHE_BACKUP_FILE_NAME}"
  fi

  info "backup" "Saving codenvy data..."
  docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
               -v "${CHE_HOST_BACKUP}":/root/backup \
               $(cmd_backup_extra_args) \
                 ${UTILITY_IMAGE_ALPINE} sh -c "tar czf /root/backup/${CHE_BACKUP_FILE_NAME} -C /root${CHE_CONTAINER_ROOT} . --exclude='backup' --exclude='instance/dev' --exclude='instance/logs' ${TAR_EXTRA_EXCLUDE}"
  info ""
  info "backup" "Codenvy data saved in ${CHE_HOST_BACKUP}/${CHE_BACKUP_FILE_NAME}"
}

cmd_backup_extra_args() {
  echo ""
}

# return date in format which can be used as a unique file or dir name
# example 2016-10-31-1477931458
get_current_date() {
    date +'%Y-%m-%d-%s'
}
