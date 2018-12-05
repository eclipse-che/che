# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

###
# Builder Image
#
FROM node:8.12.0-alpine

RUN apk add --update --no-cache curl jq expect make gcc g++ python git bash openssh openssh-keygen

ENV HOME=/home/theia-dev
WORKDIR ${HOME}

# add eclipse che theia generator
RUN yarn global add @eclipse-che/theia-generator@0.0.1-1543478099
# change permissions
RUN find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \; 2>log.txt

EXPOSE 3000 3030

CMD tail -f /dev/null
