# Copyright (c) 2012-2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

# Variables in `COPY --from=` is not supported see https://github.com/moby/moby/issues/34482
# this is workaround to handle that.
ARG CHE_DASHBOARD_ORGANIZATION=quay.io/eclipse
ARG CHE_DASHBOARD_NEXT_ORGANIZATION=quay.io/che-incubator
ARG CHE_DASHBOARD_VERSION=next
ARG CHE_DASHBOARD_NEXT_VERSION=next
ARG CHE_WORKSPACE_LOADER_ORGANIZATION=quay.io/eclipse
ARG CHE_WORKSPACE_LOADER_VERSION=next

FROM ${CHE_DASHBOARD_ORGANIZATION}/che-dashboard:${CHE_DASHBOARD_VERSION} as che_dashboard_base
FROM ${CHE_DASHBOARD_NEXT_ORGANIZATION}/che-dashboard-next:${CHE_DASHBOARD_NEXT_VERSION} as che_dashboard_next
FROM ${CHE_WORKSPACE_LOADER_ORGANIZATION}/che-workspace-loader:${CHE_WORKSPACE_LOADER_VERSION} as che_workspace_loader_base

FROM openjdk:11-jre-slim
ENV LANG=C.UTF-8
RUN echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && rm -rf /tmp/*
EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
COPY --from=che_dashboard_base /usr/local/apache2/htdocs/dashboard /home/user/eclipse-che/tomcat/webapps/dashboard
COPY --from=che_dashboard_next /usr/local/apache2/htdocs/dashboard /home/user/eclipse-che/tomcat/webapps/dashboard/next
COPY --from=che_workspace_loader_base /usr/local/apache2/htdocs/workspace-loader/ /home/user/eclipse-che/tomcat/webapps/workspace-loader
ADD eclipse-che /home/user/eclipse-che

# this should fail if the startup script is not found in correct path /home/user/eclipse-che/tomcat/bin/catalina.sh
RUN mkdir /logs /data && \
    chmod 0777 /logs /data && \
    chgrp -R 0 /home/user /logs /data && \
    chown -R user /home/user && \
    chmod -R g+rwX /home/user && \
    find /home/user -type d -exec chmod 777 {} \; && \
    # set group write permission so that entrypoint.sh can update permissions once file is updated w/ new cert
    chmod g+w /home/user/cacerts && \
    java -version && echo -n "Server startup script in: " && \
    find /home/user/eclipse-che -name catalina.sh | grep -z /home/user/eclipse-che/tomcat/bin/catalina.sh

USER user
