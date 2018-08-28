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

pre_cmd_abuild() {
  UTILITY_IMAGE_DEV="eclipse/che-dev"

  if ! is_fast && ! skip_pull; then
    load_utilities_images_if_not_done
    update_image $UTILITY_IMAGE_DEV
  fi

  if [ ! -d /archetype/$ASSEMBLY_ID ]; then
    error "Assembly at ${ARCHETYPE_MOUNT}/$ASSEMBLY_ID not found."
    return 2
  fi  
}

cmd_abuild() {
  cd /archetype/$ASSEMBLY_ID

  WRITE_PARAMETERS=""
  if is_docker_for_mac || is_native; then
    WRITE_PARAMETERS+="-v /etc/group:/etc/group:ro "
    WRITE_PARAMETERS+="-v /etc/passwd:/etc/passwd:ro "
    WRITE_PARAMETERS+="--user $CHE_USER "
    IFS=$' '
    for TMP_GROUP in ${CHE_USER_GROUPS}; do
      WRITE_PARAMETERS+="--group-add ${TMP_GROUP}"
    done
  fi

  GENERATE_COMMAND="docker run -it --rm --name build-che ${WRITE_PARAMETERS} \
                   -v /var/run/docker.sock:/var/run/docker.sock \
                   -v \"${M2_MOUNT}\":/home/user/.m2/repository \
                   -v \"${ARCHETYPE_MOUNT}/${ASSEMBLY_ID}\":/home/user/che-build \
                   -w /home/user/che-build \
                      ${UTILITY_IMAGE_DEV} \
                          mvn clean install -pl '${ASSEMBLY_GROUP}.${ASSEMBLY_TYPE}:assembly-main' --am"
  log ${GENERATE_COMMAND}
  eval ${GENERATE_COMMAND}
}
