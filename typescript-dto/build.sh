#!/usr/bin/env sh
#
# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# See: https://sipb.mit.edu/doc/safe-shell/

set -e
set -u

rm -f ./index.d.ts

set +e
docker run -i --rm -v "$HOME/.m2:/root/.m2" \
                   -v "$(pwd)/dto-pom.xml:/usr/src/mymaven/pom.xml" \
                   -w /usr/src/mymaven maven:3.6.1-jdk-11 \
                    /bin/bash -c "mvn -q -U -DskipTests=true -Dfindbugs.skip=true -Dskip-validate-sources install \
                    && cat target/dts-dto-typescript.d.ts" >> index.d.ts

set -
# validate that index.d.ts has kind of valid output
if ! grep "export namespace" index.d.ts; then
  echo "Invalid output generated for index.d.ts"
  exit 1
fi

CHE_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args="\${project.version}" --non-recursive exec:exec -f ../pom.xml)

docker build -t eclipse-che-ts-api --build-arg CHE_VERSION="${CHE_VERSION}" --build-arg NPM_AUTH_TOKEN="${CHE_NPM_AUTH_TOKEN}" .
