#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

source /scripts/base/startup_funcs.sh

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

trap "cleanup" INT TERM EXIT