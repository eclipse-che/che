# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

# Initializes an empty directory with the templates needed to configure and run Che.
#
# build:
#   docker build -t eclipse/che-init:<version> .
#
# use (to copy files onto folder):
#   docker run -v $(pwd):/che eclipse/che-init:<version>
#
# use (to run puppet config):
#   docker run <puppet-mounts> --entrypoint=/usr/bin/puppet eclipse/che-init:<version> apply <puppet-apply-options>


FROM puppet/puppet-agent-alpine:4.6.2

# install openssh needed to generate ssh keys
RUN apk --update add openssh \
    && rm -rf /var/cache/apk/* \
    && mkdir -p /files \
    && mkdir -p /etc/puppet/modules \
    && mkdir -p /etc/puppet/manifests \
    && mkdir -p /files/docs

COPY manifests /etc/puppet/manifests
COPY modules /etc/puppet/modules
COPY entrypoint.sh /
RUN chmod +x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
