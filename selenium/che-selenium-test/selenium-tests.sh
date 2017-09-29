#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#
export CUR_DIR=$(cd "$(dirname "$0")"; pwd)
export CALLER=$(basename $0)

cd $CUR_DIR

index=0
params=($*)
cheMultiuserValue=${CHE_MULTIUSER:-false}
for var in "$@"; do
   if [[ "$var" =~ --multiuser ]]; then
       cheMultiuserValue=true
       unset params[$index]                     # remove "--multiuser" from parameters
       break
   fi
   let "index+=1"
done

export CHE_MULTIUSER=$cheMultiuserValue

set -- "${params[@]}"


mvn dependency:unpack-dependencies \
    -DincludeArtifactIds=che-selenium-core \
    -DincludeGroupIds=org.eclipse.che.selenium \
    -Dmdep.unpack.includes=webdriver.sh \
    -DoutputDirectory=${CUR_DIR}/target/bin
chmod +x target/bin/webdriver.sh

TESTS_SCOPE="--suite=CheSuite.xml"
for var in "$@"; do
    if [[ "$var" =~ --test=.* ]] || [[ "$var" =~ --suite=.* ]]; then
        TESTS_SCOPE=
        break
    fi
done

(target/bin/webdriver.sh "$TESTS_SCOPE" $@)
