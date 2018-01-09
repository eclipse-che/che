#!/bin/bash
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

. ./build.include
init "$@"

USAGE="Specify assembly-che to run (--che or --codenvy)"

if [ $# = 0 ]; then
    echo $USAGE
    exit 1
fi

case $1 in
    --che6 )	   CLI_IMAGE=eclipse/che-cli:nightly
                    DATA_MOUNT=$HOME/.che6/sample/data
                    ASSEMBLY_MOUNT="-v $PWD/assembly/assembly-main/target/eclipse-che-6.0.0-M4-SNAPSHOT/eclipse-che-6.0.0-M4-SNAPSHOT:/assembly"
                    ;;
    --che6-vanilla )	   CLI_IMAGE=eclipse/che-cli:nightly
                    DATA_MOUNT=$HOME/.che6/sample/data
                    ASSEMBLY_MOUNT=
                    ;;
    --che6mu )     CLI_IMAGE=eclipse/che-cli:nightly
                   DATA_MOUNT=$HOME/.che6mu/sample/data
                   ASSEMBLY_MOUNT="-v $PWD/assembly/assembly-main/target/eclipse-che-6.0.0-M4-SNAPSHOT/eclipse-che-6.0.0-M4-SNAPSHOT:/assembly"
                   DOCKER_RUN_OPTIONS="-e CHE_MULTIUSER=true"
		            ;;
    * )            echo $USAGE
                   exit 1
esac

#TODO detect version of assembly-che. detect version of che
docker_exec run --dns 8.8.8.8 -it --rm  ${DOCKER_RUN_OPTIONS}  \
         -v /var/run/docker.sock:/var/run/docker.sock \
         -v "$DATA_MOUNT:/data" \
         $ASSEMBLY_MOUNT \
         $CLI_IMAGE start --fast --skip:scripts --debug
