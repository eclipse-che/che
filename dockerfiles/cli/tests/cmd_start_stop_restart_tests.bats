#!/usr/bin/env bats
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Marian Labuda - Initial Implementation
#   Roman Iuvshyn

source /dockerfiles/cli/tests/test_base.sh

# Kill running che server instance if there is any to be able to run tests
setup() {
  kill_running_named_container chetest
  remove_named_container chetest
}

teardown() {
  kill_running_named_container chetest
  remove_named_container chetest
}

@test "test cli 'start' command with default settings" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_start_with_default_params
  mkdir -p "${tmp_path}"

  #WHEN
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data -e CHE_CONTAINER=chetest $CLI_IMAGE start --skip:nightly --skip:pull

  #THEN
  [[ "$(docker inspect --format='{{.State.Running}}' chetest)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} chetest)
  curl -fsS http://${ip_address}:8080  > /dev/null
}

@test "test cli 'stop' command with default settings" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_stop_with_default_settings
  mkdir -p "${tmp_path}"
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data -e CHE_CONTAINER=chetest $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' chetest)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} chetest)
  curl -fsS http://${ip_address}:8080  > /dev/null

  #WHEN
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data -e CHE_CONTAINER=chetest $CLI_IMAGE stop --skip:nightly --skip:pull

  #THEN
  #check that container is stopped and removed
  [[ "$(docker ps -a)" != *"chetest"* ]]
}

@test "test cli 'restart' command with default settings" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_restart_with_default_settings
  mkdir -p "${tmp_path}"
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data -e CHE_CONTAINER=chetest $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' chetest)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} chetest)
  curl -fsS http://${ip_address}:8080  > /dev/null
  che_container_id=$(docker inspect --format="{{.Id}}" chetest)

  #WHEN
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data -e CHE_CONTAINER=chetest $CLI_IMAGE restart --skip:nightly --skip:pull

  #THEN
  [[ "$(docker inspect --format="{{.Id}}" chetest)" != "$che_container_id" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} chetest)
  curl -fsS http://${ip_address}:8080  > /dev/null
}

@test "test cli 'start' with custom port" {
  #GIVEN
  tmp_path=${TESTRUN_DIR}/cli_cmd_start_with_custom_port
  free_port=$(get_free_port)

  #WHEN
  docker run --rm -e CHE_PORT=$free_port -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data -e CHE_CONTAINER=chetest $CLI_IMAGE start --skip:nightly --skip:pull

  #THEN
  [[ "$(docker inspect --format='{{.State.Running}}' chetest)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} chetest)
  curl -fsS http://${ip_address}:${free_port}  > /dev/null
}
