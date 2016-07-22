# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Mario Loriedo - Initial implementation
#
# To build, in this directory:
#  `docker build -t codenvy/che-launcher .`
#
# To use it:
#  `docker run -v /var/run/docker.sock:/var/run/docker.sock codenvy/che start`
#  `docker run -v /var/run/docker.sock:/var/run/docker.sock codenvy/che stop`
#  `docker run -v /var/run/docker.sock:/var/run/docker.sock codenvy/che restart`
#  `docker run -v /var/run/docker.sock:/var/run/docker.sock codenvy/che update`
#

FROM alpine:3.4

RUN apk add --no-cache \
    ca-certificates \
    curl \
    openssl

ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.6.0

RUN set -x \
  && curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
  > /usr/bin/docker; chmod +x /usr/bin/docker

COPY /launcher_cmds.sh /bin/launcher_cmds.sh
COPY /launcher_funcs.sh /bin/launcher_funcs.sh
COPY /launcher.sh /bin/launcher.sh

ENTRYPOINT ["bin/launcher.sh"]
