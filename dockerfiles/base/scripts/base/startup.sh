#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

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
