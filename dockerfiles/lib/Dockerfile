# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# build:
#   docker build -t eclipse/che-lib .
#
# use:
#    docker run -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-lib [command]

FROM mhart/alpine-node:6

ENV DOCKER_BUCKET=get.docker.com \
    DOCKER_VERSION=1.6.0

RUN set -x \
        && apk add --no-cache \
        ca-certificates \
        curl \
        openssl \
        && curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
        > /usr/bin/docker; chmod +x /usr/bin/docker \
        && apk del curl ca-certificates openssl

COPY runtime-dependencies/package.json /runtime/
COPY . /lib-typescript/

RUN cd /lib-typescript/ && npm install && npm test \
    && cd /runtime && npm install && /lib-typescript/node_modules/.bin/tsc --project /lib-typescript/ \
    && mv /lib-typescript/lib /che-lib && cd /lib-typescript/src && find . -name "*.properties" -exec install -D {} /che-lib/{} \;\
    && rm -rf /lib-typescript && mv /runtime/node_modules /che-lib && rm -rf /runtime
