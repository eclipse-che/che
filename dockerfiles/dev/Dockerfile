# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc.- initial API and implementation


FROM registry.centos.org/che-stacks/centos-stack-base
EXPOSE 4403 8080 8000 9876 22

LABEL che:server:8080:ref=tomcat8 che:server:8080:protocol=http che:server:8000:ref=tomcat8-debug che:server:8000:protocol=http che:server:9876:ref=codeserver che:server:9876:protocol=http

RUN sudo yum -y update && \
    sudo yum -y install  \
           rh-maven35 \
           rh-nodejs6 \
           gcc-c++ \
           gcc \
           glibc-devel \
           make \
           golang \
    sudo yum clean all && \
    cat /opt/rh/rh-maven35/enable >> /home/user/.bashrc  &&\
    cat /opt/rh/rh-nodejs6/enable >> /home/user/.bashrc

USER user

ENV TOMCAT_HOME=/home/user/tomcat8 \
    TERM=xterm

ENV MAVEN_OPTS=$JAVA_OPTS\
    GOPATH=$HOME/go \
    PATH=$GOPATH/bin:/usr/local/go/bin:$PATH

RUN mkdir $HOME/.m2 && \
    mkdir /home/user/tomcat8 && \
    wget -qO- "https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.23/bin/apache-tomcat-8.5.23.tar.gz" | tar -zx --strip-components=1 -C /home/user/tomcat8 && \
    rm -rf /home/user/tomcat8/webapps/* && \
    echo "export MAVEN_OPTS=\$JAVA_OPTS" >> /home/user/.bashrc

ADD ./contrib/settings.xml $HOME/.m2/settings.xml


WORKDIR /projects/