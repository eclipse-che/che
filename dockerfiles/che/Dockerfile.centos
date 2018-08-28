# Copyright (c) 2012-2016 Red Hat, Inc
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Dharmit Shah  - Initial implementation
#   Mario Loriedo - Improvements
#
# To build it, in current folder:
#  cp Dockerfile.centos Dockerfile
#  ./build.sh
#
# To run it:
#  docker run -d -p 8080:8080 \
#            --name che \
#            -v /var/run/docker.sock:/var/run/docker.sock \
#            -v ~/.che/workspaces:/data \
#            eclipse/che-server:nightly
#           
FROM registry.centos.org/centos/centos:latest

ENV LANG=C.UTF-8 \
    JAVA_HOME=/usr/lib/jvm/jre-1.8.0 \
    PATH=${PATH}:${JAVA_HOME}/bin \
    CHE_HOME=/home/user/che \
    DOCKER_VERSION=1.6.0 \
    DOCKER_BUCKET=get.docker.com \
    CHE_IN_CONTAINER=true

RUN yum -y update && \
    yum -y install openssl java sudo && \
    curl -sSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-${DOCKER_VERSION}" -o /usr/bin/docker && \
    chmod +x /usr/bin/docker && \
    yum -y remove openssl && \
    yum clean all && \
    echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    sed -i 's/Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers && \
    rm -rf /tmp/* /var/cache/yum

# The following lines are needed to set the correct locale after `yum update`
# c.f. https://github.com/CentOS/sig-cloud-instance-images/issues/71
RUN localedef -i en_US -f UTF-8 C.UTF-8
ENV LANG="C.UTF-8"

EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
RUN mkdir /logs /data && \
    chmod 0777 /logs /data
ADD eclipse-che /home/user/eclipse-che
RUN find /home/user -type d -exec chmod 777 {} \;

