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

help_cmd_destroy() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} destroy [PARAMETERS]\n"
  text "\n"
  text "Deletes a ${CHE_MINI_PRODUCT_NAME} installation\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --quiet                           Do not ask user for confirmation\n"
  text "  --cli                             Removes the 'cli.log'\n"
  text "\n"
}


pre_cmd_destroy() {
  :
}

post_cmd_destroy() {
  :
}

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

  WARNING="${YELLOW}!!!${RED} Stopping services and ${YELLOW}!!!${RED} deleting data ${YELLOW}!!!${RED} this is unrecoverable ${YELLOW}!!!${NC}"
  if ! confirm_operation "${WARNING}" "${QUIET}"; then
    return;
  fi

  cmd_lifecycle stop --skip:graceful

  info "destroy" "Deleting instance and config..."

  log "docker_run -v \"${CHE_HOST_CONFIG}\":${CHE_CONTAINER_ROOT} \
                    ${BOOTSTRAP_IMAGE_ALPINE} sh -c \"rm -rf /root${CHE_CONTAINER_ROOT}/docs \
                                   && rm -rf /root${CHE_CONTAINER_ROOT}/instance \
                                   && rm -rf /root${CHE_CONTAINER_ROOT}/${CHE_MINI_PRODUCT_NAME}.env\""
  # Super weird bug.  For some reason on windows, this command has to be run 3x for everything
  # to be destroyed properly if you are in dev mode.
  until directory_is_empty; do
    docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
                  ${BOOTSTRAP_IMAGE_ALPINE} sh -c "rm -rf /root${CHE_CONTAINER_ROOT}/docs \
                                 ; rm -rf /root${CHE_CONTAINER_ROOT}/instance \
                                 ; rm -rf /root${CHE_CONTAINER_ROOT}/${CHE_MINI_PRODUCT_NAME}.env" > /dev/null 2>&1  || true
  done
  rm -rf "${CHE_CONTAINER_INSTANCE}"

  cmd_destroy_post_action

  # Sometimes users want the CLI log after they have destroyed their instance
  # If they pass destroy --cli then we will also destroy the CLI log
  if [[ "${DESTROY_CLI}" = "true" ]]; then
    info "destroy" "Deleting cli.log..."
     docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
               ${BOOTSTRAP_IMAGE_ALPINE} sh -c "rm -rf /root${CHE_CONTAINER_ROOT}/cli.log" > /dev/null 2>&1 || true
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
