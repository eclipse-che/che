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

cmd_version() {
  debug $FUNCNAME

  # Do not perform any logging in this method as it is runnable before the system is bootstrap
  echo ""
  text "Your CLI version is '%s'.\n" $(get_image_version)
  if is_initialized; then
    text "Your installed version is '%s'.\n" $(get_installed_version)
  else
    text "Your installed version is '<not-installed>'.\n"
  fi
  text "Available on DockerHub:\n"
  VERSION_LIST_JSON=$(curl -s https://hub.docker.com/v2/repositories/${CHE_IMAGE_NAME}/tags/)
  NUMBER_OF_VERSIONS=$(echo $VERSION_LIST_JSON | jq '.count')
  COUNTER=0
  while [  $COUNTER -lt $NUMBER_OF_VERSIONS ]; do
    TAG=$(echo $VERSION_LIST_JSON | jq ".results[$COUNTER].name")
#    DATE=$(echo $VERSION_LIST_JSON | jq ".results[$COUNTER].last_updated")
#   DATE=${DATE:0:10}
#    text "${DATE//\"}            ${TAG//\"}\n"
    text "  ${TAG//\"}\n"
    let COUNTER=COUNTER+1 
  done
}
