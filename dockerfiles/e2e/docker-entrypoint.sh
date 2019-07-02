#!/bin/bash

if [ -z $TS_SELENIUM_BASE_URL ]; then
    echo "TS_SELENIUM_BASE_URL is not set!";
    exit 1
fi

chromedriver &
/usr/bin/Xvfb :1 -screen 0 1920x1080x24 +extension RANDR > /dev/null 2>&1 &
x11vnc -display :1.0 > /dev/null 2>&1 &
export DISPLAY=:1.0

hostname=$(hostname -I)
echo "You can watch locally using VNC with IP: $hostname"

if mount | grep 'local_tests'; then
	echo "The local code is mounted. Executing local code."
	cd local_tests
	npm i
else
	echo "Executing e2e tests from an image."
	cd e2e
fi

npm run test
