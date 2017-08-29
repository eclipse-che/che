#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

echo "[CHE] This script is going to wait until Che is deployed and available"

command -v oc >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool oc (https://docs.openshift.org/latest/cli_reference/get_started_cli.html) is required but it's not installed. Aborting."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool jq (https://stedolan.github.io/jq) is required but it's not installed. Aborting."; exit 1; }

if [ -z "${CHE_API_ENDPOINT+x}" ]; then
    echo -n "[CHE] Inferring \$CHE_API_ENDPOINT..."
    che_host=$(oc get route che -o jsonpath='{.spec.host}')
    if [ -z "${che_host}" ]; then echo >&2 "[CHE] [ERROR] Failed to infer environment variable \$CHE_API_ENDPOINT. Aborting. Please set it and run ${0} script again."; exit 1; fi
    if [[ $(oc get route che -o jsonpath='{.spec.tls}') ]]; then protocol="https" ; else protocol="http"; fi
    CHE_API_ENDPOINT="${protocol}://${che_host}/api"
    echo "done (${CHE_API_ENDPOINT})"
fi

available=$(oc get dc che -o json | jq '.status.conditions[] | select(.type == "Available") | .status')
progressing=$(oc get dc che -o json | jq '.status.conditions[] | select(.type == "Progressing") | .status')

DEPLOYMENT_TIMEOUT_SEC=120
POLLING_INTERVAL_SEC=5
end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
while [ "${available}" != "\"True\"" ] && [ ${SECONDS} -lt ${end} ]; do
  available=$(oc get dc che -o json | jq '.status.conditions[] | select(.type == "Available") | .status')
  progressing=$(oc get dc che -o json | jq '.status.conditions[] | select(.type == "Progressing") | .status')
  timeout_in=$((end-SECONDS))
  echo "[CHE] Deployment is in progress...(Available.status=${available}, Progressing.status=${progressing}, Timeout in ${timeout_in}s)"
  sleep ${POLLING_INTERVAL_SEC}
done

if [ "${progressing}" == "\"True\"" ] && [ "${available}" == "\"True\"" ]; then
  echo "[CHE] Che deployed successfully"
elif [ "${progressing}" == "False" ]; then
  echo "[CHE] [ERROR] Che deployment failed. Aborting. Run command 'oc rollout status che' to get more details."
  exit 1
elif [ ${SECONDS} -lt ${end} ]; then
  echo "[CHE] [ERROR] Deployment timeout. Aborting."
  exit 1
fi

che_http_status=$(curl -s -o /dev/null -I -w "%{http_code}" "${CHE_API_ENDPOINT}/system/state")
if [ "${che_http_status}" == "200" ]; then  
  echo "[CHE] Che is up and running"
else
  echo "[CHE] [ERROR] Che is not reponding (HTTP status= ${che_http_status})"
  exit 1
fi

echo
echo
