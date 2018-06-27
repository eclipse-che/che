#!/bin/bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

if [ -z "$THEIA_PORT" ]; then
    export THEIA_PORT=3000
fi

yarn theia start /projects --hostname=0.0.0.0 --port=${THEIA_PORT}
