#!/bin/sh
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

base_dir=$(cd "$(dirname "$0")"; pwd)
. "${base_dir}/../../build.include"

init --name:theia-e2e "$@"

DOCKER_RUN_OPTIONS=""
# run bats with terminal mode (pretty print) if supported by current shell
if [ -t 1 ]; then
  DOCKER_RUN_OPTIONS="-t"
fi

# Runs E2E tests in a docker container.
run_test_in_docker_container() {
  docker_exec run --rm ${DOCKER_RUN_OPTIONS} \
       -v "${base_dir}/videos":/root/cypress/videos \
       -v "${base_dir}/logs":/root/logs \
       -v /var/run/docker.sock:/var/run/docker.sock \
           $IMAGE_NAME
}

run_test_in_docker_container
