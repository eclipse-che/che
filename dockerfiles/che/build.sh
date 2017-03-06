#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include


# grab assembly
DIR=$(cd "$(dirname "$0")"; pwd)
if [ ! -d "${DIR}/../../assembly/assembly-main/target" ]; then
  echo "${ERROR}Have you built assembly/assemby-main in ${DIR}/../assembly/assembly-main 'mvn clean install'?"
  exit 2
fi

# Use of folder
BUILD_ASSEMBLY_ZIP=$(echo "${DIR}"/../../assembly/assembly-main/target/eclipse-che-*.tar.gz)
LOCAL_ASSEMBLY_ZIP="${DIR}"/eclipse-che.tar.gz

if [ -f "${LOCAL_ASSEMBLY_ZIP}" ]; then
  rm "${LOCAL_ASSEMBLY_ZIP}"
fi

echo "Linking assembly ${BUILD_ASSEMBLY_ZIP} --> ${LOCAL_ASSEMBLY_ZIP}"
ln "${BUILD_ASSEMBLY_ZIP}" "${LOCAL_ASSEMBLY_ZIP}"

init --name:server "$@" 
build