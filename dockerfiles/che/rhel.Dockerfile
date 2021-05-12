# Copyright (c) 2018-2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

# https://access.redhat.com/containers/?tab=tags#/registry.access.redhat.com/ubi8-minimal
FROM registry.access.redhat.com/ubi8-minimal:8.3-298.1618432845
USER root
ENV CHE_HOME=/home/user/eclipse-che
ENV JAVA_HOME=/usr/lib/jvm/jre
RUN microdnf install java-11-openjdk-headless tar gzip shadow-utils findutils && \
    microdnf update -y gnutls && \
    microdnf -y clean all && rm -rf /var/cache/yum && echo "Installed Packages" && rpm -qa | sort -V && echo "End Of Installed Packages" && \
    adduser -G root user && mkdir -p /home/user/eclipse-che
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

ADD eclipse-che /home/user/eclipse-che
# this should fail if the startup script is not found in correct path /home/user/eclipse-che/tomcat/bin/catalina.sh
RUN mkdir /logs /data && \
    chmod 0777 /logs /data && \
    chgrp -R 0 /home/user /logs /data && \
    chown -R user /home/user && \
    chmod -R g+rwX /home/user && \
    find /home/user -type d -exec chmod 777 {} \; && \
    java -version && echo -n "Server startup script in: " && \
    find /home/user/eclipse-che -name catalina.sh | grep -z /home/user/eclipse-che/tomcat/bin/catalina.sh

USER user

# append Brew metadata here
