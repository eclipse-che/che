#!/usr/bin/env bats
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Mario Loriedo - Initial implementation
#
# To run the tests:
#   docker run -w /tests/ -v $PWD:/tests dduportal/bats:0.4.0 /tests/launcher_test.bats
#

source ./launcher_funcs.sh

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
