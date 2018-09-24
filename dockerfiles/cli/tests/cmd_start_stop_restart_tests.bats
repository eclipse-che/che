#!/usr/bin/env bats
# Copyright (c) 2012-2017 Red Hat, Inc
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
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
  execute_cli_command --che-data-path=${tmp_path} --che-container-name=chetest --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"

  #THEN
  check_che_state --che-container-name="chetest"
}

@test "test cli 'stop' command with default settings" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_stop_with_default_settings
  mkdir -p "${tmp_path}"
  execute_cli_command --che-data-path=${tmp_path} --che-container-name=chetest --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state --che-container-name="chetest"
  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-container-name=chetest --che-cli-command=stop --che-cli-extra-options="--skip:nightly --skip:pull"

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
  execute_cli_command --che-data-path=${tmp_path} --che-container-name=chetest --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state --che-container-name="chetest"
  che_container_id=$(docker inspect --format="{{.Id}}" chetest)

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-container-name=chetest --che-cli-command=restart --che-cli-extra-options="--skip:nightly --skip:pull"

  #THEN
  [[ "$(docker inspect --format="{{.Id}}" chetest)" != "$che_container_id" ]]
  check_che_state --che-container-name="chetest"
}

@test "test cli 'start' with custom port" {
  #GIVEN
  tmp_path=${TESTRUN_DIR}/cli_cmd_start_with_custom_port
  free_port=$(get_free_port)

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-port=$free_port --che-container-name=chetest --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  #THEN
  check_che_state --che-container-name="chetest" --che-port="$free_port"
}
