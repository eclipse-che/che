#!/usr/bin/env bats
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   David Festal


function log() {
    echo ""
}

load '/bats-support/load.bash'
load '/bats-assert/load.bash'
source /dockerfiles/cli/tests/test_base.sh
source /dockerfiles/base/scripts/base/startup_03_pre_networking.sh

@test "test overriding the CLI images" {
  #GIVEN
  IMAGES="IMAGE_INIT=eclipse/che-init:nightly"
  IMAGE_INIT="dfestal/che-init:nightly"

  #WHEN
  set_variables_images_list "${IMAGES}"

  #THEN
  [[ "${IMAGE_INIT}" == ""dfestal/che-init:nightly"" ]]
}

@test "test not overriding the CLI images" {
  #GIVEN
  IMAGES="IMAGE_INIT=eclipse/che-init:nightly"

  #WHEN
  set_variables_images_list "${IMAGES}"

  #THEN
  [[ "${IMAGE_INIT}" == ""eclipse/che-init:nightly"" ]]
}
