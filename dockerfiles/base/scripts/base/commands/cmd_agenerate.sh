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
    options=("che-agent-archetype                 - Assembly with sample agent" \
             "che-plugin-ide-menu-archetype       - Assembly with IDE extension to customize menu" \
             "che-plugin-ide-wizard-archetype     - Assembly with custom C project type extension" \
             "codenvy-plugin-ide-wizard-archetype - Assembly with Codenvy packaging")
    select opt in "${options[@]}"
    do
      case $opt in
        "che-agent-archetype                 - Assembly with sample agent")
          ARCHETYPE_ID="che-agent-archetype"
          break
          ;;
        "che-plugin-ide-menu-archetype       - Assembly with IDE extension to customize menu")
          ARCHETYPE_ID="che-plugin-ide-menu-archetype"
          break
          ;;
        "che-plugin-ide-wizard-archetype     - Assembly with custom C project type extension")
          ARCHETYPE_ID="che-plugin-ide-wizard-archetype"
          break
          ;;
        "codenvy-plugin-ide-wizard-archetype - Assembly with Codenvy packaging")
          ARCHETYPE_ID="codenvy-plugin-ide-wizard-archetype"
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
  fi

  GENERATE_COMMAND="docker run -it --rm --name generate-che ${WRITE_PARAMETERS} \
                   -v /var/run/docker.sock:/var/run/docker.sock \
                   -v \"${M2_MOUNT}\":/home/user/.m2/repository \
                   -v \"${ARCHETYPE_MOUNT}\":/home/user/che-build \
                   -w /home/user/che-build \
                      ${UTILITY_IMAGE_DEV} \
                          mvn -Dmaven.repo.local=/home/user/.m2/repository/ org.apache.maven.plugins:maven-archetype-plugin:2.4:generate \
                              -DarchetypeRepository=http://maven.codenvycorp.com/content/groups/public/ \
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
