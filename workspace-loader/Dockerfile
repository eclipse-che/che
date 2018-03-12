# Copyright (c) 2018-2018 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# This is a Dockerfile allowing to build workspace loader by using a docker container.
# Build step: $ docker build -t eclipse-che-workspace-loader .
# It builds an archive file that can be used by doing later
#  $ docker run --rm eclipse-che-workspace-loader | tar -C target/ -zxf -
FROM node:6.11.2

COPY package.json /workspace-loader/
RUN cd /workspace-loader && npm install

COPY . /workspace-loader/

RUN cd /workspace-loader && \
    npm run build && \
    npm run test && \
    cd target && \
    tar zcf /tmp/workspace-loader.tar.gz dist

CMD zcat /tmp/workspace-loader.tar.gz
