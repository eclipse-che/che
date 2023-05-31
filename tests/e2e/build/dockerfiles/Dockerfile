FROM selenium/standalone-chrome:107.0

ENV DISPLAY=':20'

USER root

RUN apt-get update && apt-get install && \
    apt-get install -y ftp x11vnc ffmpeg libvpx6 && \
    curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash - && \
    apt-get install -y nodejs && \
    npm install -g typescript && \
    node -v

COPY --chown=0:0 build/dockerfiles/entrypoint.sh /tmp/

RUN mkdir /tmp/e2e && \
    chmod -R 777 /tmp/e2e

RUN sed -i "s/nodaemon=true/nodaemon=false/" /etc/supervisord.conf

COPY package.json package-lock.json /tmp/e2e/

RUN cd /tmp/e2e && \
    npm i

COPY . /tmp/e2e

WORKDIR /tmp/e2e

EXPOSE 5920

RUN chgrp -R 0 /tmp && \
    chmod -R g+rwX /tmp

ENTRYPOINT [ "/tmp/entrypoint.sh" ]
