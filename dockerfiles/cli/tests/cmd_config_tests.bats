#!/usr/bin/env bats
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Roman Iuvshyn

load '/bats-support/load.bash'
load '/bats-assert/load.bash'
source /dockerfiles/cli/tests/test_base.sh

@test "test CLI 'config' command" {
  #GIVEN
  tmp_path="${TESTRUN_DIR}"/cli_cmd_config
  container_tmp_path="${CONTAINER_TESTRUN_DIR}"/cli_cmd_config

  #WHEN
  execute_cli_command --che-data-path=${tmp_path} --che-cli-command=config --che-cli-extra-options="--skip:nightly --skip:pull"

  #THEN
  [[ -d "${container_tmp_path}"/docs ]]
  [[ -e "${container_tmp_path}"/che.env ]]
  [[ -e "${container_tmp_path}"/cli.log ]]
  [[ -d "${container_tmp_path}"/instance ]]
  [[ -d "${container_tmp_path}"/instance/config ]]
  [[ -d "${container_tmp_path}"/instance/config/che ]]
  [[ -f "${container_tmp_path}"/instance/config/che/che.env ]]
  [[ -d "${container_tmp_path}"/instance/config/keycloak/che ]]
  [[ -f "${container_tmp_path}"/instance/config/keycloak/che-realm.json ]]
  [[ -f "${container_tmp_path}"/instance/config/postgres/init-che-user.sh ]]
  [[ -f "${container_tmp_path}"/instance/config/traefik/traefik.toml ]]
  [[ -d "${container_tmp_path}"/instance/data ]]
  [[ -d "${container_tmp_path}"/instance/logs ]]
  [[ -d "${container_tmp_path}"/instance/templates ]]
  [[ -f "${container_tmp_path}"/instance/docker-compose-container.yml ]]
  [[ -f "${container_tmp_path}"/instance/che.ver.do_not_modify ]]
}
