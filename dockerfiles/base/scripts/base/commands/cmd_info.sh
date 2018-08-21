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

help_cmd_info() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} info [PARAMETERS]\n"
  text "\n"
  text "Status, information, and support diagnostic bundles for ${CHE_MINI_PRODUCT_NAME}\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --all                             Prints info, runs networking tests, ad prepares diagnostic bundle\n"
  text "  --bundle                          Prepares diagnostic bundle for ${CHE_MINI_PRODUCT_NAME} and Docker\n"
  text "  --network                         Runs simulated network diagnostic to confirm network routing\n"
  text "  --print                           Default - displays status and configuration of ${CHE_MINI_PRODUCT_NAME}\n"
  text "\n"
}


pre_cmd_info() {
  :
}

post_cmd_info() {
  :
}

cmd_info() {
  debug $FUNCNAME
  if [ $# -eq 0 ]; then
    TESTS="--print"
  else
    TESTS=$1
  fi

  case $TESTS in
    --all|-all)
      print_info
      cmd_network
      prepare_bundle
    ;;
    --network|-network)
      cmd_network
    ;;
    --print|-print)
      print_info
    ;;
    --bundle|-bundle)
      prepare_bundle
    ;;
    *)
      info "info" "Unknown info flag passed: $1."
      return;
    ;;
  esac
}

prepare_bundle() {
  info "info" "Preparing diagnostic bundle..."
  docker run --net host ${BOOTSTRAP_IMAGE_ALPINE} ip a show >> "${CLI_DIR}/ip.output"
  docker run --net host ${BOOTSTRAP_IMAGE_ALPINE} route >> "${CLI_DIR}/route.output"
  curl -s https://hub.docker.com/v2/repositories/${CHE_IMAGE_NAME}/tags/ >> "${CLI_DIR}/curlversion.output"
  curl -I -k https://hub.docker.com >> "${CLI_DIR}/curldockerhub.output"
  df "${CHE_CONTAINER_ROOT}" >> "${CLI_DIR}/df.output"
  cmd_network >> "${CLI_DIR}/cli-network.output"
  print_info >> "${CLI_DIR}/cli-info.output"

  tar -cf "${CLI_DIR}"/${CHE_MINI_PRODUCT_NAME}-diagnostic-bundle.tar \
    "${CLI_DIR}/ip.output" \
    "${CLI_DIR}/route.output" \
    "${CLI_DIR}/curlversion.output" \
    "${CLI_DIR}/curldockerhub.output" \
    "${CLI_DIR}/df.output" \
    "${CLI_DIR}/cli.log" \
    "${CLI_DIR}/cli-network.output" \
    "${CLI_DIR}/cli-info.output" \
    "${CHE_CONTAINER_INSTANCE}/logs" > /dev/null 2>&1 || true


  info "info" "Diagnostic bundle ${CHE_MINI_PRODUCT_NAME}-diagnostic-bundle.tar prepared"

  # Clean
  docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
                  ${BOOTSTRAP_IMAGE_ALPINE} \
                      sh -c "rm -rf /root${CHE_CONTAINER_ROOT}/ip.output \
                           ; rm -rf /root${CHE_CONTAINER_ROOT}/route.output \
                           ; rm -rf /root${CHE_CONTAINER_ROOT}/curlversion.output \
                           ; rm -rf /root${CHE_CONTAINER_ROOT}/curldockerhub.output \
                           ; rm -rf /root${CHE_CONTAINER_ROOT}/df.output \
                           ; rm -rf /root${CHE_CONTAINER_ROOT}/cli-info.output \
                           ; rm -rf /root${CHE_CONTAINER_ROOT}/cli-network.output" > /dev/null 2>&1 || true
}

print_info() {
  DOCKER_CONNECT="${DOCKER_MOUNT}"
  if [[ "${DOCKER_MOUNT}" = "not set" ]]; then
    DOCKER_CONNECT=$(docker inspect $(get_this_container_id) | grep DOCKER_HOST)
    DOCKER_CONNECT=${DOCKER_CONNECT//\"}
    DOCKER_CONNECT=${DOCKER_CONNECT//\,}
    DOCKER_CONNECT=${DOCKER_CONNECT#*=}
  fi
  text "\n"
  text "CLI:\n"
  text " TTY:            ${TTY_ACTIVATED}\n"
  text " Daemon:         ${DOCKER_CONNECT}\n"
  text " Image:          ${CHE_IMAGE_FULLNAME}\n"
  text " Version:        ${CHE_IMAGE_VERSION}\n"
  text " Command:        $(echo $COMMAND | cut -f2 -d'_')\n"
  text " Parameters:     ${ORIGINAL_PARAMETERS}\n"
  text "Mounts:\n"
  text " $CHE_CONTAINER_ROOT:          $DATA_MOUNT\n"
  text " $CHE_CONTAINER_ROOT/instance: $INSTANCE_MOUNT\n"
  text " $CHE_CONTAINER_ROOT/backup:   $BACKUP_MOUNT\n"
  text " /repo:          $REPO_MOUNT\n"
  text " /assembly:      $ASSEMBLY_MOUNT\n"
  text " /sync:          $SYNC_MOUNT\n"
  text " /unison:        $UNISON_PROFILE_MOUNT\n"
  text " /chedir:        $CHEDIR_MOUNT\n"
  text "System:\n"
  text " Docker:         $(get_docker_install_type)\n"
  if [[ ${HTTP_PROXY} = "" ]] &&
     [[ ${HTTPS_PROXY} = "" ]] &&
     [[ ${NO_PROXY} = "" ]]; then
    text " Proxy:          not set\n"
  else
    text " Proxy:          HTTP_PROXY=${HTTP_PROXY}, HTTPS_PROXY=${HTTPS_PROXY}, NO_PROXY=${NO_PROXY}\n"
  fi
  text "Internal:\n"
  text " ${CHE_PRODUCT_NAME}_VERSION:    ${CHE_VERSION}\n"
  text " ${CHE_PRODUCT_NAME}_HOST:       ${CHE_HOST}\n"
  text " ${CHE_PRODUCT_NAME}_INSTANCE:   ${CHE_HOST_INSTANCE}\n"
  text " ${CHE_PRODUCT_NAME}_CONFIG:     ${CHE_HOST_CONFIG}\n"
  text " ${CHE_PRODUCT_NAME}_BACKUP:     ${CHE_HOST_BACKUP}\n"
  text " ${CHE_PRODUCT_NAME}_REGISTRY:   ${CHE_MANIFEST_DIR}\n"
  text " ${CHE_PRODUCT_NAME}_DEBUG:      ${CHE_DEBUG}\n"
  text " IP Detection:   $(docker run --net host ${BOOTSTRAP_IMAGE_CHEIP})\n"
  if is_initialized; then
    text " Initialized:    true\n"
  else
    text " Initialized:    false\n"
  fi    
  if local_repo; then
    text " ${CHE_PRODUCT_NAME}_DEVELOPMENT_REPO: ${CHE_HOST_DEVELOPMENT_REPO}\n"
  fi
  
  text "Image Registry:\n"
  for SINGLE_IMAGE in $IMAGE_LIST; do
    text " $SINGLE_IMAGE\n"
  done
  for SINGLE_IMAGE in $BOOTSTRAP_IMAGE_LIST; do
    text " $SINGLE_IMAGE\n"
  done
  for SINGLE_IMAGE in $UTILITY_IMAGE_LIST; do
    text " $SINGLE_IMAGE\n"
  done

  if ! is_initialized; then
    text "${CHE_ENVIRONMENT_FILE}: not initialized\n"
  else
    text "${CHE_ENVIRONMENT_FILE}:\n"
    # Implement loop of all lines that are uncommented.
    CONFIGURATION_ARRAY=($(grep "^[^#]" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"))
    for SINGLE_CONFIGURATION in "${CONFIGURATION_ARRAY[@]}"; do
      text " $SINGLE_CONFIGURATION\n"
    done
  fi
}

cmd_network() {
  info ""
  info "---------------------------------------"
  info "--------   CONNECTIVITY TEST   --------"
  info "---------------------------------------"

  info "network" "${BOOTSTRAP_IMAGE_CHEIP}: ${CHE_HOST}"

  start_test_server

  info "network" "Browser => Workspace Agent (localhost): Connection $(test1 && echo "succeeded" || echo "failed")"
  info "network" "Browser => Workspace Agent ($AGENT_EXTERNAL_IP): Connection $(test2 && echo "succeeded" || echo "failed")"
  info "network" "Server  => Workspace Agent (External IP): Connection $(test3 && echo "succeeded" || echo "failed")"
  info "network" "Server  => Workspace Agent (Internal IP): Connection $(test4 && echo "succeeded" || echo "failed")"

  stop_test_server
}
