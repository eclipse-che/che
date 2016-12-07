#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

IMAGE_NAME="eclipse/che-server"
source $(cd "$(dirname "$0")"; pwd)/../build.include

# grab assembly
DIR=$(cd "$(dirname "$0")"; pwd)
if [[ ! -d "${DIR}/../../assembly/assembly-main/target" ]]; then
  echo "${ERRO}Have you built assembly/assemby-main in ${DIR}/../assembly/assembly-main 'mvn clean install'?"
  exit 2
fi

# Use of folder
ASSEMBLY_DIR=$(echo ${DIR}/../../assembly/assembly-main/target/eclipse-che-*/eclipse-che-*)

# Remove current copy of the assembly if present
if [[ -d "${DIR}/assembly" ]]; then
  echo "Remove previous assembly folder"
  rm -rf ${DIR}/assembly
fi

# Copy assembly
cp -r ${ASSEMBLY_DIR} ${DIR}/assembly

init
build
