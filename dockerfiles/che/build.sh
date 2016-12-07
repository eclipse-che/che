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
if [ ! -d "${DIR}/../../assembly/assembly-main/target" ]; then
  echo "${ERRO}Have you built assembly/assemby-main in ${DIR}/../assembly/assembly-main 'mvn clean install'?"
  exit 2
fi

# Use of folder
ASSEMBLY_DIR=$(echo ${DIR}/../../assembly/assembly-main/target/eclipse-che-*/eclipse-che-*)
LOCAL_ASSEMBLY=${DIR}/assembly

if [ -d "${DIR}/assembly" ]; then
  if [ ${ASSEMBLY_DIR} -nt ${DIR}/assembly ]; then
    echo "There is new version of che-assembly, need to copy again"
    rm -rf ${LOCAL_ASSEMBLY}
    cp -r ${ASSEMBLY_DIR} ${LOCAL_ASSEMBLY}
  else
    echo "Copy of current assembly is up-to-date, skip copy."
  fi
  else
    echo "Copying assembly file"
    cp -r ${ASSEMBLY_DIR} ${LOCAL_ASSEMBLY}
fi

init
build
