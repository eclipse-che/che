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

echo ******************
ls ${HOME}/extensions
echo ******************

# Build Theia with all the extensions
cd ${HOME}
echo "YARN"
yarn
echo "YARN BUILD"
yarn theia build --mode development
