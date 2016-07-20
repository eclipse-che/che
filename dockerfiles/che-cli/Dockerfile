# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - Initial implementation
#
# To build, in this directory:
#  `docker build -t codenvy/che-cli .`
#
# Use it to install files into current directory with Windows:
#   set f=%~dp0:\=/%&set h=%g::=%&set i=/%h%
#   for /l %%a in (1,1,100) do if "!i:~-1!"==" " set i=!i:~0,-1!
#   docker run -v %i%:/che codenvy/che-cli
#
# Use it to install files into current directory with Linux / Mac:
#   docker run -v $(pwd):/che codenvy/che-cli
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

COPY files-to-install che

COPY /install.sh /bin/install.sh

ENTRYPOINT ["bin/install.sh"]
