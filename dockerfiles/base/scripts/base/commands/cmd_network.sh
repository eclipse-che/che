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

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CHE_VERSION
  fi

  info ""
  info "---------------------------------------"
  info "--------   CONNECTIVITY TEST   --------"
  info "---------------------------------------"
  # Start a fake workspace agent
  log "docker run -d -p 12345:80 --name fakeagent ${UTILITY_IMAGE_ALPINE} httpd -f -p 80 -h /etc/ >> \"${LOGS}\""
  docker run -d -p 12345:80 --name fakeagent ${UTILITY_IMAGE_ALPINE} httpd -f -p 80 -h /etc/ >> "${LOGS}"

  AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
  AGENT_INTERNAL_PORT=80
  AGENT_EXTERNAL_IP=$CHE_HOST
  AGENT_EXTERNAL_PORT=12345


  ### TEST 1: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I localhost:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out '%{http_code}') || echo "28" >> "${LOGS}"

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Browser    => Workspace Agent (localhost): Connection succeeded"
  else
      info "Browser    => Workspace Agent (localhost): Connection failed"
  fi

  ### TEST 1a: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out '%{http_code}') || echo "28" >> "${LOGS}"

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Browser    => Workspace Agent ($AGENT_EXTERNAL_IP): Connection succeeded"
  else
      info "Browser    => Workspace Agent ($AGENT_EXTERNAL_IP): Connection failed"
  fi

  ### TEST 2: Simulate Che server ==> workspace agent (external IP) connectivity
  export HTTP_CODE=$(docker_run --name fakeserver \
                                --entrypoint=curl \
                                $(eval "echo \${IMAGE_${CHE_PRODUCT_NAME}}") \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out '%{http_code}')

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Server     => Workspace Agent (External IP): Connection succeeded"
  else
      info "Server     => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 3: Simulate Che server ==> workspace agent (internal IP) connectivity
  export HTTP_CODE=$(docker_run --name fakeserver \
                                --entrypoint=curl \
                                $(eval "echo \${IMAGE_${CHE_PRODUCT_NAME}}") \
                                  -I ${AGENT_INTERNAL_IP}:${AGENT_INTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out '%{http_code}')

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Server     => Workspace Agent (Internal IP): Connection succeeded"
  else
      info "Server     => Workspace Agent (Internal IP): Connection failed"
  fi

  log "docker rm -f fakeagent >> \"${LOGS}\""
  docker rm -f fakeagent >> "${LOGS}"
}
