# Copyright (c) 2012-2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
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
FROM openjdk:8u191-jdk-alpine

ENV LANG=C.UTF-8 \
    DOCKER_VERSION=1.6.0 \
    DOCKER_BUCKET=get.docker.com \
    CHE_IN_CONTAINER=true

RUN echo "http://dl-4.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories && \
    apk add --update curl openssl sudo bash && \
    curl -sSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-${DOCKER_VERSION}" -o /usr/bin/docker && \
    chmod +x /usr/bin/docker && \
    echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    rm -rf /tmp/* /var/cache/apk/*

EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
COPY open-jdk-source-file-location /open-jdk-source-file-location
ENTRYPOINT ["/entrypoint.sh"]
RUN mkdir /logs /data && \
    chmod 0777 /logs /data
ADD eclipse-che /home/user/eclipse-che
RUN find /home/user -type d -exec chmod 777 {} \;
