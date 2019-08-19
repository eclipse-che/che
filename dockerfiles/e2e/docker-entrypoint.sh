#!/bin/bash
# Copyright (c) 2019 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

if [ -z "$TS_SELENIUM_BASE_URL" ]; then
    echo "TS_SELENIUM_BASE_URL is not set!";
    exit 1
fi

chromedriver --whitelisted-ips= &
/usr/bin/Xvfb :1 -screen 0 1920x1080x24 +extension RANDR > /dev/null 2>&1 &
x11vnc -display :1.0 > /dev/null 2>&1 &
export DISPLAY=:1.0

sleep 15


hostname=$(hostname -I)
echo "You can watch locally using VNC with IP: $hostname"

if mount | grep 'local_tests'; then
	echo "The local code is mounted. Executing local code."
	cd local_tests || exit
	npm i
else
	echo "Executing e2e tests from an image."
	cd e2e || exit
fi


echo "================"
nmap -p 9515 localhost
echo "================"


export TS_SELENIUM_REMOTE_DRIVER_URL=http://localhost:9515

npm run test
