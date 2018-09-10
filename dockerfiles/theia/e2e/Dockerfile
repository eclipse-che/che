# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

# Use upstream image
FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-theia:${BUILD_TAG}

USER root

# Install packages used by Cypress
RUN yum install -y gtk2-2.24.31-1.el7.x86_64 \
                   libnotify-0.7.7-1.el7.x86_64 \
                   GConf2 nss-3.36.0-5.el7_5.x86_64 \
                   libXScrnSaver-1.2.2-6.1.el7.x86_64 \
                   alsa-lib-1.1.4.1-2.el7.x86_64 \
                   xorg-x11-server-Xvfb-1.19.5-5.el7.x86_64 \
                   liberation-* && \
    dbus-uuidgen > /var/lib/dbus/machine-id

# Add cypress scripts and grab dependencies
COPY src /home/cypress/
RUN cd /home/cypress && yarn

# Add tests
ADD cypress /home/cypress/cypress/
# Command to trigger the tests
ENTRYPOINT [""]
CMD /home/cypress/docker-run.sh
