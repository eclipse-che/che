# Copyright (c) 2019 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

FROM library/centos:centos7

ENV LANG=en_US.utf8 \
    DISPLAY=:99 \
    FABRIC8_USER_NAME=fabric8
    
COPY google-chrome.repo /etc/yum.repos.d/google-chrome.repo

RUN yum install --assumeyes epel-release && \
    yum update --assumeyes && \
    yum install --assumeyes google-chrome-stable && \
    yum install --assumeyes \
        xorg-x11-server-Xvfb \
        git \
        unzip \
        centos-release-scl && \
    yum install --assumeyes nodejs && \
    npm install -g typescript && \
    yum install jq --assumeyes && \
    yum install x11vnc --assumeyes && \
    yum clean all --assumeyes && \
    rm -rf /var/cache/yum && \ 
    # Get compatible versions of chrome and chromedriver
    chrome_version=$(google-chrome --version | grep -oiE "[0-9]*\.[0-9]*\.[0-9]*") && \
    chromedriver_version=$(curl -s -g https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${chrome_version}) && \
    $(curl -sS -g https://chromedriver.storage.googleapis.com/${chromedriver_version}/chromedriver_linux64.zip > chromedriver_linux64.zip) && \
    unzip chromedriver_linux64.zip && mv chromedriver /usr/bin/chromedriver && chmod +x /usr/bin/chromedriver && rm chromedriver_linux64.zip
    
COPY e2e/package.json e2e/package-lock.json /root/e2e/
RUN cd /root/e2e && \
    npm --silent i
    
COPY e2e /root/e2e
COPY docker-entrypoint.sh /root/

WORKDIR /root/

ENTRYPOINT ["/root/docker-entrypoint.sh"]
