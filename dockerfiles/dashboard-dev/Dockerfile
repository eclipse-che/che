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

USER root
RUN apt-get update && \
    apt-get install -y git sudo \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/* \
    && echo fs.inotify.max_user_watches=524288 | tee -a /etc/sysctl.conf \
    && usermod -aG root node \
    && sed -e "s#^node:x.*#node:x:\${USER_ID}:\${GROUP_ID}::${HOME}:/bin/bash#g" \
        /etc/passwd > /.passwd.template \
    && sed -e 's#^node:.*#node:x:${GROUP_ID}:#g' \
        /etc/group > /.group.template \
    && echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers \
    # The file permission must be fixed at /entrypoint.sh .
    && mkdir -p /projects \
    && for f in "${HOME}" /projects /etc/passwd /etc/group; do \
           echo "Changing permissions on ${f}" && \
           chgrp -R 0 ${f} && \
           chmod -R g+rwX ${f}; \
       done

ADD src/entrypoint.sh /entrypoint.sh

USER node
WORKDIR "/projects"

ENTRYPOINT ["/entrypoint.sh"]

CMD tail -f /dev/null
