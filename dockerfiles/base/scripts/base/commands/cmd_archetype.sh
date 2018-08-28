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

help_cmd_archetype() {
  text "\n"
  text "USAGE: DOCKER_PARAMS ${CHE_IMAGE_FULLNAME} archetype ACTION [PARAMETERS]\n"
  text "\n"
  text "Use an archetype to generate, build or run a custom ${CHE_MINI_PRODUCT_NAME} assembly\n"
  text "\n"
  text "MANDATORY DOCKER PARAMETERS:\n"
  text "  -v <path>:/archetype        Local path where your custom assembly will be generated\n"
  text "\n"
  text "OPTIONAL DOCKER PARAMETERS:\n"
  text "  -v <path>:/m2               Local path to your host's Maven M2 repository\n"
  text "\n"
  text "ACTION:\n"
  text "  all                         Generate, build and run a new custom assembly\n"
  text "  generate                    Generate a new custom assembly to folder mounted to '/archetype'\n"
  text "  build                       Uses 'eclipse/che-dev' image to compile archetype in '/archetype'\n"
  text "  run                         Starts ${CHE_MINI_PRODUCT_NAME} from custom assembly in '/archetype'\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --che                       For run and stop commands. Select Che assembly\n"
  text "  --codenvy                   For run and stop commands. Select Codenvy assembly\n"
  text "  --archid=<id>               Different archetypes generate different types of customizations\n"
  text "  --archversion=<version>     Sets archetype version - default = tag of CLI image\n"
  text "  --version=<version>         Sets custom assembly version - default = archetype version\n"
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
  text "On most Linux based systems, your M2 repo is located at '/home/user/.m2/repository' and it is\n"
  text "'%%USERPROFILE%%/.m2/repostory 'for Windows. We default your M2 home to '/home/user/.m2'. If your.\n"
  text "local Maven pom.xml changes the location of the repository, put the full path to the repo.\n"
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
  if [ $# -eq 0 ]; then 
    help_cmd_archetype
    return 2;
  fi

  ARCHETYPE_ACTION="all"
  ARCHETYPE_ID="plugin-menu-archetype"

##############################
  ARCHETYPE_VERSION=$(get_image_version)
  ASSEMBLY_VERSION=$ARCHETYPE_VERSION
  ASSEMBLY_GROUP="com.sample"
  ASSEMBLY_ID="assembly"
  SKIP_INTERACTIVE=false
  ASSEMBLY_TYPE="che"

  for i in "$@"
  do
    case $1 in
      all|generate|build|run|stop)
        ARCHETYPE_ACTION=$1
        shift
        ;;

      --che)
        ASSEMBLY_TYPE="che"
        shift
        ;;

      --codenvy)
        ASSEMBLY_TYPE="codenvy"
        shift
        ;;

      --archid=*)
        ARCHETYPE_ID="${i#*=}"
        shift 
        ;;

      --archversion=*)
        ARCHETYPE_VERSION="${i#*=}"
        shift 
        ;;

      --version=*)
        ASSEMBLY_VERSION="${i#*=}"
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
     warning "archetype" "Maven M2 repository detected - setting to '/home/user/.m2/repository'"    
     M2_MOUNT="/home/user/.m2/repository"
  fi
}

cmd_archetype() {
  cd /archetype

  case $ARCHETYPE_ACTION in
    generate)
      cmd_lifecycle agenerate
    ;;
    build)
      cmd_lifecycle abuild
    ;;
    run)
      cmd_lifecycle arun
    ;;
    stop)
      cmd_lifecycle astop
    ;;
    all)
      cmd_lifecycle agenerate || true
      cmd_lifecycle abuild || true
      cmd_lifecycle arun || true
    ;;
  esac
}
