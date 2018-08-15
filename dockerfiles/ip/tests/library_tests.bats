#!/usr/bin/env bats
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Florent Benoit - Initial Implementation
#

load '/bats-support/load.bash'
load '/bats-assert/load.bash'
source /ip/src/library.sh

check_ip_result() {
  FILENAME="$1"
  # GIVEN
  [[ -f /ip/tests/ip-results/${FILENAME}.output ]] || exit 1
  IP_A_SHOW=$(cat /ip/tests/ip-results/${FILENAME}.output)
  [[ -f /ip/tests/ip-results/${FILENAME}.uname ]] || exit 1
  UNAME_R=$(cat /ip/tests/ip-results/${FILENAME}.uname)

  [[ -f /ip/tests/ip-results/${FILENAME}.interface ]] || exit 1
  local interface_name=$(cat /ip/tests/ip-results/${FILENAME}.interface)
  [[ -f /ip/tests/ip-results/${FILENAME}.expected ]] || exit 1
  local expected=$(cat /ip/tests/ip-results/${FILENAME}.expected)

  #WHEN
  run find_network_interface
  #THEN
  assert_output ${interface_name}
  assert_success

  # WHEN
  run get_ip_from_network_interface "${interface_name}"
  #THEN
  assert_output ${expected}
  assert_success

  #WHEN
  run get_ip_of_docker
  #THEN
  assert_output ${expected}
  assert_success



}

@test "Get ip based on ip a show interface (centos/native)" {
  check_ip_result "centos-native"
}

@test "Get ip based on ip a show interface (mac/docker4mac)" {
  check_ip_result "macos-docker-for-mac"
}

@test "Get ip based on ip a show interface (ubuntu/native)" {
  check_ip_result "ubuntu-native"
}

@test "Get ip based on ip a show interface (fedora/native)" {
  check_ip_result "fedora-native"
}

@test "Get ip based on ip a show interface (fedora/multiple-ips)" {
  check_ip_result "fedora-multiple-ips"
}

@test "Get ip based on ip a show interface (ubuntu/native/wlan)" {
  check_ip_result "ubuntu-native-wlan"
}

@test "Get ip based on ip a show interface (windows10/docker4windows)" {
  check_ip_result "windows10-docker-for-windows"
}

@test "Get ip based on ip a show interface (windows10/boot2docker)" {
  check_ip_result "windows10-boot2docker"
}

@test "Get ip based on ip a show interface (ubuntu/only docker network)" {
  check_ip_result "ubuntu-only-docker"
}

@test "Check isDocker4Mac/Win is true" {
   # GIVEN
   UNAME_R="4.9.4-moby"

   # WHEN
   run is_docker4MacOrWin

   # THEN
   assert_success
}

@test "Check isDocker4Mac/Win is true (new docker releases)" {
   # GIVEN
   UNAME_R="4.9.44-linuxkit-aufs"

   # WHEN
   run is_docker4MacOrWin

   # THEN
   assert_success
}

@test "Check isDocker4Mac/Win on Linux is false" {
   # GIVEN
   UNAME_R="4.4.0-21-generic"

   # WHEN
   run is_docker4MacOrWin

   # THEN
   assert_failure
}

@test "Check isBoot2Docker is true" {
   # GIVEN
   UNAME_R="boot2docker"

   # WHEN
   run is_boot2docker

   # THEN
   assert_success
}

@test "Check isBoot2Docker on Linux is false" {
   # GIVEN
   UNAME_R="4.4.0-21-generic"

   # WHEN
   run is_boot2docker

   # THEN
   assert_failure
}
