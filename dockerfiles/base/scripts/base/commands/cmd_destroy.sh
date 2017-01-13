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


cmd_destroy_post_action() {
 true
}

cmd_destroy() {
  debug $FUNCNAME

  QUIET=""
  DESTROY_CLI="false"

  while [ $# -gt 0 ]; do
    case $1 in
      --quiet)
        QUIET="--quiet"
        shift ;;
      --cli)
        DESTROY_CLI="true"
        shift ;;
      *) error "Unknown parameter: $1; did you mean --quiet or --cli?" ; return 2 ;;
    esac
  done

  WARNING="${RED}!!!${NC} Stopping services and ${RED}!!!${NC} deleting data ${RED}!!!${NC} this is unrecoverable ${RED}!!!${NC}"
  if ! confirm_operation "${WARNING}" "${QUIET}"; then
    return;
  fi

  cmd_stop

  info "destroy" "Deleting instance and config..."

  log "docker_run -v \"${CHE_HOST_CONFIG}\":${CHE_CONTAINER_ROOT} \
                    ${UTILITY_IMAGE_ALPINE} sh -c \"rm -rf /root${CHE_CONTAINER_ROOT}/docs \
                                   && rm -rf /root${CHE_CONTAINER_ROOT}/instance \
                                   && rm -rf /root${CHE_CONTAINER_ROOT}/${CHE_MINI_PRODUCT_NAME}.env\""
  # Super weird bug.  For some reason on windows, this command has to be run 3x for everything
  # to be destroyed properly if you are in dev mode.
  until directory_is_empty; do
    docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
                  ${UTILITY_IMAGE_ALPINE} sh -c "rm -rf /root${CHE_CONTAINER_ROOT}/docs \
                                 ; rm -rf /root${CHE_CONTAINER_ROOT}/instance \
                                 ; rm -rf /root${CHE_CONTAINER_ROOT}/${CHE_MINI_PRODUCT_NAME}.env" > /dev/null 2>&1  || true
  done
  rm -rf "${CHE_CONTAINER_INSTANCE}"

  cmd_destroy_post_action

  # Sometimes users want the CLI after they have destroyed their instance
  # If they pass destroy --cli then we will also destroy the CLI
  if [[ "${DESTROY_CLI}" = "true" ]]; then
    info "destroy" "Deleting cli.log..."
    docker_run -v "${CLI_DIR}":/root/cli ${UTILITY_IMAGE_ALPINE} sh -c "rm -rf /root/cli/cli.log"
  fi
}

directory_is_empty() {
  if [[ -d "${CHE_CONTAINER_CONFIG}/docs" ]] ||
     [[ -d "${CHE_CONTAINER_CONFIG}/instance" ]] ||
     [[ -f "${CHE_CONTAINER_CONFIG}/${CHE_MINI_PRODUCT_NAME}.env" ]]; then
    return 1
  else
    return 0
  fi
}
