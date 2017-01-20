#!/usr/bin/env bats
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Marian Labuda - Initial Implementation

source $BATS_BASE_DIR/tests/test_base.sh

@test "test CLI prompt to provide volume for docker sock" {
  #GIVEN
  prompt_substring="-v /var/run/docker.sock:/var/run/docker.sock"

  #WHEN
  result=$(docker run eclipse/che-cli:nightly start || true)

  #THEN
  [[ $result == *"$prompt_substring"* ]]
}

@test "test CLI prompt to provide directory for user data" {
  #GIVEN
  prompt_substring="-v <YOUR_LOCAL_PATH>:/data"

  #WHEN
  result=$(docker run -v $SCRIPTS_DIR:/scripts/base -v /var/run/docker.sock:/var/run/docker.sock $CLI_IMAGE start || true)

  #THEN
  [[ $result == *"$prompt_substring"* ]]
}

@test "test CLI 'usage' when running container without command" {
  #GIVEN
  output=$(usage || true)

  #WHEN
  result=$(docker run -v $SCRIPTS_DIR:/scripts/base -v /var/run/docker.sock:/var/run/docker.sock $CLI_IMAGE || true)

  #THEN
  [[ $result == *$usage* ]]
}


