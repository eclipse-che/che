# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc.- initial API and implementation


FROM eclipse/centos_jdk8


RUN sudo yum -y install  \
           rh-nodejs6 \
           gcc-c++ \
           gcc \
           glibc-devel \
           make \
           golang \
    sudo yum clean all && \
    cat /opt/rh/rh-nodejs6/enable >> /home/user/.bashrc



ENV GOPATH=$HOME/go \
    PATH=$GOPATH/bin:/usr/local/go/bin:$PATH

WORKDIR /projects/