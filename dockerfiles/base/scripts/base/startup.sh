#!/bin/sh
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Check pre/post functions are there or not
declare -f pre_init > /dev/null
if [ "$?" == "1" ]; then
  pre_init() {
    :
  }
fi

declare -f post_init > /dev/null
if [ "$?" == "1" ]; then
  post_init() {
    :
  }
fi

source /scripts/base/startup_01_init.sh

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

trap "cleanup" INT TERM EXIT
