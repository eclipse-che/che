#
# To build, in this directory:
#  `docker build -t codenvy/che .`
#
# To use it:
#  `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che start`
#  `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che stop`
#  `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che restart`
#  `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che update`
#

FROM alpine:3.4

RUN apk add --no-cache \
    ca-certificates \
    curl \
    openssl

ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.6.0

RUN set -x \
  && curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
  > /usr/bin/docker; chmod +x /usr/bin/docker

COPY /launcher.sh /bin/launcher.sh

ENTRYPOINT ["bin/launcher.sh"]
