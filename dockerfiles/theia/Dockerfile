# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

###
# Builder Image
#
FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-theia-dev:${BUILD_TAG} as builder
WORKDIR ${HOME}

# define in env variable GITHUB_TOKEN only if it is defined
# else check if github rate limit is enough, else will abort requiring to set GITHUB_TOKEN value
ARG GITHUB_TOKEN

# Define upstream version of theia to use
ARG THEIA_VERSION=0.3.18

# Invalidate cache if there is a new commit
ADD https://api.github.com/repos/theia-ide/theia/git/refs/tags/v${THEIA_VERSION} /tmp/.sha1-repository

# Check github limit
RUN if [ ! -z "${GITHUB_TOKEN-}" ]; then \
      export GITHUB_TOKEN=$GITHUB_TOKEN; \
      echo "Setting GITHUB_TOKEN value as provided"; \
    else \
      export GITHUB_LIMIT=$(curl -s 'https://api.github.com/rate_limit' | jq '.rate .remaining'); \
      echo "Current API rate limit https://api.github.com is ${GITHUB_LIMIT}"; \
      if [ "${GITHUB_LIMIT}" -lt 10 ]; then \
        printf "\033[0;31m\n\n\nRate limit on https://api.github.com is reached so in order to build this image, "; \
        printf "the build argument GITHUB_TOKEN needs to be provided so build will not fail.\n\n\n\033[0m"; \
        exit 1; \
      else \
        echo "GITHUB_TOKEN variable is not set but https://api.github.com rate limit has enough slots"; \
      fi \
    fi

# Clone theia
RUN git clone --branch v${THEIA_VERSION}  --single-branch --depth 1 https://github.com/theia-ide/theia ${HOME}/theia-source-code

# Add patches
ADD src/patches ${HOME}/patches

# Apply patches
RUN if [ -d "${HOME}/patches/${THEIA_VERSION}" ]; then \
      echo "Applying patches for Theia version ${THEIA_VERSION}"; \
      for file in $(find "${HOME}/patches/${THEIA_VERSION}" -name '*.patch'); do \
        echo "Patching with ${file}"; \
        cd ${HOME}/theia-source-code && patch -p1 < ${file}; \
      done \
    fi

# Generate che-theia
WORKDIR ${HOME}/theia-source-code
RUN che:theia init

# Compile Theia
RUN yarn

# Run into production mode
RUN che:theia production

# change permissions
RUN find production -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \; 2>log.txt

###
# Runtime Image
#
# Use node image
FROM node:8.12.0-alpine as runtime
ENV USE_LOCAL_GIT=true \
    HOME=/home/theia \
    THEIA_DEFAULT_PLUGINS=local-dir:///default-theia-plugins \
    # Specify the directory of git (avoid to search at init of Theia)
    LOCAL_GIT_DIRECTORY=/usr \
    GIT_EXEC_PATH=/usr/libexec/git-core

EXPOSE 3100 3130

# Install sudo
# Install Java for java extension
# Install git
# Install bzip2 to unpack files
# Install which tool in order to search git
# Install curl and bash
RUN apk add --update --no-cache sudo openjdk8-jre git bzip2 which bash curl
RUN adduser --disabled-password -S -u 1001 -G root -h ${HOME} -s /bin/sh theia \
    && echo "%wheel ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers \
    # Create /projects for Che
    && mkdir /projects \
    # Create root node_modules in order to not use node_modules in each project folder
    && mkdir /node_modules \
    # Create internal plugins folder
    && mkdir /default-theia-plugins \
    # Download yeoman generator plug-in
    && curl -L -o /default-theia-plugins/theia_yeoman_plugin.theia https://github.com/eclipse/theia-yeoman-plugin/releases/download/untagged-04f28ee329e479cc465b/theia_yeoman_plugin.theia \
    && curl -L -o /default-theia-plugins/eclipse_che_theia_factory_plugin.theia https://github.com/eclipse/che-theia/releases/download/0.0.1/eclipse_che_theia_factory_plugin.theia \
    && curl -L -o /default-theia-plugins/eclipse_che_theia_containers_plugin.theia https://github.com/eclipse/che-theia/releases/download/0.0.1/eclipse_che_theia_containers_plugin.theia \
    && for f in "${HOME}" "/etc/passwd" "/etc/group /node_modules /default-theia-plugins /projects"; do\
           sudo chgrp -R 0 ${f} && \
           sudo chmod -R g+rwX ${f}; \
       done \
    && cat /etc/passwd | sed s#root:x.*#root:x:\${USER_ID}:\${GROUP_ID}::\${HOME}:/bin/bash#g > ${HOME}/passwd.template \
    && cat /etc/group | sed s#root:x:0:#root:x:0:0,\${USER_ID}:#g > ${HOME}/group.template \
    # Add yeoman, theia plugin generator and typescript (to have tsc/typescript working)
    && yarn global add yo @theia/generator-plugin@0.0.1-1540209403 typescript@2.9.2 \
    && mkdir -p ${HOME}/.config/insight-nodejs/ \
    && chmod -R 777 ${HOME}/.config/ \
    # Defines the root /node_modules as the folder to use by yarn
    && echo '"--*.modules-folder" "/node_modules"' > $HOME/.yarnrc \
    # Disable the statistics for yeoman
    && echo '{"optOut": true}' > $HOME/.config/insight-nodejs/insight-yo.json \
    # Link yarn global modules for yeoman
    && mv /usr/local/lib/node_modules/* /usr/local/share/.config/yarn/global/node_modules && rm -rf /usr/local/lib/node_modules && ln -s /usr/local/share/.config/yarn/global/node_modules /usr/local/lib/ \
    # Cleanup tmp folder
    && rm -rf /tmp/* \
    # Cleanup yarn cache
    && yarn cache clean \
    # Change permissions to allow editing of files for openshift user
    && find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \;

COPY --chown=theia:root --from=builder /home/theia-dev/theia-source-code/production /home/theia
USER theia
WORKDIR /projects
ADD src/entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
