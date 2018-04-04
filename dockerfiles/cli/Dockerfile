# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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
