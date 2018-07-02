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
ARG THEIA_VERSION=0.4.0-next.b17727c1

EXPOSE 3000 3030

ENV GITHUB_TOKEN=${GITHUB_TOKEN} \
    THEIA_VERSION=${THEIA_VERSION} \
    USE_LOCAL_GIT=true \
    HOME=/home/theia

WORKDIR ${HOME}

ADD src/ ${HOME}/
ADD supervisord.conf /etc/

RUN \
    # Install needed software
    apk update && apk add --no-cache make gcc g++ python git bash supervisor jq && \
    rm -rf /tmp/* /var/cache/apk/* && \
    # Change version of Theia to specified in THEIA_VERSION
    ${HOME}/versions.sh && rm ${HOME}/versions.sh && \
    # Add Theia extensions
    #==TODO== replace with master branch after Theia release
    git clone --depth=1 -b latest-deps https://github.com/eclipse/che-theia-hosted-plugin-manager-extension /tmp/hosted-plugin-extension && \
    node ${HOME}/add-extensions.js \
        @eclipse-che/che-theia-hosted-plugin-manager-extension:file:///tmp/hosted-plugin-extension \
        #che-theia-ssh-extension:https://github.com/eclipse/che-theia-ssh-plugin.git \
    && rm ${HOME}/add-extensions.js && \
    # Build Theia with all the extensions
    cd ${HOME} && \
    cat package.json && \
    yarn && \
    yarn theia build && \
    # Install Theia plugin generator
    #==TODO== add after generator-theia-plugin release
    #npm install -g yo @wiptheia/generator-theia-plugin && \
    # Change permissions to allow editing of files for openshift user
    find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \; && \
    # Grant permissions for modifying supervisor log file
    touch /var/log/supervisord.log && chmod g+rwX /var/log/supervisord.log && chgrp 0 /var/log/supervisord.log

ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
