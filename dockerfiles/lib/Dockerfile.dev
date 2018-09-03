# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc.- Initial implementation
#

FROM mhart/alpine-node:6

ENV DOCKER_BUCKET=get.docker.com \
    DOCKER_VERSION=1.6.0

COPY package.json /compile/
COPY runtime-dependencies/package.json /runtime/
RUN set -x \
        && apk add --no-cache \
        ca-certificates \
        curl \
        openssl \
        && curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
        > /usr/bin/docker; chmod +x /usr/bin/docker \
        && apk del curl ca-certificates openssl \
        && cd /compile && npm install && cd /runtime && npm install \
        && mkdir /lib-typescript && mv /compile/node_modules /lib-typescript/
COPY . /lib-typescript/
RUN /lib-typescript/node_modules/.bin/tsc --project /lib-typescript/ && mv /lib-typescript/lib /che-lib \
    && cd /lib-typescript/src && find . -name "*.properties" -exec install -D {} /che-lib/{} \;\
    && rm -rf /lib-typescript && mv /runtime/node_modules /che-lib && rm -rf /runtime && rm -rf /compile
