FROM alpine/git:latest as clonning
# If value is git marketplace files will be cloned from git repo in $STORAGE_REPO
# Otherwise files will be copied from $STORAGE_PATH.
# Make sure $STORAGE_PATH is in the build context
ARG STORAGE_TYPE=git
ARG STORAGE_REPO=https://github.com/garagatyi/marketplace-storage
ARG STORAGE_PATH=.
COPY $STORAGE_PATH /tmp/local-marketplace-storage
RUN mkdir /tmp/marketplace-storage && \
    if [ "$STORAGE_TYPE" == "git" ]; then \
      git clone $STORAGE_REPO /tmp/marketplace-storage; \
    else \
      cp -r /tmp/local-marketplace-storage/* /tmp/marketplace-storage/; \
    fi

FROM centos/httpd-24-centos7
COPY --from=clonning /tmp/marketplace-storage/ /var/www/html/
