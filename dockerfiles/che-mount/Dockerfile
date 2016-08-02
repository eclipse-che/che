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

FROM centos

RUN rpm -Uvh http://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-7.noarch.rpm && \
    yum -y install fuse-sshfs && \
    mkdir /mntssh && \
    mkdir /mnthost

RUN yum install -y yum install -y ocaml ocaml-camlp4-devel ctags ctags-etags make && \
    mkdir -p /opt/unison && \
    cd /opt/unison && \
    curl -O http://www.seas.upenn.edu/~bcpierce/unison//download/releases/stable/unison-2.48.4.tar.gz && \
    tar xzvf unison-2.48.4.tar.gz && \
    cd src && \
    make && \
    cp unison /usr/local/sbin/ && \
    cp unison /usr/bin/ && \
    cd /opt && \
    rm -rf src/

COPY sync.sh /bin

ENTRYPOINT ["/bin/sync.sh"]
