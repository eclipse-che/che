#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

cmd_network() {
  debug $FUNCNAME

  info ""
  info "---------------------------------------"
  info "--------   CONNECTIVITY TEST   --------"
  info "---------------------------------------"

  info "network" "eclipse/che-ip: ${GLOBAL_HOST_IP}"

  start_test_server

  info "network" "Browser => Workspace Agent (localhost): Connection $(test1 && echo "succeeded" || echo "failed")"
  info "network" "Browser => Workspace Agent ($AGENT_EXTERNAL_IP): Connection $(test2 && echo "succeeded" || echo "failed")"
  info "network" "Server  => Workspace Agent (External IP): Connection $(test3 && echo "succeeded" || echo "failed")"
  info "network" "Server  => Workspace Agent (Internal IP): Connection $(test4 && echo "succeeded" || echo "failed")"

  stop_test_server
}

start_test_server() {
  export AGENT_INTERNAL_PORT=80
  export AGENT_EXTERNAL_PORT=32768

  # Start mini httpd server to run simulated tests
  docker run -d -p $AGENT_EXTERNAL_PORT:$AGENT_INTERNAL_PORT --name fakeagent \
             ${UTILITY_IMAGE_ALPINE} httpd -f -p $AGENT_INTERNAL_PORT -h /etc/ >> "${LOGS}"

  export AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
  export AGENT_EXTERNAL_IP=$CHE_HOST
}

stop_test_server() {
  # Remove httpd server
  docker rm -f fakeagent >> "${LOGS}"  
}

test1() {
  HTTP_CODE=$(curl -I localhost:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out '%{http_code}') || echo "28" >> "${LOGS}"
  
  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

test2() {
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out '%{http_code}') || echo "28" >> "${LOGS}"

  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

test3() {
   HTTP_CODE=$(docker_run --name fakeserver \
                                --entrypoint=curl \
                                $(eval "echo \${IMAGE_${CHE_PRODUCT_NAME}}") \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out '%{http_code}')

  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

test4() {
  HTTP_CODE=$(docker_run --name fakeserver \
                                --entrypoint=curl \
                                $(eval "echo \${IMAGE_${CHE_PRODUCT_NAME}}") \
                                  -I ${AGENT_INTERNAL_IP}:${AGENT_INTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out '%{http_code}')  

  if check_http_code $HTTP_CODE; then
    return 0
  else
    return 1
  fi
}

check_http_code() {
  if [ "${1}" = "200" ]; then
    return 0
  else
    return 1
  fi
}