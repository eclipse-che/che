#!/bin/sh
#
# Copyright (c) 2018-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

set -e
set -u

# Start npmjs repository
cd ${HOME}/verdaccio
verdaccio &
sleep 3

# using 0.4 there to bump major version so we're sure to not download any 0.3.x dependencies
# Set the version of Theia
export THEIA_VERSION=0.4.1-che

# Change version of Theia to specified in THEIA_VERSION
cd ${HOME} && /home/theia-build/versions.sh

# Apply resolution section to the Theia package.json to use strict versions for Theia dependencies
node /home/theia-build/resolutions-provider.js ${HOME}/package.json

# avoid issue with checksum of electron
cd ${HOME} && npm install electron-packager -g

# Add default Theia extensions
cd ${HOME} && node /home/theia-build/add-extensions.js

