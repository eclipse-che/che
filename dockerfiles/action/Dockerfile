# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t eclipse/che-action .
#
# use:
#    docker run -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-action [command]

FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-lib:${BUILD_TAG}

ENTRYPOINT ["node", "/che-lib/index.js", "che-action"]
