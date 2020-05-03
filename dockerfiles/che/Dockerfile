# Copyright (c) 2012-2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
FROM alpine:3.11
RUN apk --no-cache add "openjdk11-jre=11.0.5_p10-r0"  bash
ENV LANG=C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk
RUN echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && rm -rf /tmp/*
EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
RUN mkdir /logs /data && \
    chmod 0777 /logs /data
ADD eclipse-che /home/user/eclipse-che
RUN find /home/user -type d -exec chmod 777 {} \;
