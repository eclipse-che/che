#!/bin/bash
#
# Copyright (c) 2012-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#
export CUR_DIR=$(cd "$(dirname "$0")"; pwd)
export CALLER=$(basename $0)

cd $CUR_DIR

TESTS_SCOPE="--suite=CheSuite.xml"
CLEAN_GOAL="clean"
for var in "$@"; do
    if [[ "$var" =~ --test=.* ]] || [[ "$var" =~ --suite=.* ]]; then
        TESTS_SCOPE=
        break
    fi

    if [[ "$var" == "--compare-with-ci" ]] \
        || [[ "$var" == "--failed-tests" ]] \
        || [[ "$var" == "--regression-tests" ]]; then
        TESTS_SCOPE=
        CLEAN_GOAL=
        break
    fi
done

mvn $CLEAN_GOAL dependency:unpack-dependencies \
    -DincludeArtifactIds=che-selenium-core \
    -DincludeGroupIds=org.eclipse.che.selenium \
    -Dmdep.unpack.includes=webdriver.sh \
    -DoutputDirectory=${CUR_DIR}/target/bin
chmod +x target/bin/webdriver.sh

(target/bin/webdriver.sh "$TESTS_SCOPE" $@)
