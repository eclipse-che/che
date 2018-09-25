# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# build:
#   docker build -t eclipse/che-action .
#
# use:
#    docker run -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action [command]

FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-lib:${BUILD_TAG}

ENTRYPOINT ["node", "/che-lib/index.js", "che-action"]
