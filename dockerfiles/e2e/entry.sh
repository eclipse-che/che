#!/bin/bash


if ! whoami &> /dev/null; then
  if [ -w /etc/passwd ]; then
    echo "${USER_NAME:-default}:x:$(id -u):0:${USER_NAME:-default} user:${HOME}:/sbin/nologin" >> /etc/passwd
  fi
fi

/usr/bin/supervisord --configuration /etc/supervisord.conf &

SUPERVISOR_PID=$! &

sleep 15


function shutdown {
    echo "Trapped SIGTERM/SIGINT/x so shutting down supervisord..."
    kill -s SIGTERM ${SUPERVISOR_PID}
    wait ${SUPERVISOR_PID}
    echo "Shutdown complete"
}

# trap shutdown SIGTERM SIGINT
# wait ${SUPERVISOR_PID}

export TS_SELENIUM_REMOTE_DRIVER_URL=http://localhost:4444/wd/hub
export TS_SELENIUM_BASE_URL=http://che-che.192.168.99.196.nip.io

npm run test-happy-path
