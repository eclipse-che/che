FROM selenium/standalone-chrome:latest

USER root

RUN apt-get update && apt-get install && \
    apt-get install -y nodejs && \
    apt-get install -y npm && \
    npm install -g typescript && \
    apt-get install vnc4server -y

COPY e2e/package.json e2e/package-lock.json /tmp/e2e/

RUN cd /tmp/e2e && \
    npm --silent i

COPY e2e /tmp/e2e
COPY entrypoint.sh /tmp/

WORKDIR /tmp/e2e

ENTRYPOINT [ "/tmp/entrypoint.sh" ]
