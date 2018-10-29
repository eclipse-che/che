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

# Kill running che server instance if there is any to be able to run tests
setup() {
  kill_running_named_container che
  remove_named_container che
}

teardown() {
  kill_running_named_container che
  remove_named_container che
}

@test "test cli 'backup' command: backup fail if che is running" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_backup_fail_if_che_is_running
  mkdir -p "${tmp_path}"
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state
  #WHEN
  result=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=backup --che-cli-extra-options="--skip:nightly --skip:pull" || true)

  #THEN
  [[ $result == *"che is running. Stop before performing a backup."* ]]
}

@test "test cli 'restore' command: restore fail if che is running" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_restore_fail_if_che_is_running
  mkdir -p "${tmp_path}"
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state
  #WHEN
  result=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=restore --che-cli-extra-options="--quiet --skip:nightly --skip:pull")

  #THEN
  [[ $result == *"Eclipse Che is running. Stop before performing a restore."* ]]
}

@test "test cli 'restore' command: restore fail if no backup found" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_restore_fail_if_no_backup_found
  mkdir -p "${tmp_path}"
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=stop --che-cli-extra-options="--skip:nightly --skip:pull"

  #WHEN
  result=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=restore --che-cli-extra-options="--quiet --skip:nightly --skip:pull")

  #THEN
  [[ $result == *"Backup files not found. To do restore please do backup first."* ]]
}

@test "test cli 'backup / restore' commands" {
  # TEST BACKUP
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_backup_do_backup_restore
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/cli_cmd_backup_do_backup_restore
  workspace_name="backup-restore"
  mkdir -p "${tmp_path}"
  #start che
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state
  #create a workspace

  ws_create=$(curl 'http://'${ip_address}':8080/api/workspace?namespace=che&attribute=stackId:java-default' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"defaultEnv":"wksp-1p0b","environments":{"wksp-1p0b":{"recipe":{"location":"eclipse/ubuntu_jdk8","type":"dockerimage"},"machines":{"dev-machine":{"servers":{},"installers":["org.eclipse.che.exec","org.eclipse.che.terminal","org.eclipse.che.ws-agent","org.eclipse.che.ssh"],"attributes":{"memoryLimitBytes":"2147483648"}}}}},"projects":[],"commands":[{"commandLine":"mvn clean install -f ${current.project.path}","name":"build","type":"mvn","attributes":{"goal":"Build","previewUrl":""}}],"name":"backup-restore","links":[]}' --compressed)
  [[ "$ws_create" == *"created"* ]]
  [[ "$ws_create" == *"STOPPED"* ]]
  #stop che
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=stop --che-cli-extra-options="--skip:nightly --skip:pull"

  #WHEN
  backup=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=backup --che-cli-extra-options="--skip:nightly --skip:pull")

  #THEN
  [[ "$backup" == *"Saving Eclipse Che data..."* ]]
  [[ "$backup" == *"che data saved in ${tmp_path}/backup/che_backup.tar.gz"* ]]
  [[ -f "${container_tmp_path}"/backup/che_backup.tar.gz ]]

  # TEST RESTORE
  #GIVEN
  #destroy to wipe data
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=destroy --che-cli-extra-options="--quiet --skip:nightly --skip:pull"
  [[ ! -d "${container_tmp_path}"/instance ]]
  #WHEN
  #perform restore from backup
  restore=$(execute_cli_command --che-data-path=${tmp_path} --che-cli-command=restore --che-cli-extra-options="--quiet --skip:nightly --skip:pull")

  #THEN
  [[ "$restore" == *"Recovering Eclipse Che data..."* ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -d "${container_tmp_path}"/instance/data ]]

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=start --che-cli-extra-options="--skip:nightly --skip:pull"
  check_che_state

  #THEN
  [[ "$(curl -fsS http://${ip_address}:8080/api/workspace)" == *"$workspace_name"* ]]
}
