#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

. ./build.include
init "$@"

# loop on all libraries first
for directory in lib*/ ; do
  if [ -e ${directory}/build.sh ] ; then
   echo "Call buid.sh from ${directory}"
   ${directory}build.sh "$@"
 else
   echo "skipping ${directory}"
 fi
done

# loop on all directories and call build.sh script if present
for directory in */ ; do
  if [[ ${directory} == lib*/ ]] ; then
    # skipping lib directory
    :
  elif [ -e ${directory}/build.sh ] ; then
    echo "Call buid.sh from ${directory}"
    ${directory}build.sh "$@"
  else
    echo "skipping ${directory}"
  fi
done


if [ $? -eq 0 ]; then
  echo "${GREEN}All images have been generated successfully${NC}"
else
  echo "${RED}Failure when building a docker image"
  exit 1
fi
