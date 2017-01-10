#!/usr/bin/env bats
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Mario Loriedo - Initial implementation
#   Tyler Jewell - Improvements
#   Marian Labuda - Improvements
#
# To run the tests:
#   docker run -w /tests/ -v $PWD:/tests dduportal/bats:0.4.0 /tests/launcher_test.bats
#

source ./launcher_funcs.sh

#source ./launcher.sh

# Set up clean environment 
setup() {
  if [ "$(docker ps -qa -f status=exited | wc -l)" -gt 0 ]; then
    docker rm $(docker ps -qa -f status=exited)
  fi
}

@test "clean folder path that is already clean" {
  result="$(get_clean_path /somefolder)"
  [ "$result" = "/somefolder" ]
}

@test "clean folder path with extra slash" {
  result="$(get_clean_path /somefolder/)"
  [ "$result" = "/somefolder" ]
}

@test "clean folder path with two consecutive slashes" {
  result="$(get_clean_path /some//path)"
  [ "$result" = "/some/path" ]
}

@test "clean folder path with backslashes" {
  result="$(get_clean_path \\some\\path)"
  [ "$result" = "/some/path" ]
}

@test "clean folder path with quotes" {
  result="$(get_clean_path \"/some\"/path\")"
  [ "$result" = "/some/path" ]
}

@test "wait for che container that never stops" {
  export CHE_SERVER_CONTAINER_NAME="wait-for-che-test1"
  docker run -d --name ${CHE_SERVER_CONTAINER_NAME} alpine:3.4 ping localhost
  wait_until_container_is_stopped 2
  run che_container_is_stopped
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}
  [ "$status" -eq 1 ]
}

@test "wait for che container that stops" {
  export CHE_SERVER_CONTAINER_NAME="wait-for-che-test2"
  docker run -d --name ${CHE_SERVER_CONTAINER_NAME} alpine:3.4 ping -c 2 localhost
  wait_until_container_is_stopped 3
  run che_container_is_stopped
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}
  [ "$status" -eq 0 ]
}

@test "get container conf folder that is set" {
  # Given
  export CHE_SERVER_CONTAINER_NAME="che-test-get-container-conf-folder"
  CONF_FOLDER=$(pwd)/che-test-conf
  mkdir ${CONF_FOLDER} -p
  docker run --name ${CHE_SERVER_CONTAINER_NAME} -v ${CONF_FOLDER}:/conf:Z alpine:3.4 true

  # When
  result=$(get_che_container_conf_folder $CHE_SERVER_CONTAINER_NAME)
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}
  rmdir ${CONF_FOLDER}

  # Then
  [ "$result" = "${CONF_FOLDER}" ]
}

@test "get container conf folder that is not set" {
  # Given
  export CHE_SERVER_CONTAINER_NAME="che-test-get-container-conf-folder"
  docker run --name ${CHE_SERVER_CONTAINER_NAME} alpine:3.4 true

  # When
  result=$(get_che_container_conf_folder $CHE_SERVER_CONTAINER_NAME)
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}

  # Then
  [ "$result" = "not set" ]
}

@test "get container data folder" {
  # Given
  export CHE_SERVER_CONTAINER_NAME="che-test-get-container-data-folder"
  DATA_FOLDER=$(pwd)/che-test-data
  mkdir ${DATA_FOLDER} -p
  docker run --name ${CHE_SERVER_CONTAINER_NAME} -v ${DATA_FOLDER}:/home/user/che/workspaces:Z alpine:3.4 true

  # When
  result=$(get_che_container_data_folder $CHE_SERVER_CONTAINER_NAME)
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}
  rmdir ${DATA_FOLDER}

  # Then
  [ "$result" = "${DATA_FOLDER}" ]
}

@test "get image name" {
  # Given
  export CHE_SERVER_CONTAINER_NAME="che-test-get-container-image-name"
  docker run --name ${CHE_SERVER_CONTAINER_NAME} alpine:3.4 true

  # When
  result=$(get_che_container_image_name $CHE_SERVER_CONTAINER_NAME) 
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}

  # Then
  [ "$result" = "alpine:3.4" ]
}

@test "get che server container id" {
  # Given
  export CHE_SERVER_CONTAINER_NAME="che-test-get-container-id"
  long_id=$(docker run -d --name ${CHE_SERVER_CONTAINER_NAME} alpine:3.4 true)

  # When
  result=$(get_che_server_container_id ${CHE_SERVER_CONTAINER_NAME})
  docker rm -f ${CHE_SERVER_CONTAINER_NAME}

  # Then
  [ "$result" = "$long_id" ]
}

@test "get docker daemon version" {
  # When
  result=$(get_docker_daemon_version)

  # Then
  [ "$result" ]
}

# could be added check on host OS
@test "get docker host os" {
  # When
  result=$(get_docker_host_os)

  # Then
  [ "$result" ]
}

# get_che_launcher_container_id should be called in container
@test "get che launcher version with nightly" {
  # Given
  export CHE_LAUNCHER_CONTAINER_NAME="che-test-get-che-launcher-version"
  long_id=$(docker run -d --name ${CHE_LAUNCHER_CONTAINER_NAME} --entrypoint=true eclipse/che-launcher:nightly)
  image_name=$(get_che_container_image_name ${long_id})

  # When
  wait_until_container_is_stopped 5 ${long_id}
  result=$(get_che_image_version ${image_name})
  docker rm $CHE_LAUNCHER_CONTAINER_NAME

  # Then
  [ "$result" = "nightly" ]
}

#get_che_launcher_container_id should run in container
@test "get che get che launcher version with no specific version" {
  # Given
  export CHE_SERVER_CONTAINER_NAME="che-test-get-che-launcher-version"
  long_id=$(docker run -d --name ${CHE_SERVER_CONTAINER_NAME} --entrypoint=true eclipse/che-launcher)
  image_name=$(get_che_container_image_name ${long_id})

  # When
  wait_until_container_is_stopped 5 ${long_id}
  result=$(get_che_image_version ${image_name})
  docker rm $CHE_SERVER_CONTAINER_NAME

  # Then
  [ "$result" = "latest" ]
}
