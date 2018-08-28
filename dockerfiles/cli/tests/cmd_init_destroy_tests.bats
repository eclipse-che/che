#!/usr/bin/env bats
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Marian Labuda - Initial Implementation

source /dockerfiles/cli/tests/test_base.sh

@test "test 'init' and 'destroy --quiet' with existing dir" {

  #GIVEN
  tmp_path="${TESTRUN_DIR}"/init-destroy1
  container_tmp_path=""${CONTAINER_TESTRUN_DIR}""/init-destroy1
  mkdir -p "${tmp_path}"

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=init --che-cli-extra-options="--skip:nightly --skip:pull"

  #THEN
  [[ -d "${container_tmp_path}"/docs ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=destroy --che-cli-extra-options="--quiet --skip:nightly --skip:pull"

  #THEN
  [[ ! -d "${container_tmp_path}"/docs ]]
  [[ ! -d "${container_tmp_path}"/instance ]]
  [[ ! -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]
  rm -rf "${container_tmp_path}"
}

@test "test 'init' and 'destroy --quiet' with non-existing dir" {

  #GIVEN
  tmp_path="${TESTRUN_DIR}"/init-destroy2
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/init-destroy2
 
  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=init --che-cli-extra-options="--skip:nightly --skip:pull 1>/dev/null"

  #THEN
  [[ -e "${container_tmp_path}" ]]
  [[ -d "${container_tmp_path}"/docs ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=destroy --che-cli-extra-options="--quiet --skip:nightly --skip:pull 1>/dev/null"
  
  #THEN
  [[ ! -d "${container_tmp_path}"/docs ]]
  [[ ! -d "${container_tmp_path}"/instance ]]
  [[ ! -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]
  rm -rf "${container_tmp_path}"

}

@test "test 'init' and 'destroy --quiet --cli' with existing dir" {
  #GIVEN
  tmp_path="${TESTRUN_DIR}"/init-destroy3
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/init-destroy3

  mkdir -p "${tmp_path}"

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=init --che-cli-extra-options="--skip:nightly --skip:pull 1>/dev/null"
  remove_named_container $CLI_CONTAINER

  #THEN
  [[ -d "${container_tmp_path}"/docs ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=destroy --che-cli-extra-options="--quiet --skip:nightly --skip:pull --cli 1>/dev/null"
  
  #THEN
  [[ ! -d "${container_tmp_path}"/docs ]]
  [[ ! -d "${container_tmp_path}"/instance ]]
  [[ ! -e "${container_tmp_path}"/che.env ]]
  [[ ! -e "${container_tmp_path}"/cli.log ]]
  rm -rf "${container_tmp_path}"

}

@test "test 'init' and 'destroy --quiet --cli' with non-existing dir" {

  #GIVEN
  tmp_path="${TESTRUN_DIR}"/init-destroy4
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/init-destroy4

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=init --che-cli-extra-options="--skip:nightly --skip:pull 1>/dev/null"

  #THEN
  [[ -d "${container_tmp_path}" ]]
  [[ -d "${container_tmp_path}"/docs ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=destroy --che-cli-extra-options="--skip:nightly --skip:pull --quiet --cli 1>/dev/null"

  #THEN
  [[ ! -d "${container_tmp_path}"/docs ]]
  [[ ! -d "${container_tmp_path}"/instance ]]
  [[ ! -e "${container_tmp_path}"/che.env ]]
  [[ ! -e "${container_tmp_path}"/cli.log ]]
  rm -rf "${container_tmp_path}"
}
