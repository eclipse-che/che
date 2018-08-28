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


function log() {
    echo ""
}

load '/bats-support/load.bash'
load '/bats-assert/load.bash'
source /dockerfiles/cli/tests/test_base.sh
source /dockerfiles/base/scripts/base/startup_03_pre_networking.sh


@test "Check isDocker4Mac is true" {
   # GIVEN
   UNAME_R="4.9.4-moby"

   # WHEN
   run is_docker_for_mac

   # THEN
   assert_success
}

@test "Check isDocker4Mac is true (new docker releases)" {
   # GIVEN
   UNAME_R="4.9.44-linuxkit-aufs"

   # WHEN
   run is_docker_for_mac

   # THEN
   assert_success
}


@test "Check isDocker4Mac is false on Linux kernel" {
   # GIVEN
   UNAME_R="4.4.0-21-generic"

   # WHEN
   run is_docker_for_mac

   # THEN
   assert_failure
}


@test "Check isDocker4Win is false on Linux kernel" {
   # GIVEN
   UNAME_R="4.4.0-21-generic"

   # WHEN
   run is_docker_for_windows

   # THEN
   assert_failure
}


@test "Check isDocker4Win is true" {
   # GIVEN
   UNAME_R="4.9.4-moby"
   GLOBAL_HOST_IP="10.0.75.2"

   # WHEN
   run is_docker_for_windows

   # THEN
   assert_success
}

@test "Check isDocker4Win is true (new docker releases)" {
   # GIVEN
   UNAME_R="4.9.44-linuxkit-aufs"
   GLOBAL_HOST_IP="10.0.75.2"

   # WHEN
   run is_docker_for_windows

   # THEN
   assert_success
}