# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial implementation
#
FROM jekyll/jekyll:3.2.1

ENV UNISON_VERSION=2.40.102

RUN apk add --update build-base curl bash openssh && \
    apk add ocaml --update-cache --repository http://dl-6.alpinelinux.org/alpine/edge/testing/ --allow-untrusted && \
    curl -L https://www.seas.upenn.edu/~bcpierce/unison/download/releases/unison-$UNISON_VERSION/unison-$UNISON_VERSION.tar.gz | tar xzv -C /tmp && \
    cd /tmp/unison-$UNISON_VERSION && \
    make && \
    cp /tmp/unison-$UNISON_VERSION/unison /usr/local/bin && \
    echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    ssh-keygen -f ${HOME}/.ssh/id_rsa -t rsa -q -N ''  && \
    chown -R jekyll:jekyll /srv/jekyll
    
RUN apk del ocaml curl build-base && \
    echo "ChallengeResponseAuthentication no" >> /etc/ssh/sshd_config && \
    echo "PasswordAuthentication no" >> /etc/ssh/sshd_config && \
    echo "LogLevel VERBOSE" >> /etc/ssh/sshd_config && \
    echo "SyslogFacility AUTH" >> /etc/ssh/sshd_config && \
    rm -rf /tmp /var/cache/apk/* /var/lib/apt/lists/* && \
    ssh-keygen -A && \
    syslogd && passwd -d jekyll && \
    mv /usr/local/bin/unison /bin/unison && \
    gem install kramdown rouge
    
USER jekyll
    
RUN mkdir -m 700 /home/jekyll/.ssh && ssh-keygen -f ${HOME}/.ssh/id_rsa -t rsa -q -C '' -N '' && \
    mv /home/jekyll/.ssh/id_rsa.pub /home/jekyll/.ssh/authorized_keys && \
    chmod 600 /home/jekyll/.ssh/authorized_keys 


WORKDIR /srv/jekyll

CMD sudo ssh-keygen -A && sudo /usr/sbin/sshd -D && \
    tail -f /dev/null