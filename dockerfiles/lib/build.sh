#!/bin/bash
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}"/../build.include

DIR=$(cd "$(dirname "$0")"; pwd)

generate_dto() {
  echo "Checking DTO"

  # if file already exists and in snapshot mode
  POM_VERSION=$(cat "${DIR}"/dto-pom.xml | grep "^        <version>.*</version>$" | awk -F'[><]' '{print $3}')
  if [ -e "${DIR}/src/api/dto/che-dto.ts" ]; then
    # DTO file exists, Do we have snapshot ?
    if [[ ${POM_VERSION} != *"SNAPSHOT"* ]]
    then
      if [ ${DIR}/src/api/dto/che-dto.ts -nt ${DIR}/dto-pom.xml ]; then
        echo "Using tagged version and dto file is up-to-date. Not generating it."
        return
      else
        echo "Using tagged version but DTO file is older than dto-pom.xml file. Need to generate again."
      fi
    else
      echo "Snapshot version is used in pom.xml. Generating again pom.xml";
    fi
  fi

  DTO_CONTENT=$(cd "${DIR}" && docker run -i --rm -v "$HOME/.m2:/root/.m2" -v "$PWD"/dto-pom.xml:/usr/src/mymaven/pom.xml -w /usr/src/mymaven maven:3.3-jdk-8 /bin/bash -c "mvn -q -U -DskipTests=true -Dfindbugs.skip=true -Dskip-validate-sources install  && cat target/dto-typescript.ts")

  # Check if maven command has worked or not
  if [ $? -eq 0 ]; then
    # Create directory if it doesn't exist
    if [ ! -d "${DIR}/src/api/dto" ]; then
      mkdir ${DIR}/src/api/dto
    fi
    echo 'DTO has been generated'
    echo "${DTO_CONTENT}" > "${DIR}"/src/api/dto/che-dto.ts
  else
    echo "Failure when generating DTO. Error was ${DTO_CONTENT}"
    exit 1
  fi
}


native_build() {
  ./node_modules/typescript/bin/tsc --project .
}

init --name:lib "$@"
generate_dto

DIR=$(cd "$(dirname "$0")"; pwd)
echo "Building Docker Image ${IMAGE_NAME} from $DIR directory"
cd "${DIR}" && docker build -t ${IMAGE_NAME} .
if [ $? -eq 0 ]; then
  printf "${GREEN}Script run successfully: ${BLUE}${IMAGE_NAME}${NC}\n"
else
  printf "${RED}Failure when building docker image ${IMAGE_NAME}${NC}\n"
  exit 1
fi
