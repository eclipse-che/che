FROM selenium/standalone-chrome:latest

ENV DISPLAY=':20'

USER root

RUN apt-get update && apt-get install && \
    apt-get install -y nodejs && \
    apt-get install -y npm && \
    npm install -g typescript && \
    apt-get install x11vnc -y

COPY e2e/package.json e2e/package-lock.json /tmp/e2e/

RUN cd /tmp/e2e && \
    npm --silent i

COPY e2e /tmp/e2e
COPY entrypoint.sh /tmp/

WORKDIR /tmp/e2e

EXPOSE 5920

ENTRYPOINT [ "/tmp/entrypoint.sh" ]
