#!/bin/bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

set -e

echo "[CHE] This script is going to wait until Keycloak is deployed and available"

command -v oc >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool oc (https://docs.openshift.org/latest/cli_reference/get_started_cli.html) is required but it's not installed. Aborting."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool jq (https://stedolan.github.io/jq) is required but it's not installed. Aborting."; exit 1; }

echo "[CHE] Wait for Keycloak pod booting..."
available=$(oc get dc keycloak -o json | jq ".status.conditions[] | select(.type == \"Available\") | .status")
progressing=$(oc get dc keycloak -o json | jq ".status.conditions[] | select(.type == \"Progressing\") | .status")

DEPLOYMENT_TIMEOUT_SEC=1200
POLLING_INTERVAL_SEC=5
end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
while [[ "${available}" != "\"True\"" || "${progressing}" != "\"True\"" ]] && [ ${SECONDS} -lt ${end} ]; do
    available=$(oc get dc keycloak -o json | jq ".status.conditions[] | select(.type == \"Available\") | .status")
    progressing=$(oc get dc keycloak -o json | jq ".status.conditions[] | select(.type == \"Progressing\") | .status")
    timeout_in=$((end-SECONDS))
    echo "[CHE] Deployment is in progress...(Available.status=${available}, Progressing.status=${progressing}, Timeout in ${timeout_in}s)"
    sleep ${POLLING_INTERVAL_SEC}
done

if [ "${progressing}" == "\"True\"" ]; then
    echo "[CHE] Keycloak deployed successfully"
elif [ "${progressing}" == "False" ]; then
    echo "[CHE] [ERROR] Keycloak deployment failed. Aborting. Run command 'oc rollout status keycloak' to get more details."
elif [ ${SECONDS} -ge ${end} ]; then
    echo "[CHE] [ERROR] Deployment timeout. Aborting."
    exit 1
fi
