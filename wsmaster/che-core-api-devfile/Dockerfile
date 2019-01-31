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
RUN git clone https://github.com/adobe/jsonschema2md.git
RUN cd jsonschema2md && npm install && npm link
RUN cd ..


COPY ./src/main/resources/schema /schema
RUN cd /schema && jsonschema2md -d . -e json -n
RUN cd /schema/out && mv devfile.md index.md
RUN cd /schema && tar zcf /tmp/out.tar.gz out/

CMD zcat /tmp/out.tar.gz