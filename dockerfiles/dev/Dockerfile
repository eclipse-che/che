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

RUN sudo yum -y update && \
    sudo yum -y install  \
           rh-maven35 \
           plexus-classworlds \
           rh-nodejs6 \
           gcc-c++ \
           gcc \
           glibc-devel \
           bzip2 \
           make \
           golang \
    sudo yum clean all && \
    cat /opt/rh/rh-maven35/enable >> /home/user/.bashrc  && \
    cat /opt/rh/rh-nodejs6/enable >> /home/user/.bashrc && \
    sudo ln -s /opt/rh/rh-nodejs6/root/usr/bin/node /usr/local/bin/nodejs

ENV TOMCAT_VERSION=8.5.23

RUN mkdir $HOME/.m2 && \
    mkdir /home/user/tomcat8 && \
    wget -qO- "https://archive.apache.org/dist/tomcat/tomcat-8/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz" | tar -zx --strip-components=1 -C /home/user/tomcat8 && \
    rm -rf /home/user/tomcat8/webapps/* && \
    echo "export MAVEN_OPTS=\$JAVA_OPTS" >> /home/user/.bashrc

USER user


ENV LD_LIBRARY_PATH=/opt/rh/rh-nodejs6/root/usr/lib64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}} \
 PYTHONPATH=/opt/rh/rh-nodejs6/root/usr/lib/python2.7/site-packages${PYTHONPATH:+:${PYTHONPATH}} \
 MANPATH=/opt/rh/rh-nodejs6/root/usr/share/man:$MANPATH \
 TOMCAT_HOME=/home/user/tomcat8 \
 TERM=xterm  \
 M2_HOME=/opt/rh/rh-maven35/root/usr/share/maven \
 GOPATH=$HOME/go \
 NODEJS_VERSION=6 \
 NPM_RUN=start \
 NPM_CONFIG_PREFIX=$HOME/.npm-global
ENV PATH=$HOME/node_modules/.bin/:$HOME/.npm-global/bin/:$GOPATH/bin:$M2_HOME/bin:/opt/rh/rh-nodejs6/root/usr/bin:/usr/local/go/bin:$PATH

RUN sudo mkdir /var/run/sshd
RUN sudo  ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N '' && \
    sudo  ssh-keygen -t rsa -f /etc/ssh/ssh_host_ecdsa_key -N '' && \
    sudo  ssh-keygen -t rsa -f /etc/ssh/ssh_host_ed25519_key -N '' && \
    npm install -g typescript@2.5.3 typescript-language-server@0.1.4

WORKDIR /projects
