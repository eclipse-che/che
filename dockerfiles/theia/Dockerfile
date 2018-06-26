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
    HOME=/home/theia

RUN apk update && apk add --no-cache make gcc g++ python git openssh bash supervisor jq && \
    rm -rf /tmp/* /var/cache/apk/*

# Packages which will be included into default build of Theia
ADD theia-default-package.json ${HOME}/package.json

# Change version of Theia to specified in THEIA_VERSION
ADD src/versions.sh ${HOME}/versions.sh
RUN ${HOME}/versions.sh && rm ${HOME}/versions.sh

# Add extensions
ADD src/add-extensions.js ${HOME}
#RUN git clone https://github.com/eclipse/che-theia-hosted-plugin-manager-extension /tmp/hosted-plugin-extension && \
# ==TODO== replace with eclipse official version after release
RUN git clone https://github.com/mmorhun/che-theia-hosted-plugin-manager-extension /tmp/hosted-plugin-extension && \
    node ${HOME}/add-extensions.js \
        @eclipse-che/che-theia-hosted-plugin-manager-extension:file:///tmp/hosted-plugin-extension \
#       che-theia-ssh-extension:https://github.com/eclipse/che-theia-ssh-plugin.git \
    && rm ${HOME}/add-extensions.js

# Build Theia with all extensions
RUN cat ${HOME}/package.json && \
    cd ${HOME} && \
    yarn && \
    yarn theia build
    # ==TODO== add after generator-theia-plugin release
    # npm install -g yo @wiptheia/generator-theia-plugin
# Change permissions to allow editing of files for openshift user
RUN find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \;

EXPOSE 3000 3030
WORKDIR ${HOME}
ADD supervisord.conf /etc/
ADD src/start.sh ${HOME}/start.sh
RUN touch /var/log/supervisord.log && chmod g+rwX /var/log/supervisord.log && chgrp 0 /var/log/supervisord.log
ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
