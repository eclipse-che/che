# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM jboss/keycloak:6.0.1
ADD che /opt/jboss/keycloak/themes/che
ADD . /scripts/
ADD cli /scripts/cli

USER root
RUN chgrp -R 0 /scripts && \
    chmod -R g+rwX /scripts
