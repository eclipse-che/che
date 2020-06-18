# Copyright (c) 2012-2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# Variables in `COPY --from=` is not supported see https://github.com/moby/moby/issues/34482
# this is workaround to handle that.
ARG CHE_DASHBOARD_ORGANIZATION=quay.io/eclipse
ARG CHE_DASHBOARD_VERSION=next
ARG CHE_WORKSPACE_LOADER_ORGANIZATION=quay.io/eclipse
ARG CHE_WORKSPACE_LOADER_VERSION=next

FROM ${CHE_DASHBOARD_ORGANIZATION}/che-dashboard:${CHE_DASHBOARD_VERSION} as che_dashboard_base
FROM ${CHE_WORKSPACE_LOADER_ORGANIZATION}/che-workspace-loader:${CHE_WORKSPACE_LOADER_VERSION} as che_workspace_loader_base

FROM openjdk:11-jre-slim
ENV LANG=C.UTF-8
RUN echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && rm -rf /tmp/*
EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
RUN mkdir /logs /data && \
    chmod 0777 /logs /data
COPY --from=che_dashboard_base /usr/local/apache2/htdocs/dashboard /home/user/eclipse-che/tomcat/webapps/dashboard
COPY --from=che_workspace_loader_base /usr/local/apache2/htdocs/workspace-loader/ /home/user/eclipse-che/tomcat/webapps/workspace-loader
ADD eclipse-che /home/user/eclipse-che
RUN find /home/user -type d -exec chmod 777 {} \;
