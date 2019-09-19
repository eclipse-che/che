#!/bin/bash

# Validate selenium base URL
if [ -z "$TS_SELENIUM_BASE_URL" ]; then
    echo "The \"TS_SELENIUM_BASE_URL\" is not set!";
    echo "Please, set the \"TS_SELENIUM_BASE_URL\" environment variable."
    exit 1
fi

# Set testing suite
if [ -z "$TEST_SUITE" ]; then
    TEST_SUITE=test-happy-path
fi

# Launch selenium server
/usr/bin/supervisord --configuration /etc/supervisord.conf & \
export TS_SELENIUM_REMOTE_DRIVER_URL=http://localhost:4444/wd/hub

# Check selenium server launching
expectedStatus=200
currentTry=1
maximumAttempts=5

while [ $(curl -s -o /dev/null -w "%{http_code}" --fail http://localhost:4444/wd/hub/status) != $expectedStatus ];
do
  if (( currentTry > maximumAttempts ));
  then
    status=$(curl -s -o /dev/null -w "%{http_code}" --fail http://localhost:4444/wd/hub/status)
    echo "Exceeded the maximum number of checking attempts,"
    echo "selenium server status is '$status' and it is different from '$expectedStatus'";
    exit 1;
  fi;

  echo "Wait selenium server availability ..."

  curentTry=$((curentTry + 1))
  sleep 1
done

# Print information about launching tests
if mount | grep 'e2e'; then
	echo "The local code is mounted. Executing local code."
	cd /tmp/e2e || exit
	npm install
else
	echo "Executing e2e tests from an image."
	cd /tmp/e2e || exit
fi


# Launch tests
npm run $TEST_SUITE
