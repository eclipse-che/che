# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

# Use upstream image
FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-theia-dev:${BUILD_TAG}
# Just try to build the latest theia with current image
RUN git clone -b 'v0.3.17' --single-branch --depth 1 https://github.com/theia-ide/theia theia
RUN cd theia && yarn
 
