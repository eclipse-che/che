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

# Kill running che server instance if there is any to be able to run tests
setup() {
  kill_running_named_container che
  remove_named_container che
}

@test "test cli 'start' with default settings" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path=${TESTRUN_DIR}/start1

  #WHEN
  docker run -v $SCRIPTS_DIR:/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v $tmp_path:/data $CLI_IMAGE start

  #THEN
  [[ $(docker inspect --format="{{.State.Running}}" che) -eq "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:8080  > /dev/null
}

@test "test cli 'start' with custom port" {
  #GIVEN
  tmp_path=${TESTRUN_DIR}/start2
  free_port=$(get_free_port)
  
  #WHEN
  docker run -e CHE_PORT=$free_port -v $SCRIPTS_DIR:/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v $tmp_path:/data $CLI_IMAGE start

  #THEN
  [[ $(docker inspect --format="{{.State.Running}}" che) -eq "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:${free_port}  > /dev/null
}

