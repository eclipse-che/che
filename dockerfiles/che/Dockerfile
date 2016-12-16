# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Mario Loriedo
#   Codenvy S.A
#
# To build it, run in the repository root:
#  `docker build -t eclipse/che-server .`
#
# To run it:
#  docker run --net=host \
#             --name che \
#             -v /var/run/docker.sock:/var/run/docker.sock \
#             -v /home/user/che/lib:/home/user/che/lib-copy \
#             -v /home/user/che/workspaces:/home/user/che/workspaces \
#             -v /home/user/che/storage:/home/user/che/storage \
#             codenvy/che-server
#           
FROM alpine:3.4

ENV LANG=C.UTF-8 \
    JAVA_HOME=/usr/lib/jvm/default-jvm/jre \
    PATH=${PATH}:${JAVA_HOME}/bin \
    DOCKER_VERSION=1.6.0 \
    DOCKER_BUCKET=get.docker.com \
    CHE_IN_CONTAINER=true

RUN echo "http://dl-4.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories && \
    apk upgrade --update && \
    apk add --update ca-certificates curl openssl openjdk8 sudo bash && \
    curl -sSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-${DOCKER_VERSION}" -o /usr/bin/docker && \
    chmod +x /usr/bin/docker && \
    echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    rm -rf /tmp/* /var/cache/apk/*

EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
ADD eclipse-che.tar.gz /home/user/
