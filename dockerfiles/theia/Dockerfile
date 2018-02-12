FROM node:8-alpine
RUN apk add --no-cache make gcc g++ python git openssh bash
WORKDIR /home/theia
ADD theia-package.json package.json
RUN yarn && \
    yarn theia build && \
    rm -rf *
EXPOSE 3000
ENV USE_LOCAL_GIT=true
ENV GITHUB_TOKEN=957257352ad57b7853c8b8714d2201bf4d337b5e
ENV THEIA_PLUGINS=[\"@theia\/extension-manager\",\"\@theia\/python\"]
ADD src/main.js /theia_launcher/theia_launcher.js
ENTRYPOINT ["node", "/theia_launcher/theia_launcher.js"]
