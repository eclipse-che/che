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

pre_cmd_agenerate() {
  # Not loaded as part of the init process to save on download time

  UTILITY_IMAGE_DEV="eclipse/che-dev"

  if ! is_fast && ! skip_pull; then
    load_utilities_images_if_not_done
    update_image $UTILITY_IMAGE_DEV
  fi
}

cmd_agenerate() {
  if ! $SKIP_INTERACTIVE; then 
    info "archetype" "Welcome to $CHE_FORMAL_PRODUCT_NAME custom assembly generator!"
    info "archetype" ""
    info "archetype" "You can skip this message with '--no:interactive'."
    info "archetype" ""
    info "archetype" "This generator requires:"
    info "archetype" "  1. Maven 3.3+ to be installed on your host."
    info "archetype" "  2. Your host Maven M2 repo mounted to ':/m2'."
    info "archetype" "  3. A local path for us to place the assembly mounted to ':/archetype'."

    text "\n"
    read -p "      Ready? [Y/n] " -n 1 -r
    text "\n"
    if [[ $REPLY =~ ^[Nn]$ ]]; then
      return 2
    fi

    PS3="Please enter your choice: "
    options=("agent-archetype                - Assembly with sample agent" \
             "plugin-menu-archetype          - Assembly with IDE extension to customize menu" \
             "plugin-wizard-archetype        - Assembly with custom C project type extension" \
             "plugin-serverservice-archetype - Assembly with simple IDE extesion and a server service" \
             "plugin-embedjs-archetype       - Assembly with simple IDE extesion for using native javascript in widgets" \
             "plugin-json-archetype          - Assembly with sample JSON project type, editor codeassistant, and workspace services" \
             "stacks-archetype               - Assembly with sample stack packaging module, for using custom stacks in assemblies")
    select opt in "${options[@]}"
    do
      case $opt in
        "agent-archetype                - Assembly with sample agent")
          ARCHETYPE_ID="agent-archetype"
          break
          ;;
        "plugin-menu-archetype          - Assembly with IDE extension to customize menu")
          ARCHETYPE_ID="plugin-menu-archetype"
          break
          ;;
        "plugin-wizard-archetype        - Assembly with custom C project type extension")
          ARCHETYPE_ID="plugin-wizard-archetype"
          break
          ;;
        "plugin-serverservice-archetype - Assembly with simple IDE extesion and a server service")
          ARCHETYPE_ID="plugin-serverservice-archetype"
          break
          ;;
        "plugin-embedjs-archetype       - Assembly with simple IDE extesion for using native javascript in widgets")
          ARCHETYPE_ID="plugin-embedjs-archetype"
          break
          ;;
        "plugin-json-archetype          - Assembly with sample JSON project type, editor codeassistant, and workspace services")
          ARCHETYPE_ID="plugin-json-archetype"
          break
          ;;
        "stacks-archetype               - Assembly with sample stack packaging module, for using custom stacks in assemblies")
          ARCHETYPE_ID="stacks-archetype"
          break
          ;;
        *) echo invalid option;;
      esac
    done
    text "\n"
  fi

  WRITE_PARAMETERS=""
  if is_docker_for_mac || is_native; then
    WRITE_PARAMETERS+="-v /etc/group:/etc/group:ro "
    WRITE_PARAMETERS+="-v /etc/passwd:/etc/passwd:ro "
    WRITE_PARAMETERS+="--user $CHE_USER "
    IFS=$' '
    for TMP_GROUP in ${CHE_USER_GROUPS}; do
      WRITE_PARAMETERS+="--group-add ${TMP_GROUP} "
    done
  fi

  GENERATE_COMMAND="docker run -it --rm --name generate-che ${WRITE_PARAMETERS} \
                   -v /var/run/docker.sock:/var/run/docker.sock \
                   -v \"${M2_MOUNT}\":/home/user/.m2/repository \
                   -v \"${ARCHETYPE_MOUNT}\":/home/user/che-build \
                   -w /home/user/che-build \
                      ${UTILITY_IMAGE_DEV} \
                          mvn -Dmaven.repo.local=/home/user/.m2/repository/ org.apache.maven.plugins:maven-archetype-plugin:2.4:generate \
                              -DarchetypeGroupId=org.eclipse.che.archetype \
                              -DarchetypeArtifactId=$ARCHETYPE_ID \
                              -DarchetypeVersion=$ARCHETYPE_VERSION \
                              -DgroupId=$ASSEMBLY_GROUP \
                              -DartifactId=$ASSEMBLY_ID \
                              -Dversion=$ASSEMBLY_VERSION \
                              -DinteractiveMode=false"
  log ${GENERATE_COMMAND}
  eval ${GENERATE_COMMAND}
}
