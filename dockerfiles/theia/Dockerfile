# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

FROM node:8-alpine
# build dependencies requireed to compile a custom Theia
RUN apk add --no-cache make gcc g++ python git openssh bash rsync
WORKDIR /home/theia
# build Theia with all extensions to persist yarn cache in the image and
# have default Theia build in the workspace in case no plugins are requested
ADD https://raw.githubusercontent.com/theia-ide/theia-apps/master/theia-full-docker/latest.package.json /home/theia/package.json
ADD theia-default-package.json /home/default/theia/package.json
ADD src/main.js /theia_launcher/theia_launcher.js
RUN yarn && \
    yarn theia build && \
    rm -rf * && \
    cd /home/default/theia && \
    yarn && \
    yarn theia build
RUN ln -sf /home/default/theia/node_modules /home/theia/node_modules
RUN ln -sf /home/default/theia/yarn.lock /home/theia/yarn.lock
EXPOSE 3000
ARG GITHUB_TOKEN
ENV USE_LOCAL_GIT=true \
    GITHUB_TOKEN=${GITHUB_TOKEN}
ENTRYPOINT ["node", "/theia_launcher/theia_launcher.js"]
