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
if [ -d "${HOME}/patches/${THEIA_VERSION}" ]; then
    echo "Applying patches for Theia version ${THEIA_VERSION}";
    for file in $(find "${HOME}/patches/${THEIA_VERSION}" -name '*.patch'); do
        echo "Patching with ${file}";
        cd ${HOME}/theia-source-code && patch -p1 < ${file};
    done
fi

# Compile Theia
cd ${HOME}/theia-source-code && yarn

# add registry and start it
npm install -g verdaccio
mkdir ${HOME}/verdaccio
cd ${HOME}/verdaccio
verdaccio &
sleep 3

# Update registry URL to local one
cd ${HOME}
yarn config set registry http://localhost:4873
npm config set registry http://localhost:4873


# Create user for local registry
export USERNAME=theia
export PASSWORD=theia
export EMAIL=che-theia@eclipse.org

/usr/bin/expect <<EOD
spawn npm adduser --registry http://localhost:4873
expect {
  "Username:" {send "$USERNAME\r"; exp_continue}
  "Password:" {send "$PASSWORD\r"; exp_continue}
  "Email: (this IS public)" {send "$EMAIL\r"; exp_continue}
}
EOD

# Now go to source code of theia and publish it
cd ${HOME}/theia-source-code

# using 0.4 there to bump major version so we're sure to not download any 0.3.x dependencies
# Set the version of Theia
export THEIA_VERSION=0.4.1-che

./node_modules/.bin/lerna publish --registry=http://localhost:4873 --exact --repo-version=${THEIA_VERSION} --skip-git --force-publish --npm-tag=latest  --yes
cd ${HOME}

# Code has been published, let's delete it
rm -rf ${HOME}/theia-source-code
