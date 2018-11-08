# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

# Use upstream image
FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-theia:${BUILD_TAG} as theia
FROM cypress/base:8

USER root
ENV HOME=/root

CMD /root/docker-run.sh
RUN yarn global add typescript@2.9.2 node-gyp

# Add cypress scripts and grab dependencies
COPY src /root/
RUN cd /root && yarn

# Add tests
ADD cypress /root/cypress/

COPY --from=theia /home/theia /home/theia
COPY --from=theia /entrypoint.sh /entrypoint.sh
RUN find /home/theia/ -name "binding.gyp" |  xargs -i sh -c 'cd $(dirname {}) && node-gyp rebuild'
 