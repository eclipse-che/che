# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

FROM centos

ARG GITHUB_TOKEN
ARG THEIA_VERSION=0.3.14

EXPOSE 3000 3030

ENV THEIA_VERSION=${THEIA_VERSION} \
    USE_LOCAL_GIT=true \
    HOME=/home/theia

WORKDIR ${HOME}

ADD src/ ${HOME}/
ADD supervisord.conf /etc/

RUN ${HOME}/setup.sh

USER theia
ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
