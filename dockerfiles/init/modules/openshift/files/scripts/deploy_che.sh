#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# This script is meant for quick & easy install of Che on OpenShift via:
#
#  ``` bash
#   DEPLOY_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/deploy_che.sh
#   curl -fsSL ${DEPLOY_SCRIPT_URL} -o get-che.sh
#   WAIT_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/wait_until_che_is_available.sh
#   curl -fsSL ${WAIT_SCRIPT_URL} -o wait-che.sh
#   STACKS_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/replace_stacks.sh
#   curl -fsSL ${STACKS_SCRIPT_URL} -o stacks-che.sh
#   bash get-che.sh && wait-che.sh && stacks-che.sh
#   ```
#
# For more deployment options: https://www.eclipse.org/che/docs/setup/openshift/index.html

set -e

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
# Check pre-requisites
# --------------------------------------------------------
command -v oc >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool oc (https://docs.openshift.org/latest/cli_reference/get_started_cli.html) is required but it's not installed. Aborting."; exit 1; }

# --------------------------------------------------------
# Parse options
# --------------------------------------------------------
while [[ $# -gt 1 ]]
do
key="$1"
case $key in
    -c | --command)
    COMMAND="$2"
    shift
    ;;
    *)
            # unknown option
    ;;
esac
shift
done

# OPENSHIFT_FLAVOR can be minishift or osio or ocp
# TODO Set flavour via a parameter
DEFAULT_OPENSHIFT_FLAVOR="minishift"
OPENSHIFT_FLAVOR=${OPENSHIFT_FLAVOR:-${DEFAULT_OPENSHIFT_FLAVOR}}
DEFAULT_DNS_PROVIDER="nip.io"
DNS_PROVIDER=${DNS_PROVIDER:-${DEFAULT_DNS_PROVIDER}}

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
  DEFAULT_CHE_INFRA_OPENSHIFT_PVC_STRATEGY="unique"
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
  DEFAULT_CHE_INFRA_OPENSHIFT_PVC_PRECREATE__SUBPATHS="true"
elif [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then
  # ----------------------
  # Set osio configuration
  # ----------------------
  if [ -z "${OPENSHIFT_TOKEN+x}" ]; then echo "[CHE] **ERROR** Env var OPENSHIFT_TOKEN is unset. You need to set it with your OSO token to continue. To retrieve your token: https://console.starter-us-east-2.openshift.com/console/command-line. Aborting"; exit 1; fi
  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_CHE_INFRA_OPENSHIFT_PVC_STRATEGY="common"
  DEFAULT_CHE_INFRA_OPENSHIFT_USERNAME=""
  DEFAULT_CHE_INFRA_OPENSHIFT_PASSWORD=""
  DEFAULT_OPENSHIFT_ENDPOINT="https://api.starter-us-east-2.openshift.com"
  DEFAULT_CHE_OPENSHIFT_PROJECT="$(oc get projects -o=custom-columns=NAME:.metadata.name --no-headers | grep "\\-che$")"
  DEFAULT_OPENSHIFT_ROUTING_SUFFIX="8a09.starter-us-east-2.openshiftapps.com"
  DEFAULT_CHE_DEBUG_SERVER="false"
  DEFAULT_OC_SKIP_TLS="false"
  DEFAULT_ENABLE_SSL="true"
  DEFAULT_CHE_LOG_LEVEL="INFO"
  DEFAULT_CHE_PREDEFINED_STACKS_RELOAD="true"
  DEFAULT_CHE_INFRA_OPENSHIFT_PVC_PRECREATE__SUBPATHS="false"
elif [ "${OPENSHIFT_FLAVOR}" == "ocp" ]; then
  # ----------------------
  # Set ocp configuration
  # ----------------------
  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=""
  DEFAULT_CHE_INFRA_OPENSHIFT_PVC_STRATEGY="unique"
  DEFAULT_OPENSHIFT_USERNAME="developer"
  DEFAULT_OPENSHIFT_PASSWORD="developer"
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  DEFAULT_CHE_DEBUG_SERVER="false"
  DEFAULT_OC_SKIP_TLS="false"
  DEFAULT_ENABLE_SSL="true"
  DEFAULT_CHE_LOG_LEVEL="INFO"
  DEFAULT_CHE_PREDEFINED_STACKS_RELOAD="true"
  DEFAULT_CHE_INFRA_OPENSHIFT_PVC_PRECREATE__SUBPATHS="true"
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

COMMAND=${COMMAND:-${DEFAULT_COMMAND}}
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
CHE_INFRA_OPENSHIFT_USERNAME=${CHE_INFRA_OPENSHIFT_USERNAME:-${OPENSHIFT_USERNAME}}
CHE_INFRA_OPENSHIFT_PASSWORD=${CHE_INFRA_OPENSHIFT_PASSWORD:-${OPENSHIFT_PASSWORD}}
CHE_INFRA_OPENSHIFT_OAUTH__TOKEN=${CHE_INFRA_OPENSHIFT_OAUTH__TOKEN:-${OPENSHIFT_TOKEN}}
CHE_INFRA_OPENSHIFT_PVC_PRECREATE__SUBPATHS=${CHE_INFRA_OPENSHIFT_PVC_PRECREATE__SUBPATHS:-${DEFAULT_CHE_INFRA_OPENSHIFT_PVC_PRECREATE__SUBPATHS}}

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
CHE_INFRA_OPENSHIFT_PVC_STRATEGY=${CHE_INFRA_OPENSHIFT_PVC_STRATEGY:-${DEFAULT_CHE_INFRA_OPENSHIFT_PVC_STRATEGY}}
CHE_HOST="${OPENSHIFT_NAMESPACE_URL}"
if [ "${ENABLE_SSL}" == "true" ]; then
    HTTP_PROTOCOL="https"
    WS_PROTOCOL="wss"
else
    HTTP_PROTOCOL="http"
    WS_PROTOCOL="ws"
fi
CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}
CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}
CHE_IMAGE="${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}"
# Escape slashes in CHE_IMAGE to use it with sed later
# e.g. docker.io/rhchestage => docker.io\/rhchestage
CHE_IMAGE_SANITIZED=$(echo "${CHE_IMAGE}" | sed 's/\//\\\//g')
# Keycloak production endpoints are used by default
CHE_KEYCLOAK_OSO_ENDPOINT=${CHE_KEYCLOAK_OSO_ENDPOINT:-${DEFAULT_CHE_KEYCLOAK_OSO_ENDPOINT}}
KEYCLOAK_GITHUB_ENDPOINT=${KEYCLOAK_GITHUB_ENDPOINT:-${DEFAULT_KEYCLOAK_GITHUB_ENDPOINT}}

get_che_pod_config() {
DEFAULT_CHE_DEPLOYMENT_FILE_PATH=./che-openshift.yml
CHE_DEPLOYMENT_FILE_PATH=${CHE_DEPLOYMENT_FILE_PATH:-${DEFAULT_CHE_DEPLOYMENT_FILE_PATH}}
DEFAULT_CHE_CONFIG_FILE_PATH=./che-config
CHE_CONFIG_FILE_PATH=${CHE_CONFIG_FILE_PATH:-${DEFAULT_CHE_CONFIG_FILE_PATH}}
cat "${CHE_DEPLOYMENT_FILE_PATH}" | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/          imagePullPolicy:.*/          imagePullPolicy: \"${IMAGE_PULL_POLICY}\"/" | \
    inject_che_config "#CHE_MASTER_CONFIG" "${CHE_CONFIG_FILE_PATH}" | \
    if [ "${ENABLE_SSL}" == "false" ]; then grep -v -e "tls:" -e "insecureEdgeTerminationPolicy: Redirect" -e "termination: edge" ; else cat -; fi #| \
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

# --------------------------
# Create project (if needed)
# --------------------------
echo -n "[CHE] Checking if project \"${CHE_OPENSHIFT_PROJECT}\" exists..."
if ! oc get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then

  if [ "${COMMAND}" == "cleanup" ] || [ "${COMMAND}" == "rollupdate" ]; then echo "**ERROR** project doesn't exist. Aborting"; exit 1; fi
  if [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then echo "**ERROR** project doesn't exist on OSIO. Aborting"; exit 1; fi

  echo -n "Project does not exist...creating it..."
  oc new-project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
fi
echo "done!"

echo -n "[CHE] Switching to \"${CHE_OPENSHIFT_PROJECT}\"..."
oc project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
echo "done!"

# -------------------------------------------------------------
# Deploying secondary servers
# for postgres and optionally Keycloak
# -------------------------------------------------------------

COMMAND_DIR=$(dirname "$0")

if [[ "${CHE_MULTIUSER}" == "true" ]] && [[ "${COMMAND}" == "deploy" ]]; then
    if [ "${CHE_DEDICATED_KEYCLOAK}" == "true" ]; then
        "${COMMAND_DIR}"/multi-user/deploy_postgres_and_keycloak.sh
    else
        "${COMMAND_DIR}"/multi-user/deploy_postgres_only.sh
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

if [ "${CHE_DEDICATED_KEYCLOAK}" == "true" ]; then
  "${COMMAND_DIR}"/multi-user/configure_and_start_keycloak.sh
fi

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

che_route=$(oc get route che -o jsonpath='{.spec.host}')
echo
echo "[CHE] Che deployment has been successufully bootstrapped"
echo "[CHE] -> To check OpenShift deployment logs: 'oc get events -w'"
echo "[CHE] -> To check Che server logs: 'oc logs -f dc/che'"
echo "[CHE] -> Once the deployment is completed Che will be available at: "
echo "[CHE]    ${HTTP_PROTOCOL}://${che_route}"
echo
echo
