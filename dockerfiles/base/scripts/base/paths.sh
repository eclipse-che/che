#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Contains path utilities

check_host_volume_mount() {
  if is_boot2docker; then
    warning "Boot2docker detected - ensure :/data is mounted to %userprofile%"
  fi

  if file_system_writable "${CHE_CONTAINER_ROOT}/test"; then 
    delete_file_system_test "${CHE_CONTAINER_ROOT}/test"
  else 
    error "Unable to write files to your host"
    error "Have you enabled Docker to allow mounting host directories?"
    error "Did you give our CLI rights to create files on your host?"
    return 2;
  fi
}

file_system_writable() {
  echo 'test' > "${1}" 

  if [[ -f "${1}" ]]; then
    return 0
  else 
    return 1
  fi
}

delete_file_system_test() {
  rm -rf $1 > /dev/null 2>&1
}

get_mount_path() {
  debug $FUNCNAME
  FULL_PATH=$(get_full_path "${1}")
  POSIX_PATH=$(convert_windows_to_posix "${FULL_PATH}")
  CLEAN_PATH=$(get_clean_path "${POSIX_PATH}")
  echo $CLEAN_PATH
}

get_full_path() {
  debug $FUNCNAME
  # create full directory path
  echo "$(cd "$(dirname "${1}")"; pwd)/$(basename "$1")"
}

convert_windows_to_posix() {
  debug $FUNCNAME
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

convert_posix_to_windows() {
  debug $FUNCNAME
  # Remove leading slash
  VALUE="${1:1}"

  # Get first character (drive letter)
  VALUE2="${VALUE:0:1}"

  # Replace / with \
  VALUE3=$(echo ${VALUE} | tr '/' '\\' | sed 's/\\/\\\\/g')

  # Replace c\ with c:\ for drive letter
  echo "$VALUE3" | sed "s/./$VALUE2:/1"
}

get_clean_path() {
  debug $FUNCNAME
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}