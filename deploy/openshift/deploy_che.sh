#!/bin/bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# This script is meant for quick & easy install of Che on OpenShift via:
#
#  ``` bash
#   DEPLOY_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/deploy/openshift/deploy_che.sh
#   curl -fsSL ${DEPLOY_SCRIPT_URL} -o get-che.sh
#   bash get-che.sh --wait-che
#   ```
#
# For more deployment options: https://www.eclipse.org/che/docs/setup/openshift/index.html

set -e

# --------------------------------------------------------
# Check pre-requisites
# --------------------------------------------------------
command -v oc >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool oc (https://docs.openshift.org/latest/cli_reference/get_started_cli.html) is required but it's not installed. Aborting."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool jq (https://stedolan.github.io/jq) is required but it's not installed. Aborting."; exit 1; }

# ----------------
# helper functions
# ----------------

# inject_che_config injects che configuration in ENV format in deploy config
# first arg is a marker string, second is a path to the file with parameters in KV format which will be inserted after marker
inject_che_config() {
    while IFS= read -r line
    do
      printf '%s\n' "$line"
      if [[ "$line" == *"$1"* ]];then
          while read l; do
            #ignore comments and empty lines
            if [[ "$l" != "#"* ]] && [[ ! -z "$l" ]]; then
                # properly extract key and value from config map file
                KEY=$(echo $l | cut -d ' ' -f1 | cut -d ':' -f1)
                VALUE=$(eval echo $l | cut -d ':' -f2- | cut -d ' ' -f2-)
                # put key and value in proper format in to a yaml file after marker line
                printf '%s\n' "          - name: $KEY"
                printf '%s\n' "            value: \"$VALUE\""
            fi
          done <$2
      fi
    done < /dev/stdin
}

wait_until_che_is_available() {
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

    DEPLOYMENT_TIMEOUT_SEC=300
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
    while [[ "${available}" != "\"True\"" || "${progressing}" != "\"True\"" ]] && [ ${SECONDS} -lt ${end} ]; do
      available=$(oc get dc che -o json | jq '.status.conditions[] | select(.type == "Available") | .status')
      progressing=$(oc get dc che -o json | jq '.status.conditions[] | select(.type == "Progressing") | .status')
      timeout_in=$((end-SECONDS))
      echo "[CHE] Deployment is in progress...(Available.status=${available}, Progressing.status=${progressing}, Timeout in ${timeout_in}s)"
      sleep ${POLLING_INTERVAL_SEC}
    done

    if [ "${progressing}" == "\"True\"" ]; then
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
      echo "[CHE] [ERROR] Che is not responding (HTTP status= ${che_http_status})"
      exit 1
    fi
}

# --------------
# Print Che logo
# --------------

echo
cat <<EOF
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;52m1[38;5;94m0[38;5;136m1[38;5;215m0[38;5;215m0[38;5;136m0[38;5;94m0[38;5;58m0[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;58m0[38;5;136m1[38;5;179m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;179m1[38;5;136m0[38;5;58m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [38;5;52m0[38;5;94m1[38;5;136m0[38;5;179m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;179m0[38;5;136m1[38;5;94m1[38;5;52m0[0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [38;5;58m1[38;5;136m1[38;5;179m0[38;5;215m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;215m0[38;5;179m0[38;5;100m0[38;5;58m1[0m [0m [0m 
[38;5;136m0[38;5;179m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;136m0[38;5;52m1
[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;215m0[38;5;179m0[38;5;179m0[38;5;215m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;215m1[38;5;179m1[38;5;100m1[38;5;58m0[0m [0m [0m 
[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;179m1[38;5;136m0[38;5;94m0[38;5;52m1[0m [0m [0m [0m [38;5;52m1[38;5;94m0[38;5;136m0[38;5;179m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;179m1[38;5;136m0[38;5;94m0[38;5;52m0[0m [0m [0m [0m [0m [0m [0m 
[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;215m1[38;5;179m0[38;5;136m0[38;5;58m0[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;58m0[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;179m1[38;5;136m0[38;5;94m1[38;5;52m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;59m1[38;5;60m1[38;5;60m0
[38;5;221m1[38;5;179m0[38;5;180m1[38;5;138m0[38;5;102m0[38;5;60m0[38;5;23m0[38;5;17m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m1[38;5;60m1[38;5;60m0[38;5;67m1[38;5;103m1[38;5;103m1[38;5;103m1[38;5;67m1
[38;5;103m1[38;5;67m1[38;5;61m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;103m1[38;5;103m1[38;5;67m1[38;5;60m0[38;5;60m1[38;5;23m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;23m1[38;5;60m1[38;5;60m1[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1
[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;103m0[38;5;103m1[38;5;103m1[38;5;67m0[38;5;60m1[38;5;59m0[38;5;17m1[0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;59m0[38;5;60m0[38;5;67m0[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1
[38;5;103m0[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;103m0[38;5;103m0[38;5;67m0[38;5;60m0[38;5;60m0[38;5;60m0[38;5;60m1[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;103m1
[38;5;59m1[38;5;60m1[38;5;67m0[38;5;67m1[38;5;103m0[38;5;103m0[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;103m0[38;5;103m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;103m0[38;5;103m1[38;5;67m1[38;5;67m0[38;5;60m1[38;5;59m1
[0m [0m [0m [0m [38;5;23m0[38;5;60m0[38;5;60m0[38;5;67m0[38;5;103m0[38;5;103m0[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;103m1[38;5;103m0[38;5;67m0[38;5;60m0[38;5;60m0[38;5;23m0[0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;59m0[38;5;60m0[38;5;67m1[38;5;103m1[38;5;103m0[38;5;103m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;103m0[38;5;103m0[38;5;103m1[38;5;67m1[38;5;60m1[38;5;60m1[38;5;17m0[0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m1[38;5;59m1[38;5;60m0[38;5;60m1[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m1[38;5;67m0[38;5;103m0[38;5;103m0[38;5;67m0[38;5;60m1[38;5;60m0[38;5;59m0[38;5;17m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;60m0[38;5;60m0[38;5;60m1[38;5;60m0[38;5;17m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m
EOF
echo

# --------------------------------------------------------
# Parse options
# --------------------------------------------------------
for key in "$@"
do
case $key in
    -c | --command)
    COMMAND="$2"
    shift
    ;;
    --wait-che)
    WAIT_FOR_CHE=true
    shift
    ;;
    *)
            # unknown option
    ;;
esac
done

# OPENSHIFT_FLAVOR can be minishift or osio or ocp
# TODO Set flavour via a parameter
DEFAULT_OPENSHIFT_FLAVOR="minishift"
OPENSHIFT_FLAVOR=${OPENSHIFT_FLAVOR:-${DEFAULT_OPENSHIFT_FLAVOR}}
DEFAULT_DNS_PROVIDER="nip.io"
DNS_PROVIDER=${DNS_PROVIDER:-${DEFAULT_DNS_PROVIDER}}
BASE_DIR=$(cd "$(dirname "$0")"; pwd)

# If OpenShift flavor is MiniShift check its availability
if [ "${OPENSHIFT_FLAVOR}" == "minishift" ]; then
  if [ -z "${MINISHIFT_IP}" ]; then
    # ---------------------------
    # Set minishift configuration
    # ---------------------------
    echo -n "[CHE] Checking if minishift is running..."
    minishift status | grep -q "Running" ||(echo "Minishift is not running. Aborting"; exit 1)
    echo "done!"
    MINISHIFT_IP="$(minishift ip)"
  fi
fi

# -----------------------------------------------
# Set defaults for different flavors of OpenShift
# -----------------------------------------------
if [ "${OPENSHIFT_FLAVOR}" == "minishift" ]; then
  # ----------------------
  # Set minishift configuration
  # ----------------------
  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=""
  DEFAULT_CHE_INFRA_KUBERNETES_PVC_STRATEGY="unique"
  DEFAULT_OPENSHIFT_ENDPOINT="https://${MINISHIFT_IP}:8443/"
  DEFAULT_OPENSHIFT_USERNAME="developer"
  DEFAULT_OPENSHIFT_PASSWORD="developer"
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  DEFAULT_OPENSHIFT_ROUTING_SUFFIX="${MINISHIFT_IP}.${DNS_PROVIDER}"
  DEFAULT_CHE_DEBUG_SERVER="true"
  DEFAULT_OC_SKIP_TLS="true"
  DEFAULT_CHE_APPLY_RESOURCE_QUOTAS="false"
  DEFAULT_IMAGE_PULL_POLICY="IfNotPresent"
  DEFAULT_ENABLE_SSL="false"
  DEFAULT_CHE_LOG_LEVEL="INFO"
  DEFAULT_CHE_PREDEFINED_STACKS_RELOAD="false"
  DEFAULT_CHE_INFRA_KUBERNETES_PVC_PRECREATE__SUBPATHS="true"
elif [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then
  # ----------------------
  # Set osio configuration
  # ----------------------
  if [ -z "${OPENSHIFT_TOKEN+x}" ]; then echo "[CHE] **ERROR** Env var OPENSHIFT_TOKEN is unset. You need to set it with your OSO token to continue. To retrieve your token: https://console.starter-us-east-2.openshift.com/console/command-line. Aborting"; exit 1; fi
  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_CHE_INFRA_KUBERNETES_PVC_STRATEGY="common"
  DEFAULT_OPENSHIFT_ENDPOINT="https://api.starter-us-east-2.openshift.com"
  DEFAULT_CHE_OPENSHIFT_PROJECT="$(oc get projects -o=custom-columns=NAME:.metadata.name --no-headers | grep "\\-che$")"
  DEFAULT_OPENSHIFT_ROUTING_SUFFIX="8a09.starter-us-east-2.openshiftapps.com"
  DEFAULT_CHE_DEBUG_SERVER="false"
  DEFAULT_OC_SKIP_TLS="false"
  DEFAULT_ENABLE_SSL="true"
  DEFAULT_CHE_LOG_LEVEL="INFO"
  DEFAULT_CHE_PREDEFINED_STACKS_RELOAD="true"
  DEFAULT_CHE_INFRA_KUBERNETES_PVC_PRECREATE__SUBPATHS="false"
elif [ "${OPENSHIFT_FLAVOR}" == "ocp" ]; then
  # ----------------------
  # Set ocp configuration
  # ----------------------
  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=""
  DEFAULT_CHE_INFRA_KUBERNETES_PVC_STRATEGY="unique"
  DEFAULT_OPENSHIFT_USERNAME="developer"
  DEFAULT_OPENSHIFT_PASSWORD="developer"
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  DEFAULT_CHE_DEBUG_SERVER="false"
  DEFAULT_OC_SKIP_TLS="false"
  DEFAULT_ENABLE_SSL="true"
  DEFAULT_CHE_LOG_LEVEL="INFO"
  DEFAULT_CHE_PREDEFINED_STACKS_RELOAD="true"
  DEFAULT_CHE_INFRA_KUBERNETES_PVC_PRECREATE__SUBPATHS="true"
fi

# --------------------------------------------------------
# Set configuration common to any flavor of OpenShift
# --------------------------------------------------------
CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
DEFAULT_COMMAND="deploy"
DEFAULT_CHE_MULTIUSER="false"
DEFAULT_CHE_IMAGE_REPO="docker.io/eclipse/che-server"
DEFAULT_CHE_IMAGE_TAG="nightly"
DEFAULT_CHE_KEYCLOAK_OSO_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token"
DEFAULT_KEYCLOAK_GITHUB_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/github/token"
DEFAULT_CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD="true"

COMMAND=${COMMAND:-${DEFAULT_COMMAND}}
WAIT_FOR_CHE=${WAIT_FOR_CHE:-"false"}
CHE_MULTIUSER=${CHE_MULTIUSER:-${DEFAULT_CHE_MULTIUSER}}
if [ "${CHE_MULTIUSER}" == "true" ]; then
  CHE_DEDICATED_KEYCLOAK=${CHE_DEDICATED_KEYCLOAK:-"true"}
else
  CHE_DEDICATED_KEYCLOAK="false"
fi

OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
if [ -z "${OPENSHIFT_TOKEN+x}" ]; then
  OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}
  OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}
fi

CHE_OAUTH_GITHUB_CLIENTID=${CHE_OAUTH_GITHUB_CLIENTID:-}
CHE_OAUTH_GITHUB_CLIENTSECRET=${CHE_OAUTH_GITHUB_CLIENTSECRET:-}
CHE_INFRA_OPENSHIFT_PROJECT=${CHE_INFRA_OPENSHIFT_PROJECT:-${DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT}}
if [ -z ${CHE_INFRA_KUBERNETES_USERNAME+x} ]; then CHE_INFRA_KUBERNETES_USERNAME=$OPENSHIFT_USERNAME; fi
if [ -z ${CHE_INFRA_KUBERNETES_PASSWORD+x} ]; then CHE_INFRA_KUBERNETES_PASSWORD=$OPENSHIFT_PASSWORD; fi
if [ -z ${CHE_INFRA_KUBERNETES_OAUTH__TOKEN+x} ]; then CHE_INFRA_KUBERNETES_OAUTH__TOKEN=$OPENSHIFT_TOKEN; fi
CHE_INFRA_KUBERNETES_PVC_PRECREATE__SUBPATHS=${CHE_INFRA_KUBERNETES_PVC_PRECREATE__SUBPATHS:-${DEFAULT_CHE_INFRA_KUBERNETES_PVC_PRECREATE__SUBPATHS}}

OPENSHIFT_ROUTING_SUFFIX=${OPENSHIFT_ROUTING_SUFFIX:-${DEFAULT_OPENSHIFT_ROUTING_SUFFIX}}
DEFAULT_OPENSHIFT_NAMESPACE_URL="${CHE_OPENSHIFT_PROJECT}.${OPENSHIFT_ROUTING_SUFFIX}"
OPENSHIFT_NAMESPACE_URL=${OPENSHIFT_NAMESPACE_URL:-${DEFAULT_OPENSHIFT_NAMESPACE_URL}}

CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}
ENABLE_SSL=${ENABLE_SSL:-${DEFAULT_ENABLE_SSL}}
WORKSPACE_MEMORY_REQUEST=${WORKSPACE_MEMORY_REQUEST:-${DEFAULT_WORKSPACE_MEMORY_REQUEST}}
CHE_PREDEFINED_STACKS_RELOAD=${CHE_PREDEFINED_STACKS_RELOAD:-${DEFAULT_CHE_PREDEFINED_STACKS_RELOAD}}
CHE_DEBUG_SERVER=${CHE_DEBUG_SERVER:-${DEFAULT_CHE_DEBUG_SERVER}}
OC_SKIP_TLS=${OC_SKIP_TLS:-${DEFAULT_OC_SKIP_TLS}}
CHE_APPLY_RESOURCE_QUOTAS=${CHE_APPLY_RESOURCE_QUOTAS:-${DEFAULT_CHE_APPLY_RESOURCE_QUOTAS}}
IMAGE_PULL_POLICY=${IMAGE_PULL_POLICY:-${DEFAULT_IMAGE_PULL_POLICY}}
CHE_INFRA_KUBERNETES_PVC_STRATEGY=${CHE_INFRA_KUBERNETES_PVC_STRATEGY:-${DEFAULT_CHE_INFRA_KUBERNETES_PVC_STRATEGY}}
CHE_HOST="${OPENSHIFT_NAMESPACE_URL}"
if [ "${ENABLE_SSL}" == "true" ]; then
    HTTP_PROTOCOL="https"
    WS_PROTOCOL="wss"
else
    HTTP_PROTOCOL="http"
    WS_PROTOCOL="ws"
fi
export IMAGE_POSTGRES=${IMAGE_POSTGRES:-"eclipse/che-postgres:nightly"}
export IMAGE_KEYCLOAK=${IMAGE_KEYCLOAK:-"eclipse/che-keycloak:nightly"}
CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}
CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}
CHE_IMAGE="${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}"
# Escape slashes in CHE_IMAGE to use it with sed later
# e.g. docker.io/rhchestage => docker.io\/rhchestage
CHE_IMAGE_SANITIZED=$(echo "${CHE_IMAGE}" | sed 's/\//\\\//g')
# Keycloak production endpoints are used by default
CHE_KEYCLOAK_OSO_ENDPOINT=${CHE_KEYCLOAK_OSO_ENDPOINT:-${DEFAULT_CHE_KEYCLOAK_OSO_ENDPOINT}}
KEYCLOAK_GITHUB_ENDPOINT=${KEYCLOAK_GITHUB_ENDPOINT:-${DEFAULT_KEYCLOAK_GITHUB_ENDPOINT}}

CHE_MASTER_PVC="\
- apiVersion: v1\n \
 kind: PersistentVolumeClaim\n \
 metadata:\n \
   labels:\n \
     app: che\n \
   name: che-data-volume\n \
 spec:\n \
   accessModes:\n \
   - ReadWriteOnce\n \
   resources:\n \
     requests:\n \
       storage: 1Gi"

CHE_MASTER_VOLUME_MOUNTS="\
- mountPath: /data\n \
           name: che-data-volume"

CHE_MASTER_VOLUMES="\
- name: che-data-volume\n \
         persistentVolumeClaim:\n \
           claimName: che-data-volume"

get_che_pod_config() {
DEFAULT_CHE_DEPLOYMENT_FILE_PATH=${BASE_DIR}/che-openshift.yml
CHE_DEPLOYMENT_FILE_PATH=${CHE_DEPLOYMENT_FILE_PATH:-${DEFAULT_CHE_DEPLOYMENT_FILE_PATH}}
DEFAULT_CHE_CONFIG_FILE_PATH=${BASE_DIR}/che-config
CHE_CONFIG_FILE_PATH=${CHE_CONFIG_FILE_PATH:-${DEFAULT_CHE_CONFIG_FILE_PATH}}
cat "${CHE_DEPLOYMENT_FILE_PATH}" | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/          imagePullPolicy:.*/          imagePullPolicy: \"${IMAGE_PULL_POLICY}\"/" | \
    if [[ "${CHE_MULTIUSER}" != "true" ]]; then
    sed "s|#CHE_MASTER_PVC|$CHE_MASTER_PVC|" | \
    sed "s|#CHE_MASTER_VOLUME_MOUNTS.*|$CHE_MASTER_VOLUME_MOUNTS|" | \
    sed "s|#CHE_MASTER_VOLUMES.*|$CHE_MASTER_VOLUMES|";else cat -; fi | \
    inject_che_config "#CHE_MASTER_CONFIG" "${CHE_CONFIG_FILE_PATH}"
}

# ---------------------------------------
# Verify that we have all env var are set
# ---------------------------------------
if ([ -z "${OPENSHIFT_USERNAME+x}" ] ||
    [ -z "${OPENSHIFT_PASSWORD+x}" ]) &&
    [ -z "${OPENSHIFT_TOKEN+x}" ]; then echo "[CHE] **ERROR** Env var OPENSHIFT_USERNAME, OPENSHIFT_PASSWORD and OPENSHIFT_TOKEN are unset. You need to set username/password or token to continue. Aborting"; exit 1; fi

if [ -z "${OPENSHIFT_ENDPOINT+x}" ]; then echo "[CHE] **ERROR**Env var OPENSHIFT_ENDPOINT is unset. You need to set it to continue. Aborting"; exit 1; fi
if [ -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then echo "[CHE] **ERROR**Env var OPENSHIFT_NAMESPACE_URL is unset. You need to set it to continue. Aborting"; exit 1; fi

# -----------------------------------
# Logging on to the OpenShift cluster
# -----------------------------------
echo -n "[CHE] Logging on using OpenShift endpoint \"${OPENSHIFT_ENDPOINT}\"..."
if [ -z "${OPENSHIFT_TOKEN+x}" ]; then
  oc login "${OPENSHIFT_ENDPOINT}" --insecure-skip-tls-verify="${OC_SKIP_TLS}" -u "${OPENSHIFT_USERNAME}" -p "${OPENSHIFT_PASSWORD}" > /dev/null
  OPENSHIFT_TOKEN=$(oc whoami -t)
else
  oc login "${OPENSHIFT_ENDPOINT}" --insecure-skip-tls-verify="${OC_SKIP_TLS}" --token="${OPENSHIFT_TOKEN}"  > /dev/null
fi
echo "done!"

# -------------------------------------------------------------
# If command == cleanup then delete all openshift objects
# -------------------------------------------------------------
if [ "${COMMAND}" == "cleanup" ]; then
  echo "[CHE] Deleting all OpenShift objects..."
  oc delete all --all
  echo "[CHE] Cleanup successfully started. Use \"oc get all\" to verify that all resources have been deleted."
  exit 0
# -------------------------------------------------------------
# If command == rollupdate then update Che
# -------------------------------------------------------------
elif [ "${COMMAND}" == "rollupdate" ]; then
  echo "[CHE] Update CHE pod"
  get_che_pod_config | oc apply -f -
  echo "[CHE] Update successfully started"
  exit 0
# ----------------------------------------------------------------
# At this point command should be "deploy" otherwise it's an error
# ----------------------------------------------------------------
elif [ "${COMMAND}" != "deploy" ]; then
  echo "[CHE] **ERROR**: Command \"${COMMAND}\" is not a valid command. Aborting."
  exit 1
fi

# --------------------------
# Create project (if needed)
# --------------------------
echo -n "[CHE] Checking if project \"${CHE_OPENSHIFT_PROJECT}\" exists..."
if ! oc get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then

    if [ "${COMMAND}" == "cleanup" ] || [ "${COMMAND}" == "rollupdate" ]; then echo "**ERROR** project doesn't exist. Aborting"; exit 1; fi
    if [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then echo "**ERROR** project doesn't exist on OSIO. Aborting"; exit 1; fi

    # OpenShift will not get project but project still exists for a period after being deleted.
    # The following will loop until it can create successfully.

    WAIT_FOR_PROJECT_TO_DELETE=true
    WAIT_FOR_PROJECT_TO_DELETE_MESSAGE="Waiting for project to be deleted fully(~15 seconds)..."

    echo "Project \"${CHE_OPENSHIFT_PROJECT}\" does not exist...trying to create it."
    DEPLOYMENT_TIMEOUT_SEC=120
    POLLING_INTERVAL_SEC=2
    timeout_in=$((POLLING_INTERVAL_SEC+DEPLOYMENT_TIMEOUT_SEC))
    while $WAIT_FOR_PROJECT_TO_DELETE
    do
    { # try
        timeout_in=$((timeout_in-POLLING_INTERVAL_SEC))
        if [ "$timeout_in" -le "0" ] ; then
            echo "[CHE] **ERROR**: Timeout of $DEPLOYMENT_TIMEOUT_SEC waiting for project \"${CHE_OPENSHIFT_PROJECT}\" to be deleted."
            exit 1
        fi
        oc new-project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null && \
        WAIT_FOR_PROJECT_TO_DELETE=false # Only excutes if project creation is successfully
    } || { # catch
        echo -n $WAIT_FOR_PROJECT_TO_DELETE_MESSAGE
        WAIT_FOR_PROJECT_TO_DELETE_MESSAGE="."
        sleep $POLLING_INTERVAL_SEC
    }
    done
    echo "Project \"${CHE_OPENSHIFT_PROJECT}\" creation done!"
else
    echo "Project \"${CHE_OPENSHIFT_PROJECT}\" already exists. Please remove project before running this script."
    exit 1
fi

# -------------------------------------------------------------
# create CHE service and route
# -------------------------------------------------------------
echo "[CHE] Creating serviceaccount, service and route for CHE pod"
 echo "apiVersion: v1
kind: List
items:
- apiVersion: v1
  kind: ServiceAccount
  metadata:
    labels:
      app: che
    name: che
- apiVersion: v1
  kind: Service
  metadata:
    labels:
      app: che
    name: che-host
  spec:
    ports:
    - name: http
      port: 8080
      protocol: TCP
      targetPort: 8080
    selector:
      app: che
- apiVersion: v1
  kind: Route
  metadata:
    labels:
      app: che
    name: che
  spec:
    tls:
      insecureEdgeTerminationPolicy: Redirect
      termination: edge
    to:
      kind: Service
      name: che-host" | \
if [ "${ENABLE_SSL}" == "false" ]; then grep -v -e "tls:" -e "insecureEdgeTerminationPolicy: Redirect" -e "termination: edge" ; else cat -; fi | \
oc apply -f -

# -------------------------------------------------------------
# Deploying secondary servers
# for postgres and optionally Keycloak
# -------------------------------------------------------------

if [[ "${CHE_MULTIUSER}" == "true" ]] && [[ "${COMMAND}" == "deploy" ]]; then
    if [ "${CHE_DEDICATED_KEYCLOAK}" == "true" ]; then
        "${BASE_DIR}"/multi-user/deploy_postgres_and_keycloak.sh
        "${BASE_DIR}"/multi-user/configure_keycloak.sh
    else
        "${BASE_DIR}"/multi-user/deploy_postgres_only.sh
    fi
fi

# -------------------------------------------------------------
# Setting Keycloak-related environment variables
# Done here since the Openshift project should be available
# TODO Maybe this should go into a config map, but I don't know
# How we would manage the retrieval of the Keycloak route
# external URL.
# -------------------------------------------------------------

if [ "${CHE_DEDICATED_KEYCLOAK}" == "true" ]; then
  CHE_KEYCLOAK_SERVER_ROUTE=$(oc get route keycloak -o jsonpath='{.spec.host}' || echo "")
  if [ "${CHE_KEYCLOAK_SERVER_ROUTE}" == "" ]; then
    echo "[CHE] **ERROR**: The dedicated Keycloak server should be deployed and visible through a route before starting the Che server"
    exit 1
  fi

  CHE_POSTRES_SERVICE=$(oc get service postgres || echo "")
  if [ "${CHE_POSTRES_SERVICE}" == "" ]; then
    echo "[CHE] **ERROR**: The dedicated Postgres server should be started in Openshift project ${CHE_OPENSHIFT_PROJECT} before starting the Che server"
    exit 1
  fi

  CHE_KEYCLOAK_AUTH__SERVER__URL=${CHE_KEYCLOAK_AUTH__SERVER__URL:-"${HTTP_PROTOCOL}://${CHE_KEYCLOAK_SERVER_ROUTE}/auth"}
  CHE_KEYCLOAK_REALM=${CHE_KEYCLOAK_REALM:-"che"}
  CHE_KEYCLOAK_CLIENT__ID=${CHE_KEYCLOAK_CLIENT__ID:-"che-public"}
else
  CHE_KEYCLOAK_AUTH__SERVER__URL=${CHE_KEYCLOAK_AUTH__SERVER__URL:-"https://sso.openshift.io/auth"}
  CHE_KEYCLOAK_REALM=${CHE_KEYCLOAK_REALM:-"fabric8"}
  CHE_KEYCLOAK_CLIENT__ID=${CHE_KEYCLOAK_CLIENT__ID:-"openshiftio-public"}
fi

# -------------------------------------------------------------
# Verify that Che ServiceAccount has admin rights at project level
# -------------------------------------------------------------
## TODO we should create Che SA if it doesn't exist
## TODO we should check if che has admin rights before creating the role biding
## TODO if we are not in minishift we should fail if che SA doesn't have admin rights
if [[ "${OPENSHIFT_FLAVOR}" =~ ^(minishift|ocp)$ ]]; then
  echo -n "[CHE] Setting admin role to \"che\" service account..."
  echo "apiVersion: v1
kind: RoleBinding
metadata:
  name: che
roleRef:
  name: admin
subjects:
- kind: ServiceAccount
  name: che" | oc apply -f -
fi

# ----------------------------------------------
# Get latest version of fabric8 tenant templates
# ----------------------------------------------
# TODO make it possible to use a local Che template instead of always downloading it from maven central
echo -n "[CHE] Retrieving latest version of fabric8 tenant Che template..."
OSIO_VERSION=$(curl -sSL http://central.maven.org/maven2/io/fabric8/tenant/apps/che/maven-metadata.xml | grep latest | sed -e 's,.*<latest>\([^<]*\)</latest>.*,\1,g')
echo "done! (v.${OSIO_VERSION})"

# --------------------------------------
# Applying resource quotas on minishift
# --------------------------------------
if [ "${CHE_APPLY_RESOURCE_QUOTAS}" == "true" ] && [ "${OPENSHIFT_FLAVOR}" == "minishift" ]; then
 # Only cluster admin can set limitranges / resourcequotas
 oc login "${OPENSHIFT_ENDPOINT}" -u system:admin &> /dev/null
 echo "[CHE] Applying resource quotas for ${CHE_OPENSHIFT_PROJECT}"
 curl -sSL http://central.maven.org/maven2/io/fabric8/tenant/packages/fabric8-tenant-che-quotas-oso/"${OSIO_VERSION}"/fabric8-tenant-che-quotas-oso-"${OSIO_VERSION}"-openshift.yml |
 oc apply --force=true -f-
 echo "[CHE] Resource quotas have been successfully applied"
 oc login "${OPENSHIFT_ENDPOINT}" --token="${OPENSHIFT_TOKEN}"  &> /dev/null
fi

# ----------------------------------------------
# Start the deployment
# ----------------------------------------------
echo
echo "[CHE] Deploying Che on ${OPENSHIFT_FLAVOR} (image ${CHE_IMAGE})"
get_che_pod_config | oc apply --force=true -f -
echo

# --------------------------------
# Setup debugging routes if needed
# --------------------------------
if [ "${CHE_DEBUG_SERVER}" == "true" ]; then

  if oc get svc che-debug &> /dev/null; then
    echo -n "[CHE] Deleting old che-debug service..."
    oc delete svc che-debug
    echo "done"
  fi

  echo -n "[CHE] Creating an OS route to debug Che wsmaster..."
  oc expose dc che --name=che-debug --target-port=http-debug --port=8000 --type=NodePort
  NodePort=$(oc get service che-debug -o jsonpath='{.spec.ports[0].nodePort}')
  echo "[CHE] Remote wsmaster debugging URL: ${MINISHIFT_IP}:${NodePort}"
fi

if [ "${WAIT_FOR_CHE}" == "true" ]; then
  wait_until_che_is_available
fi

che_route=$(oc get route che -o jsonpath='{.spec.host}')
echo
echo "[CHE] Che deployment has been successufully bootstrapped"
echo "[CHE] -> To check OpenShift deployment logs: 'oc get events -w'"
echo "[CHE] -> To check Che server logs: 'oc logs -f dc/che'"
echo "[CHE] -> Once the deployment is completed Che will be available at: "
echo "[CHE]    ${HTTP_PROTOCOL}://${che_route}"
echo
echo
