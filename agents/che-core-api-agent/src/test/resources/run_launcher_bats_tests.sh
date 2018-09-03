#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

#images=(bitnami/che-codeigniter:3.1.3-r6 bitnami/che-express:4.15.3-r2 bitnami/che-java-play:1.3.12-r3 bitnami/che-laravel:5.4.23-r1 bitnami/che-rails:5.1.2-r0 bitnami/che-swift:3.1.1-r0 bitnami/che-symfony:3.3.2-r0 eclipse/centos_jdk8 eclipse/cpp_gcc eclipse/debian_jdk8 eclipse/debian_jre eclipse/dotnet_core eclipse/hadoop-dev eclipse/kotlin eclipse/node eclipse/php eclipse/php:5.6 eclipse/php:gae eclipse/selenium eclipse/ubuntu_android eclipse/ubuntu_go eclipse/ubuntu_jdk8 eclipse/ubuntu_jre eclipse/ubuntu_python:2.7 eclipse/ubuntu_python:gae_python2.7 eclipse/ubuntu_python:latest eclipse/ubuntu_rails kaloyanraev/che-zendserver registry.centos.org/che-stacks/centos-go registry.centos.org/che-stacks/centos-nodejs registry.centos.org/che-stacks/spring-boot registry.centos.org/che-stacks/vertx registry.centos.org/che-stacks/wildfly-swarm tomitribe/ubuntu_tomee_173_jdk8 registry.centos.org/che-stacks/centos-git)
#arbitrary_images=(rhche/centos_jdk8 rhche/vertx rhche/ubuntu_jdk8 rhche/centos-nodejs rhche/spring-boot rhche/wildfly-swarm)
images=(eclipse/centos_jdk8)
arbitrary_images=(rhche/centos_jdk8)

run_bats_test() {
  export CHE_BASE_DIR=$(pwd)
  export BATS_TEST_SCRIPT=${1}
  export LAUNCHER_SCRIPT_TO_TEST=${2}
  export DOCKER_IMAGE=${3}
  docker run -ti --rm -e CHE_BASE_DIR -e LAUNCHER_SCRIPT_TO_TEST -e DOCKER_IMAGE \
       -v ${CHE_BASE_DIR}/${BATS_TEST_SCRIPT}:/scripts/launcher_tests.bats \
       -v ${CHE_BASE_DIR}/dockerfiles:/dockerfiles \
       -v /var/run/docker.sock:/var/run/docker.sock \
       eclipse/che-bats bats /scripts/launcher_tests.bats
}

for image in "${images[@]}"; do
   launcher_script_to_test="wsagent/agent/src/main/resources/org.eclipse.che.ws-agent.script.sh"
   bats_test_script="agents/che-core-api-agent/src/test/resources/agents-launchers-tests.bats"
   echo "RUNNING LAUNCHER BATS TESTS FOR IMAGE ${image}"
   run_bats_test "${bats_test_script}" "${launcher_script_to_test}" "${image}"
done

for arbitrary_image in "${arbitrary_images[@]}"; do
   launcher_script_to_test="wsagent/agent/src/main/resources/org.eclipse.che.ws-agent.script.sh"
   bats_test_script="agents/che-core-api-agent/src/test/resources/agents-launchers-tests-arbitraryuser.bats"
   echo "RUNNING LAUNCHER BATS TESTS FOR IMAGE ${arbitrary_image}"
   run_bats_test "${bats_test_script}" "${launcher_script_to_test}"  "${arbitrary_image}"
done
