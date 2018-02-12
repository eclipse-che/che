FROM node:8-alpine
# build dependencies requireed to compile a custom Theia
RUN apk add --no-cache make gcc g++ python git openssh bash
WORKDIR /home/theia
# build Theia with all extensions to persist yarn cache in the image and
# have default Theia build in the workspace in case no plugins are requested
ADD theia-full-package.json /home/theia/package.json
ADD theia-default-package.json /home/default/theia/package.json
ADD src/main.js /theia_launcher/theia_launcher.js
RUN yarn && \
    yarn theia build && \
    rm -rf * && \
    cd /home/default/theia && \
    yarn && \
    yarn theia build
EXPOSE 3000
ENV USE_LOCAL_GIT=true \
    GITHUB_TOKEN=957257352ad57b7853c8b8714d2201bf4d337b5e
#    ENV THEIA_PLUGINS=[\"@theia\/extension-manager\",\"\@theia\/python\"]
ENTRYPOINT ["node", "/theia_launcher/theia_launcher.js"]
