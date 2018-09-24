# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# build:
#   docker build -t eclipse/che-base .
#
# use:
#    docker run eclipse/che-base

FROM alpine:3.4

ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.11.2
ENV DOCKER_SHA256 8c2e0c35e3cda11706f54b2d46c2521a6e9026a7b13c7d4b8ae1f3a706fc55e1

# install packages
# coreutils is required for iso8601 compliant date utility
RUN mkdir -p /version \
    && mkdir -p /cli \
    && mkdir /scripts/ \
    && apk add --no-cache ca-certificates coreutils curl openssl jq \
    && apk add --update bash \
    && rm -rf /var/cache/apk/* \
    && set -x \
    && curl -fSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-${DOCKER_VERSION}.tgz" -o docker.tgz \
    && echo "${DOCKER_SHA256} *docker.tgz" | sha256sum -c - \
    && tar -xzvf docker.tgz \
    && mv docker/docker /usr/local/bin/ \
    && rm -rf docker \
    && rm docker.tgz \
    && docker -v

COPY scripts/base /scripts/base/
COPY scripts/entrypoint.sh /scripts/entrypoint.sh

RUN chmod u+x /scripts/entrypoint.sh
