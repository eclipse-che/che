# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# build:
#   docker build -t eclipse/che-ip .
#
# use:
#    docker run --rm --net=host eclipse/che-ip

FROM alpine:3.4

COPY /src/ /ip/
ENTRYPOINT ["/ip/entrypoint.sh"]
