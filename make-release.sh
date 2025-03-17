#!/bin/bash
# Copyright (c) 2021-2023 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#


VERSION=7.9999
BRANCH_NAME=pr-test-prettier
git checkout -b $BRANCH_NAME
cd tests/e2e
# update devworkspace generator version
jq ".\"dependencies\".\"@eclipse-che/che-devworkspace-generator\" = \"${VERSION}\"" package.json > package.json.update
mv package.json.update package.json
npm i -g prettier
npm --no-git-tag-version version --allow-same-version "${VERSION}"
npm run prettier
npm -v prettier

COMMIT_MSG="[DO NOT MERGE] test npm prettier"
git commit -asm "${COMMIT_MSG}"

git push origin $BRANCH_NAME
