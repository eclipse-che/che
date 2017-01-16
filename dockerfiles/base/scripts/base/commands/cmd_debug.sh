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

cmd_debug() {
  debug $FUNCNAME
  info "---------------------------------------"
  info "------------   CLI INFO   -------------"
  info "---------------------------------------"
  info ""
  info "-----------  ${CHE_PRODUCT_NAME} INFO  ------------"
  info "${CHE_PRODUCT_NAME}_VERSION           = ${CHE_VERSION}"
  info "${CHE_PRODUCT_NAME}_INSTANCE          = ${CHE_HOST_INSTANCE}"
  info "${CHE_PRODUCT_NAME}_CONFIG            = ${CHE_HOST_CONFIG}"
  local CHE_HOST_LOCAL=${CHE_HOST}
  if is_initialized; then
    CHE_HOST_LOCAL=$(get_value_of_var_from_env_file CHE_HOST)
  fi
  info "${CHE_PRODUCT_NAME}_HOST              = ${CHE_HOST_LOCAL}"
  info "${CHE_PRODUCT_NAME}_REGISTRY          = ${CHE_MANIFEST_DIR}"
  info "${CHE_PRODUCT_NAME}_DEBUG             = ${CHE_DEBUG}"
  info "${CHE_PRODUCT_NAME}_BACKUP            = ${CHE_HOST_BACKUP}"
  if local_repo; then
    info "${CHE_PRODUCT_NAME}_DEVELOPMENT_REPO  = ${CHE_HOST_DEVELOPMENT_REPO}"
  fi
  info ""
  info "-----------  PLATFORM INFO  -----------"
  info "DOCKER_INSTALL_TYPE       = $(get_docker_install_type)"
  info "IS_NATIVE                 = $(is_native && echo "YES" || echo "NO")"
  info "IS_WINDOWS                = $(has_docker_for_windows_client && echo "YES" || echo "NO")"
  info "IS_DOCKER_FOR_WINDOWS     = $(is_docker_for_windows && echo "YES" || echo "NO")"
  info "HAS_DOCKER_FOR_WINDOWS_IP = $(has_docker_for_windows_client && echo "YES" || echo "NO")"
  info "IS_DOCKER_FOR_MAC         = $(is_docker_for_mac && echo "YES" || echo "NO")"
  info "IS_BOOT2DOCKER            = $(is_boot2docker && echo "YES" || echo "NO")"
  info ""
}
