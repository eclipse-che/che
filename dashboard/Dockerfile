# Copyright (c) 2015-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

# This is a Dockerfile allowing to build dashboard by using a docker container.
# Build step: $ docker build -t eclipse-che-dashboard
# It builds an archive file that can be used by doing later
#  $ docker run --rm eclipse-che-dashboard | tar -C target/ -zxf -
FROM node:8.10.0

RUN apt-get update && \
    apt-get install -y git \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/*
COPY package.json /dashboard/
COPY yarn.lock /dashboard/
RUN cd /dashboard && npm i yarn && npx yarn install --ignore-scripts
COPY . /dashboard/
RUN cd /dashboard  && npx yarn
RUN cd /dashboard && cd target/ && tar zcf /tmp/dashboard.tar.gz dist/

CMD zcat /tmp/dashboard.tar.gz
