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
