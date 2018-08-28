#!/usr/bin/env bats
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Roman Iuvshyn

source /dockerfiles/cli/tests/test_base.sh

@test "test cli 'offline' command: with default parameters" {
  #GIVEN
  tmp_path="${TESTRUN_DIR}"/cli_cmd_offline_with_default_parameters
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/cli_cmd_offline_with_default_parameters
  mkdir -p "${tmp_path}"

  #WHEN
  result=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=offline --che-cli-extra-options="--skip:nightly --skip:pull")

  #THEN
  [[ $result == *"Saving che cli image..."* ]]
  [[ $result == *"Saving che bootstrap images..."* ]]
  [[ $result == *"Saving che system images..."* ]]
  [[ $result == *"Saving utility images..."* ]]
  [[ $result == *"Saving che stack images..."* ]]

  [[ -f $(ls "${container_tmp_path}"/backup/alpine*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/docker_compose*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-action*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-cli*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-dir*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-init*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-ip*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-mount*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-test*.tar) ]]
}

@test "test cli 'offline' command: include custom stack images" {
  #GIVEN
  tmp_path="${TESTRUN_DIR}"/cli_cmd_offline_with_custom_stack_images
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/cli_cmd_offline_with_custom_stack_images
  mkdir -p "${tmp_path}"

  #WHEN
  result=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=offline --che-cli-extra-options="--image:eclipse/alpine_jdk8 --image:eclipse/debian_jre --skip:nightly --skip:pull")

  #THEN
  [[ $result == *"Saving che cli image..."* ]]
  [[ $result == *"Saving che bootstrap images..."* ]]
  [[ $result == *"Saving che system images..."* ]]
  [[ $result == *"Saving utility images..."* ]]
  [[ $result == *"Saving che stack images..."* ]]

  [[ -f $(ls "${container_tmp_path}"/backup/alpine*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/docker_compose*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-action*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-cli*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-dir*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-init*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-ip*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-mount*.tar) ]]
  [[ -f $(ls "${container_tmp_path}"/backup/eclipse_che-test*.tar) ]]
  [[ -f "${container_tmp_path}"/backup/eclipse_alpine_jdk8.tar ]]
  [[ -f "${container_tmp_path}"/backup/eclipse_debian_jre.tar ]]
}
