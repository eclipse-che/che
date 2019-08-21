# FROM selenium/standalone-chrome-debug
FROM selenium/standalone-chrome:3.141.59-titanium

USER root

RUN apt-get update && apt-get install && \
    apt-get install -y nodejs && \
    apt-get install -y npm && \
    npm install -g typescript

COPY e2e/package.json e2e/package-lock.json /tmp/e2e/

RUN cd /tmp/e2e && \
    npm --silent i

COPY e2e /tmp/e2e
COPY entry.sh /tmp/

WORKDIR /tmp/e2e

ENTRYPOINT [ "/tmp/entry.sh" ]
