# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial implementation
#
# BUILD:
#  docker build -t codenvy/che-mount .
#
# LAUNCH CONTAINER:
#  docker run --rm -it --cap-add SYS_ADMIN --device /dev/fuse 
#             -v /var/run/docker.sock:/var/run/docker.sock
#             -v <host-dir>:/mnthost codenvy/che-mount <ip> <ws-port>
#
# RUN IN CONTAINER:
#  echo "secret" | $(echo "yes" | sshfs user@10.0.75.2:/projects /mntssh -p 32774)
#   
# TO UNMOUNT IN CONTAINER
#  fusermount -u /mntssh
#
# INTERNAL SYNC SCRIPT
#   /bin/synch.sh <ip> <ws-port>
FROM alpine:3.4

ENV UNISON_VERSION=2.48.4

RUN apk add --update build-base curl bash sshfs && \
    apk add ocaml --update-cache --repository http://dl-3.alpinelinux.org/alpine/edge/testing/ --allow-untrusted && \
    curl -L https://www.seas.upenn.edu/~bcpierce/unison//download/releases/unison-$UNISON_VERSION/unison-$UNISON_VERSION.tar.gz | tar xzv -C /tmp && \
    cd /tmp/src && \
    sed -i -e 's/GLIBC_SUPPORT_INOTIFY 0/GLIBC_SUPPORT_INOTIFY 1/' fsmonitor/linux/inotify_stubs.c && \
    make && \
    cp /tmp/src/unison /usr/local/bin && \
    apk del ocaml curl build-base bash && \
    rm -rf /tmp /var/cache/apk/* && \
    mkdir /mntssh && \
    mkdir /mnthost

COPY /sync.sh /bin/sync.sh

ENTRYPOINT ["/bin/sync.sh"]
