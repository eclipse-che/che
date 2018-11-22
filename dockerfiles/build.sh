#!/bin/bash
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# See: https://sipb.mit.edu/doc/safe-shell/

. ./build.include
init "$@"

DIRECTORIES_PROCESSED=""

# if there is a .require file in that directrory, call that directory first (if not done)
build_directory() {
  local directory="$1"
  local require_file=${directory}.require

  if [ -e ${require_file} ] ; then
    while IFS= read -r required_directory; do
      # if required image is not yet built, build it
      if echo ${DIRECTORIES_PROCESSED} | grep "${required_directory}/"; then
        printf "${BROWN}${required_directory} dependency already built [SKIP]${NC}\n"
      else
        printf "${PURPLE}Build required dependency ${required_directory}${NC}\n"
        build_directory "${required_directory}/"
      fi
    done < ${require_file}
  fi
  shift
  # Calling build.sh
  if [ -e ${directory}/build.sh ] ; then
    ${directory}build.sh ${OPTIONS} ${ARGS}
    DIRECTORIES_PROCESSED="${DIRECTORIES_PROCESSED} ${directory}"
  else
    printf "${RED}No build.sh in directory ${directory}${NC}\n"
    exit 2
  fi
}

build_all() {
  # loop on all directories and call build.sh script if present
  for directory in */ ; do
    if [ -e ${directory}/build.sh ] ; then
      build_directory ${directory}
    else
      printf "${RED}skipping ${directory} as there is no build.sh script${NC}\n"
    fi
  done
}

build_custom() {
  echo "directories are $ARGS and options $OPTIONS"
  # loop on provided directories by the user
   for directory in $(echo ${ARGS}); do
     build_directory "${directory}/" ${OPTIONS}
   done

}

if [ "${ARGS}" = "" ]; then
  build_all
else
  build_custom
fi

if [ $? -eq 0 ]; then
  echo "${GREEN}All images have been generated successfully${NC}"
else
  echo "${RED}Failure when building a docker image"
  exit 1
fi
