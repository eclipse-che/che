FROM node:0.10
WORKDIR /usr/src/app

RUN apt-get update && \
    apt-get -y install sudo procps wget unzip mc gcc make libnotify-bin && \
    echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    useradd -u 5001 -G users,sudo -d /home/user --shell /bin/bash -m user && \
    echo "secret\nsecret" | passwd user && usermod -u 1000 user && apt-get clean

RUN npm install -g gulp bower

ENV CODENVY_APP_BIND_DIR /home/user/application
VOLUME ["/home/user/application"]
RUN mkdir -p /usr/src/app/bower_components && chown -R user /usr/src/app/bower_components && mkdir -p /usr/src/app/node_modules && chown -R user /usr/src/app/node_modules

EXPOSE 5000
ENV CODENVY_APP_PORT_5000_HTTP 5000

USER user
ADD package.json package.json
ADD bower.json bower.json

RUN bower install && npm install

CMD  echo "Updating rights..." && sudo chmod a+rw  /home/user/application/ && \
     ([ -d /home/user/application/node_modules ] || (echo "Initializing NPM dependencies..." && cp -a /usr/src/app/node_modules /home/user/application/)) && \
     ([ -d /home/user/application/bower_components ] || (echo "Initializing Bower dependencies..." && cp -a /usr/src/app/bower_components /home/user/application/)) && \
    umask 0 && cd /home/user/application/ && gulp serve --server http://machines.codenvy-stg.com/
