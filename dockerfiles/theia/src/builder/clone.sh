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

# Clone specific tag of a Theia version
git clone --branch v${THEIA_VERSION} https://github.com/theia-ide/theia ${HOME}/theia-source-code

# Apply patches (if any)
if [ -d "/home/theia-build/patches/${THEIA_VERSION}" ]; then
    echo "Applying patches for Theia version ${THEIA_VERSION}";
    for file in $(find "/home/theia-build/patches/${THEIA_VERSION}" -name '*.patch'); do
        echo "Patching with ${file}";
        cd ${HOME}/theia-source-code && patch -p1 < ${file};
    done
fi
