# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

FROM node:8-alpine

ARG GITHUB_TOKEN
ARG THEIA_VERSION
ENV USE_LOCAL_GIT=true \
    GITHUB_TOKEN=${GITHUB_TOKEN}

RUN if [ -z "${THEIA_VERSION}" ]; then echo -e '\033[0;31m' Set up build argument 'THEIA_VERSION' '\033[0m'; exit 1; fi

# build dependencies requireed to compile a custom Theia
RUN apk add --no-cache make gcc g++ python git openssh bash supervisor jq
WORKDIR /home/theia
# build Theia with all extensions to persist yarn cache in the image and
# have default Theia build in the workspace in case no plugins are requested
ADD https://raw.githubusercontent.com/theia-ide/theia/v${THEIA_VERSION}/examples/browser/package.json /home/theia/package.json
ADD theia-default-package.json /home/default/theia/package.json
ADD versions.sh /home/default/versions.sh
RUN cd /home/default && ./versions.sh

ADD supervisord.conf /etc/
RUN cd /home/theia && \
    yarn && \
    yarn theia build --mode development && \
    rm -rf * && \
    cd /home/default/theia && \
    yarn && \
    yarn theia build
ADD src/main.js /theia_launcher/theia_launcher.js
EXPOSE 3000

ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
