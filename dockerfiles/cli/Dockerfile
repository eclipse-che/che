# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# build:
#   docker build -t eclipse/che-cli .
#
# use:
#    docker run -v $(pwd):/che eclipse/che-cli [command]
FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-base:${BUILD_TAG}

COPY scripts /scripts/
COPY version /version/

RUN mkdir /che
ENTRYPOINT ["/scripts/entrypoint.sh"]
