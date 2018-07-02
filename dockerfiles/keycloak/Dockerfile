# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

FROM jboss/keycloak-openshift:3.4.3.Final
ADD che /opt/jboss/keycloak/themes/che
ADD . /scripts/
ADD cli /scripts/cli

USER root
RUN chgrp -R 0 /scripts && \
    chmod -R g+rwX /scripts
