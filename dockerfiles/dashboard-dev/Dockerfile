# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

FROM node:8.16.0

RUN apt-get update && \
    apt-get install -y git \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/* \
    && echo fs.inotify.max_user_watches=524288 | tee -a /etc/sysctl.conf

WORKDIR "/projects"

ADD src/entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
CMD tail -f /dev/null
