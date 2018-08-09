# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

FROM centos

ARG GITHUB_TOKEN
ARG THEIA_VERSION=0.3.13

EXPOSE 3000 3030

ENV THEIA_VERSION=${THEIA_VERSION} \
    USE_LOCAL_GIT=true \
    HOME=/home/theia

WORKDIR ${HOME}

ADD src/ ${HOME}/
ADD supervisord.conf /etc/

RUN ${HOME}/setup.sh

ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
