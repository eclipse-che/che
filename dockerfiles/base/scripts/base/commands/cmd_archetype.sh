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

help_cmd_archetype() {
  text "\n"
  text "USAGE: DOCKER_PARAMS ${CHE_IMAGE_FULLNAME} archetype ACTION [PARAMETERS]\n"
  text "\n"
  text "Use an archetype to generate, build or run a custom ${CHE_MINI_PRODUCT_NAME} assembly\n"
  text "\n"
  text "MANDATORY DOCKER PARAMETERS:\n"
  text "  -v <path>:/archetype       Local path where your custom assembly will be generated\n"
  text "\n"
  text "OPTIONAL DOCKER PARAMETERS:\n"
  text "  -v <path>:/m2              Local path to your host's Maven M2 repository\n"
  text "\n"
  text "ACTION:\n"
  text "  generate                    Generate a new custom assembly to folder mounted to '/archetype'\n"
  text "  build                       Uses 'eclipse/che-dev' image to compile archetype in '/archetype'\n"
  text "  run                         Starts ${CHE_MINI_PRODUCT_NAME} from custom assembly in '/archetype'\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --arch=<id>                 Different archetypes generate different types of customizations\n"
  text "  --version=<version>         Sets archetype and custom assembly version - default = tag of CLI image\n"
  text "  --group=<group>             Sets groupId of generated assembly - default = com.sample\n"
  text "  --id=<id>                   Sets artifaceId of generated assembly - default = assembly\n"
  text "  --no:interactive            Disables interactive mode\n"
  text "\n"
  text "An assembly is a bundling of extensions, plugins, stacks, agents, branding elements, and a CLI\n"
  text "that can be built into a new binary for distribution. In essence, an assemly is a custom ${CHE_MINI_PRODUCT_NAME}.\n"
  text "\n"
  text "An archetype is a maven technique for generating code templates. A single archetype has an ID and\n"
  text "generates a complete custom assembly. Differnent archetypes generate assemblies with different\n"
  text "types of customizations. We make each archetype customize the minimal number of features to make\n"
  text "learning about customizations simpler.\n"
  text "\n"
  text "Your host system must have Maven 3.3+ installed to facilitate generation and compiling of custom\n"
  text "assemblies. You must pass in your Maven's M2 repository path on your host. Our archetype generator\n"
  text "will download libraries into that repository making repeated compilations faster over time.\n"
  text "On most Linux based systems, your M2 is located at '/home/user/.m2' and it is '%%USERPROFILE%%/.m2'\n"
  text "for Windows. We default your M2 repository to '/home/user/.m2'. Use the '/m2' mount to chnage this.\n"
  text "\n"
  text "Your custom assembly will be generated in the host path mounted to '/archetype'. This generates a \n"
  text "Maven multi-module project. You can enter the folder and build it with 'mvn clean install' or use\n"
  text "this utility to build it. Compiling an assembly requires other tools like Java, Angular, Go to be\n"
  text "installed on your host system. However, if you use this tool to compile your custom assembly we\n"
  text "use 'eclipse/che-dev' Docker image which contains all of these utilities preinstalled. It is simple\n"
  text "but is a large download >1GB and compilation is slower than using your host since the Docker\n"
  text "container is performing compilation against files that are host-mounted.\n"
}

pre_cmd_archetype() {
  # Not loaded as part of the init process to save on download time

  UTILITY_IMAGE_DEV="eclipse/che-dev"

  if ! is_fast && ! skip_pull; then
    load_utilities_images_if_not_done
    update_image $UTILITY_IMAGE_DEV
  fi

  if [ $# -eq 0 ]; then 
    help_cmd_archetype
    return 2;
  fi

  ARCHETYPE_ACTION="generate"
  ARCHETYPE_ID="che-plugin-ide-menu-archetype"
#  ARCHETYPE_VERSION=$(get_image_version)

##############################
# REPLACE THIS WITH $(get_image_version) AFTER CI SYSTEMS GENERATING
  ARCHETYPE_VERSION=5.3.0-SNAPSHOT   
  ASSEMBLY_GROUP="com.sample"
  ASSEMBLY_ID="assembly"
  SKIP_INTERACTIVE=false

  for i in "$@"
  do
    case $1 in
      generate|build|run)
        ARCHETYPE_ACTION=$1
        shift
        ;;

      --arch=*)
        ARCHETYPE_ID="${i#*=}"
        shift 
        ;;

      --version=*)
        ARCHETYPE_VERSION="${i#*=}"
        shift 
        ;;

      --group=*)
        ASSEMBLY_GROUP="${i#*=}"
        shift 
        ;;

      --id=*)
        ASSEMBLY_ID="${i#*=}"
        shift 
        ;;

      --no:interactive)
        SKIP_INTERACTIVE=true
        shift 
        ;;

      *)
        # unknown option
        error "You passed an unknown command line option."
        return 2
        ;;
    esac
  done

  ARCHETYPE_MOUNT=$(get_container_folder ":/archetype")
  M2_MOUNT=$(get_container_folder ":/m2")

  if [[ "${ARCHETYPE_MOUNT}" = "not set" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME custom assembly generator!"
    info ""
    info "We could not detect a location to create your custom assembly."
    info "Volume mount a local directory to ':/archetype'."
    info ""
    info "Syntax:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <DATA_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                      -v <ASSEMBLY_LOCAL_PATH>:/archetype"
    info "                         ${CHE_IMAGE_FULLNAME} archetype $*"
    info ""
    return 2;
  fi

  if [[ "${M2_MOUNT}" = "not set" ]]; then
     warning "archetype" "Maven M2 not detected - setting to '/home/user/.m2'"    
     M2_MOUNT="/home/user/.m2"
  fi
}


cmd_archetype() {
  cd /archetype
  case $ARCHETYPE_ACTION in
    generate)
      archetype_generate
    ;;
    build)
      archetype_build
    ;;
    run)
      archetype_run
    ;;
  esac
}

archetype_generate() {
  if ! $SKIP_INTERACTIVE; then 
    info "archetype" "Welcome to $CHE_FORMAL_PRODUCT_NAME custom assembly generator!"
    info "archetype" ""
    info "archetype" "You can skip this message with '--no:interactive'."
    info "archetype" ""
    info "archetype" "This generator requires:"
    info "archetype" "  1. Maven 3.3+ to be installed on your host."
    info "archetype" "  2. Your Maven M2 repo mounted to ':/m2'."
    info "archetype" "  3. A local path for us to place the assembly mounted to ':/archetype'."
    info ""
    
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

  docker run -it --rm --name generate-che              \
       -v /var/run/docker.sock:/var/run/docker.sock \
       -v "${M2_MOUNT}":/home/user/.m2         \
       -v "${ARCHETYPE_MOUNT}":/home/user/che-build         \
       -w /home/user/che-build                              \
         ${UTILITY_IMAGE_DEV}                          \
           mvn archetype:generate               \
              -DarchetypeRepository=http://maven.codenvycorp.com/content/groups/public/ \
              -DarchetypeGroupId=org.eclipse.che.archetype \
              -DarchetypeArtifactId=$ARCHETYPE_ID \
              -DarchetypeVersion=$ARCHETYPE_VERSION \
              -DgroupId=$ASSEMBLY_GROUP \
              -DartifactId=$ASSEMBLY_ID \
              -Dversion=$ARCHETYPE_VERSION \
              -DinteractiveMode=false
}

archetype_build() {
  cd /archetype/$ASSEMBLY_ID
  docker run -it --rm --name build-che \
       -v /var/run/docker.sock:/var/run/docker.sock \
       -v "${M2_MOUNT}"/repository:/home/user/.m2/repository \
       -v "${ARCHETYPE_MOUNT}/${ASSEMBLY_ID}":/home/user/che-build \
       -w /home/user/che-build \
          ${UTILITY_IMAGE_DEV} mvn clean install
}

archetype_run() {
  cd /archetype/$ASSEMBLY_ID
  docker run -it --rm --name run-che \
       -v /var/run/docker.sock:/var/run/docker.sock \
       -v "${DATA_MOUNT}":"${CHE_CONTAINER_ROOT}" \
       -v "${ARCHETYPE_MOUNT}"/$ASSEMBLY_ID/assembly-main/target/eclipse-che-$ARCHETYPE_VERSION/eclipse-che-$ARCHETYPE_VERSION:/assembly \
         ${CHE_IMAGE_FULLNAME} start --skip:nightly
         echo 'hi'
}
