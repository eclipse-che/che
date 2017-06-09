#!/usr/bin/env bats
# Copyright (c) 2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE stop --skip:nightly --skip:pull
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE destroy --quiet --skip:nightly --skip:pull
}

@test "test cli 'backup' command: backup fail if che is running" {
  #GIVEN
  if [ ! port_is_free 8080 ]; then
    [ "$status" -eq 1 ]
    [ "$output" = "Default port 8080 for che server is used. Cannot run this test on default che server port" ]
  fi
  tmp_path="${TESTRUN_DIR}"/cli_cmd_backup_fail_if_che_is_running
  mkdir -p "${tmp_path}"
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' che)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:8080  > /dev/null

  #WHEN
  result="$(docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE backup --skip:nightly --skip:pull || true)"

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
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' che)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:8080  > /dev/null

  #WHEN
  result="$(docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE restore --quiet --skip:nightly --skip:pull)"

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
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' che)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:8080  > /dev/null
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE stop --skip:nightly --skip:pull

  #WHEN
  result="$(docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE restore --quiet --skip:nightly --skip:pull)"

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
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' che)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:8080  > /dev/null
  #create a workspace

  ws_create=$(curl 'http://'${ip_address}':8080/api/workspace?namespace=che&attribute=stackId:java-default' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"defaultEnv":"wksp-1p0b","environments":{"wksp-1p0b":{"recipe":{"location":"eclipse/ubuntu_jdk8","type":"dockerimage"},"machines":{"dev-machine":{"servers":{},"agents":["org.eclipse.che.exec","org.eclipse.che.terminal","org.eclipse.che.ws-agent","org.eclipse.che.ssh"],"attributes":{"memoryLimitBytes":"2147483648"}}}}},"projects":[],"commands":[{"commandLine":"mvn clean install -f ${current.project.path}","name":"build","type":"mvn","attributes":{"goal":"Build","previewUrl":""}}],"name":"backup-restore","links":[]}' --compressed)
  [[ "$ws_create" == *"created"* ]]
  [[ "$ws_create" == *"STOPPED"* ]]
  #stop che
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE stop --skip:nightly --skip:pull

  #WHEN
  backup=$(docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE backup --skip:nightly --skip:pull)

  #THEN
  [[ "$backup" == *"Saving codenvy data..."* ]]
  [[ "$backup" == *"che data saved in ${tmp_path}/backup/che_backup.tar.gz"* ]]
  [[ -f "${container_tmp_path}"/backup/che_backup.tar.gz ]]

  # TEST RESTORE
  #GIVEN
  #destroy to wipe data
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE destroy --quiet --skip:nightly --skip:pull
  [[ ! -d "${container_tmp_path}"/instance ]]
  #WHEN
  #perform restore from backup
  restore=$(docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE restore --quiet --skip:nightly --skip:pull)

  #THEN
  [[ "$restore" == *"Recovering Eclipse Che data..."* ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -d "${container_tmp_path}"/instance/data ]]

  #WHEN
  docker run --rm -v "${SCRIPTS_DIR}":/scripts/base -v /var/run/docker.sock:/var/run/docker.sock -v "${tmp_path}":/data $CLI_IMAGE start --skip:nightly --skip:pull
  [[ "$(docker inspect --format='{{.State.Running}}' che)" == "true" ]]
  ip_address=$(docker inspect -f {{.NetworkSettings.Networks.bridge.IPAddress}} che)
  curl -fsS http://${ip_address}:8080  > /dev/null

  #THEN
  [[ "$(curl -fsS http://${ip_address}:8080/api/workspace)" == *"$workspace_name"* ]]
}
