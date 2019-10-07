# Copyright (c) 2012-2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc.- initial API and implementation


FROM registry.centos.org/che-stacks/centos-stack-base

EXPOSE 4403 8080 8000 9876 22

RUN sudo yum -y install epel-release && \
    sudo yum -y update && \
    sudo yum -y install  \
           rh-maven35 \
           plexus-classworlds \
           rh-nodejs8 \
           gcc-c++ \
           gcc \
           glibc-devel \
           bzip2 \
           make \
           golang \
    sudo yum clean all && \
    cat /opt/rh/rh-maven35/enable >> /home/user/.bashrc  && \
    cat /opt/rh/rh-nodejs8/enable >> /home/user/.bashrc && \
    sudo ln -s /opt/rh/rh-nodejs8/root/usr/bin/node /usr/local/bin/nodejs

ENV TOMCAT_VERSION=8.5.23 YARN_VERSION=1.15.2

RUN mkdir $HOME/.m2 && \
   wget -O  /home/user/tomcat8.zip "https://oss.sonatype.org/content/repositories/releases/org/eclipse/che/lib/che-tomcat8-slf4j-logback/6.11.0/che-tomcat8-slf4j-logback-6.11.0.zip" ;\
   unzip   /home/user/tomcat8.zip -d /home/user/tomcat8;\
   rm /home/user/tomcat8.zip;\
   mkdir /home/user/tomcat8/webapps;\
   sed -i -- 's/autoDeploy=\"false\"/autoDeploy=\"true\"/g' /home/user/tomcat8/conf/server.xml; \
   sed -i 's/<Context>/<Context reloadable=\"true\">/g' /home/user/tomcat8/conf/context.xml; \
   echo "export MAVEN_OPTS=\$JAVA_OPTS" >> /home/user/.bashrc

RUN wget -qO- https://dl.yarnpkg.com/debian/pubkey.gpg | gpg --import \
    && curl -fsSLO --compressed "https://yarnpkg.com/downloads/$YARN_VERSION/yarn-v$YARN_VERSION.tar.gz" \
    && curl -fsSLO --compressed "https://yarnpkg.com/downloads/$YARN_VERSION/yarn-v$YARN_VERSION.tar.gz.asc" \
    && gpg --batch --verify yarn-v$YARN_VERSION.tar.gz.asc yarn-v$YARN_VERSION.tar.gz \
    && sudo mkdir -p /opt \
    && sudo tar -xzf yarn-v$YARN_VERSION.tar.gz -C /opt/ \
    && sudo ln -s /opt/yarn-v$YARN_VERSION/bin/yarn /usr/local/bin/yarn \
    && sudo ln -s /opt/yarn-v$YARN_VERSION/bin/yarnpkg /usr/local/bin/yarnpkg \
    && rm yarn-v$YARN_VERSION.tar.gz.asc yarn-v$YARN_VERSION.tar.gz

USER user

ENV LD_LIBRARY_PATH=/opt/rh/rh-nodejs8/root/usr/lib64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}} \
 PYTHONPATH=/opt/rh/rh-nodejs8/root/usr/lib/python2.7/site-packages${PYTHONPATH:+:${PYTHONPATH}} \
 MANPATH=/opt/rh/rh-nodejs8/root/usr/share/man:$MANPATH \
 TOMCAT_HOME=/home/user/tomcat8 \
 TERM=xterm  \
 M2_HOME=/opt/rh/rh-maven35/root/usr/share/maven \
 GOPATH=$HOME/go \
 NODEJS_VERSION=6 \
 NPM_RUN=start \
 NPM_CONFIG_PREFIX=$HOME/.npm-global

ENV PATH=$HOME/node_modules/.bin/:$HOME/.npm-global/bin/:$GOPATH/bin:$M2_HOME/bin:/opt/rh/rh-nodejs8/root/usr/bin:/usr/local/go/bin:$PATH

RUN mkdir /home/user/traefik ;\
    wget -O /home/user/traefik/traefik "https://github.com/containous/traefik/releases/download/v1.4.3/traefik_linux-amd64"; \
    chmod +x /home/user/traefik/traefik
COPY traefik.toml /home/user/traefik/
ADD entrypoint.sh /home/user/entrypoint.sh
RUN sudo mkdir /var/run/sshd && \
    sudo  ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N '' && \
    sudo  ssh-keygen -t rsa -f /etc/ssh/ssh_host_ecdsa_key -N '' && \
    sudo  ssh-keygen -t rsa -f /etc/ssh/ssh_host_ed25519_key -N '' && \
    npm install -g typescript@2.5.3 typescript-language-server@0.1.4 && \
    sudo chgrp -R 0 ~ && \
    sudo chmod -R g+rwX ~
WORKDIR /projects
ENTRYPOINT ["/home/user/entrypoint.sh"]
CMD tail -f /dev/null
