# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# This is a Dockerfile allowing to build dashboard by using a docker container.
# Build step: $ docker build -t eclipse-che-dashboard
# It builds an archive file that can be used by doing later
#  $ docker run --rm eclipse-che-dashboard | tar -C target/ -zxf -
FROM node:6.11.2

RUN apt-get update && \
    apt-get install -y git \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/*
COPY package.json /dashboard/
RUN cd /dashboard && npm install
COPY bower.json /dashboard/
RUN cd /dashboard && ./node_modules/.bin/bower install --allow-root
COPY . /dashboard/
RUN cd /dashboard && npm run build && cd target/ && tar zcf /tmp/dashboard.tar.gz dist/

CMD zcat /tmp/dashboard.tar.gz
