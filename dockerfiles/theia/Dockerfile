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
ARG THEIA_VERSION=next

ENV GITHUB_TOKEN=${GITHUB_TOKEN} \
    THEIA_VERSION=${THEIA_VERSION} \
    USE_LOCAL_GIT=true \
    YARN_CACHE_FOLDER=/usr/local/share/.cache/yarn

RUN apk update && apk add --no-cache make gcc g++ python git openssh bash supervisor jq && \
    rm -rf /tmp/* /var/cache/apk/*

# Add the upstream package.json
# Packages to be cached in the image
# ==TODO== after release replace with release depending link, e.g. https://raw.githubusercontent.com/theia-ide/theia/v${THEIA_VERSION}/examples/browser/package.json
ADD https://raw.githubusercontent.com/theia-ide/theia-apps/master/theia-docker/next.package.json /home/theia/package.json
# Subset of packages above which will be included into default build of Theia
ADD theia-default-package.json /home/default/theia/package.json

# Change version of Theia to specified in ARG
ADD versions.sh /home/default/versions.sh
RUN /home/default/versions.sh && rm /home/default/versions.sh

# Add extensions
ADD src/add-extensions.js /home/default
#RUN git clone https://github.com/eclipse/che-theia-hosted-plugin-manager-extension /tmp/hosted-plugin-extension && \
# ==TODO== replace with eclipse official version after release
RUN git clone https://github.com/mmorhun/che-theia-hosted-plugin-manager-extension /tmp/hosted-plugin-extension && \
    node /home/default/add-extensions.js \
        @eclipse-che/che-theia-hosted-plugin-manager-extension:file:///tmp/hosted-plugin-extension \
#       che-theia-ssh-extension:https://github.com/eclipse/che-theia-ssh-plugin.git \
    && rm /home/default/add-extensions.js

# Build Theia with all extensions to persist yarn cache in the image and
# have default Theia build in the workspace in case no plugins are requested
RUN cat /home/theia/package.json && \
    cd /home/theia && \
    yarn && \
    yarn theia build && \
    rm -rf * && \
    cd /home/default/theia && \
    yarn && \
    yarn theia build
    # ==TODO== add after generator-theia-plugin release
    # npm install -g yo @wiptheia/generator-theia-plugin
RUN find ${YARN_CACHE_FOLDER} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \;

EXPOSE 3000 3030
ADD src/main.js /theia_launcher/theia_launcher.js
ADD supervisord.conf /etc/
ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
WORKDIR /home/theia
ENV HOME=/home/theia
