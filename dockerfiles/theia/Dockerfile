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
FROM centos as builder

ARG GITHUB_TOKEN

# Install basic software used for checking github API rate limit
RUN yum install -y epel-release && yum -y install curl jq expect

# define in env variable GITHUB_TOKEN only if it is defined
# else check if github rate limit is enough, else will abort requiring to set GITHUB_TOKEN value
ADD src/builder/check-github-limit.sh /tmp/builder/scripts/check-github-limit.sh
RUN /tmp/builder/scripts/check-github-limit.sh

ADD src/builder/setup-nodejs.sh /tmp/builder/scripts/setup-nodejs.sh
RUN /tmp/builder/scripts/setup-nodejs.sh

# Install tools required to build theia
RUN yum install -y gcc-c++ make python git

# Define upstream version of theia to use
ARG THEIA_VERSION=plugin-id

ENV THEIA_VERSION=${THEIA_VERSION} \
    HOME=/home/theia
WORKDIR ${HOME}

# Add verdaccio config
ADD src/config.yaml ${HOME}/.config/verdaccio/

# Add patches
ADD src/patches /home/theia-build/patches

# Build theia from source code and publish artifacts locally
ADD src/builder/build-and-publish-theia.sh /tmp/builder/scripts/build-and-publish-theia.sh
RUN /tmp/builder/scripts/build-and-publish-theia.sh

# Now, add the extensions
ADD src/add-extensions.js /home/theia-build/add-extensions.js
ADD src/versions.sh /home/theia-build/versions.sh
ADD src/resolutions-provider.js /home/theia-build/resolutions-provider.js

ADD src/extensions.json ${HOME}/extensions.json
ADD src/package.json ${HOME}/package.json
ADD src/builder/add-extensions.sh /tmp/builder/scripts/add-extensions.sh
RUN /tmp/builder/scripts/add-extensions.sh

# Assemble theia
ADD src/builder/assemble-theia.sh /tmp/builder/scripts/assemble-theia.sh
RUN /tmp/builder/scripts/assemble-theia.sh

# Clean
RUN yarn cache clean

# Restore registry setting
RUN npm config set registry https://registry.npmjs.org && yarn config set registry https://registry.npmjs.org

# change permissions
RUN find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \;

###
# Runtime Image
#
FROM centos
ENV USE_LOCAL_GIT=true \
    HOME=/home/theia

EXPOSE 3000 3030

ADD src/builder/setup-nodejs.sh /tmp/builder/scripts/setup-nodejs.sh

# Install sudo
# Install Java for java extension
# Install git (from software collection in order to have a recent version)
# Install bzip2 to unpack files
# Install supervisor for running theia
RUN yum install -y centos-release-scl
RUN yum install -y rh-git29 scl-utils sudo java-1.8.0-openjdk-devel bzip2 && \
    /tmp/builder/scripts/setup-nodejs.sh

RUN useradd -u 1000 -G users,wheel,root -d ${HOME} --shell /bin/bash theia \
    && usermod -p "*" theia \
    && echo "%wheel ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers \
    && for f in "${HOME}" "/etc/passwd" "/etc/group"; do\
           sudo chgrp -R 0 ${f} && \
           sudo chmod -R g+rwX ${f}; \
       done \
    && cat /etc/passwd | sed s#root:x.*#root:x:\${USER_ID}:\${GROUP_ID}::\${HOME}:/bin/bash#g > ${HOME}/passwd.template \
    && cat /etc/group | sed s#root:x:0:#root:x:0:0,\${USER_ID}:#g > ${HOME}/group.template \
    && npm install -g yo @theia/generator-plugin \
    && mkdir -p ${HOME}/.config/insight-nodejs/ \
    && chmod -R 777 ${HOME}/.config/ \
    # use typescript globally (to have tsc/typescript working)
    && npm install -g typescript@2.9.2 \
    # Disable the statistics for yeoman
    && echo '{"optOut": true}' > $HOME/.config/insight-nodejs/insight-yo.json \
    # Add software collection git to env
    && echo 'source scl_source enable rh-git29' >> /home/theia/.bashrc \
    # Change permissions to allow editing of files for openshift user
    && find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \;
COPY --chown=theia:root --from=builder /home/theia /home/theia

USER theia
ADD src/entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
