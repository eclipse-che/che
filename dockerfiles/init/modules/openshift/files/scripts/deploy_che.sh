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
#   DEPLOY_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/spi/dockerfiles/cli/scripts/openshift/deploy_che.sh
#   curl -fsSL ${DEPLOY_SCRIPT_URL} -o get-che.sh
#   WAIT_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/spi/dockerfiles/cli/scripts/openshift/wait_until_che_is_available.sh
#   curl -fsSL ${WAIT_SCRIPT_URL} -o wait-che.sh
#   STACKS_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/spi/dockerfiles/cli/scripts/openshift/replace_stacks.sh
#   curl -fsSL ${STACKS_SCRIPT_URL} -o stacks-che.sh
#   bash get-che.sh && wait-che.sh && stacks-che.sh
#   ```
#
# For more deployment options: https://www.eclipse.org/che/docs/setup/openshift/index.html

set -e

# ----------------
# helper functions
# ----------------

# append_after_match allows to append content after matching line
# this is needed to append content of yaml files
# first arg is mathing string, second string to insert after match
append_after_match() {
    while IFS= read -r line
    do
      printf '%s\n' "$line"
      if [[ "$line" == *"$1"* ]];then
          printf '%s\n' "$2"
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

# --------------------------------------------------------
# Set configuration common to both minishift and openshift
# --------------------------------------------------------

CHE_MULTI_USER=${CHE_MULTI_USER:-"false"}
DEFAULT_COMMAND="deploy"
COMMAND=${COMMAND:-${DEFAULT_COMMAND}}

if [ "${CHE_MULTI_USER}" == "true" ]; then
  DEFAULT_CHE_KEYCLOAK_DISABLED="false"
  CHE_DEDICATED_KEYCLOAK=${CHE_DEDICATED_KEYCLOAK:-"true"}
  DEFAULT_CHE_IMAGE_REPO="docker.io/eclipse/che-server-multiuser"
else
  DEFAULT_CHE_KEYCLOAK_DISABLED="true"
  CHE_DEDICATED_KEYCLOAK="false"
  DEFAULT_CHE_IMAGE_REPO="docker.io/eclipse/che-server"
fi

CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}
DEFAULT_CHE_IMAGE_TAG="spi"
CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}
DEFAULT_CHE_LOG_LEVEL="INFO"
CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}
DEFAULT_ENABLE_SSL="true"
ENABLE_SSL=${ENABLE_SSL:-${DEFAULT_ENABLE_SSL}}
DEFAULT_K8S_VERSION_PRIOR_TO_1_6="true"
K8S_VERSION_PRIOR_TO_1_6=${K8S_VERSION_PRIOR_TO_1_6:-${DEFAULT_K8S_VERSION_PRIOR_TO_1_6}}
if [ "${ENABLE_SSL}" == "true" ]; then
    HTTP_PROTOCOL="https"
    WS_PROTOCOL="wss"
else
    HTTP_PROTOCOL="http"
    WS_PROTOCOL="ws"
fi
# Keycloak production endpoints are used by default
DEFAULT_KEYCLOAK_OSO_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token"
KEYCLOAK_OSO_ENDPOINT=${KEYCLOAK_OSO_ENDPOINT:-${DEFAULT_KEYCLOAK_OSO_ENDPOINT}}
DEFAULT_KEYCLOAK_GITHUB_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/github/token"
KEYCLOAK_GITHUB_ENDPOINT=${KEYCLOAK_GITHUB_ENDPOINT:-${DEFAULT_KEYCLOAK_GITHUB_ENDPOINT}}

# OPENSHIFT_FLAVOR can be minishift or openshift
# TODO Set flavour via a parameter
DEFAULT_OPENSHIFT_FLAVOR=minishift
OPENSHIFT_FLAVOR=${OPENSHIFT_FLAVOR:-${DEFAULT_OPENSHIFT_FLAVOR}}

# TODO move this env variable as a config map in the deployment config
# as soon as the 'che-multiuser' branch is merged to master
CHE_WORKSPACE_LOGS="/data/logs/machine/logs" \
CHE_HOST="${OPENSHIFT_NAMESPACE_URL}"

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

  DEFAULT_OPENSHIFT_ENDPOINT="https://${MINISHIFT_IP}:8443/"
  OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
  DEFAULT_OPENSHIFT_USERNAME="developer"
  OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}
  DEFAULT_OPENSHIFT_PASSWORD="developer"
  OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_OPENSHIFT_NAMESPACE_URL="${CHE_OPENSHIFT_PROJECT}.${MINISHIFT_IP}.nip.io"
  OPENSHIFT_NAMESPACE_URL=${OPENSHIFT_NAMESPACE_URL:-${DEFAULT_OPENSHIFT_NAMESPACE_URL}}
  CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
  DEFAULT_CHE_DEBUGGING_ENABLED="true"
  CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}
  DEFAULT_OC_SKIP_TLS="true"
  OC_SKIP_TLS=${OC_SKIP_TLS:-${DEFAULT_OC_SKIP_TLS}}
  DEFAULT_CHE_APPLY_RESOURCE_QUOTAS="false"
  CHE_APPLY_RESOURCE_QUOTAS=${CHE_APPLY_RESOURCE_QUOTAS:-${DEFAULT_CHE_APPLY_RESOURCE_QUOTAS}}
  DEFAULT_IMAGE_PULL_POLICY="IfNotPresent"
  IMAGE_PULL_POLICY=${IMAGE_PULL_POLICY:-${DEFAULT_IMAGE_PULL_POLICY}}

  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT}
  CHE_INFRA_OPENSHIFT_PROJECT=${CHE_INFRA_OPENSHIFT_PROJECT:-${DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT}}

elif [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then
  # ----------------------
  # Set osio configuration
  # ----------------------
  if [ -z "${OPENSHIFT_TOKEN+x}" ]; then echo "[CHE] **ERROR** Env var OPENSHIFT_TOKEN is unset. You need to set it with your OSO token to continue. To retrieve your token: https://console.starter-us-east-2.openshift.com/console/command-line. Aborting"; exit 1; fi
  
  DEFAULT_OPENSHIFT_ENDPOINT="https://api.starter-us-east-2.openshift.com"
  OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
  DEFAULT_CHE_OPENSHIFT_PROJECT="$(oc get projects -o=custom-columns=NAME:.metadata.name --no-headers | grep "\\-che$")"
  CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_OPENSHIFT_NAMESPACE_URL="${CHE_OPENSHIFT_PROJECT}.8a09.starter-us-east-2.openshiftapps.com"
  OPENSHIFT_NAMESPACE_URL=${OPENSHIFT_NAMESPACE_URL:-${DEFAULT_OPENSHIFT_NAMESPACE_URL}}
  DEFAULT_CHE_KEYCLOAK_DISABLED="false"
  CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
  DEFAULT_CHE_DEBUGGING_ENABLED="false"
  CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}
  DEFAULT_OC_SKIP_TLS="false"
  OC_SKIP_TLS=${OC_SKIP_TLS:-${DEFAULT_OC_SKIP_TLS}}

elif [ "${OPENSHIFT_FLAVOR}" == "ocp" ]; then
  # ----------------------
  # Set ocp configuration
  # ----------------------
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}

  DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT}
  CHE_INFRA_OPENSHIFT_PROJECT=${CHE_INFRA_OPENSHIFT_PROJECT:-${DEFAULT_CHE_INFRA_OPENSHIFT_PROJECT}}

  CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
  DEFAULT_CHE_DEBUGGING_ENABLED="false"
  CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}
  DEFAULT_OC_SKIP_TLS="false"
  OC_SKIP_TLS=${OC_SKIP_TLS:-${DEFAULT_OC_SKIP_TLS}}

fi


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

  echo -n "no creating it..."
  oc new-project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
fi
echo "done!"

echo -n "[CHE] Switching to \"${CHE_OPENSHIFT_PROJECT}\"..."
oc project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
echo "done!"

# -------------------------------------------------------------
# If command == clean up then delete all openshift objects
# -------------------------------------------------------------
if [ "${COMMAND}" == "cleanup" ]; then
  echo "[CHE] Deleting all OpenShift objects..."
  oc delete all --all
  echo "[CHE] Cleanup successfully started. Use \"oc get all\" to verify that all resources have been deleted."
  exit 0
# -------------------------------------------------------------
# If command == clean up then delete all openshift objects
# -------------------------------------------------------------
elif [ "${COMMAND}" == "rollupdate" ]; then
  echo "[CHE] Rollout latest version of Che..."
  oc rollout latest che
  echo "[CHE] Rollaout successfully started"
  exit 0
# ----------------------------------------------------------------
# At this point command should be "deploy" otherwise it's an error
# ----------------------------------------------------------------
elif [ "${COMMAND}" != "deploy" ]; then
  echo "[CHE] **ERROR**: Command \"${COMMAND}\" is not a valid command. Aborting."
  exit 1
fi

# -------------------------------------------------------------
# Deploying secondary servers
# for postgres and optionally Keycloak
# -------------------------------------------------------------

COMMAND_DIR=$(dirname "$0")

if [ "${CHE_MULTI_USER}" == "true" ]; then
    if [ "${CHE_DEDICATED_KEYCLOAK}" == "true" ]; then
        "${COMMAND_DIR}"/multi-user/deploy_postgres_and_keycloak.sh
    else
        "${COMMAND_DIR}"/multi-user/deploy_postgres_only.sh
    fi

    "${COMMAND_DIR}"/multi-user/wait_until_postgres_is_available.sh
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

  CHE_KEYCLOAK_AUTH__SERVER__URL=${CHE_KEYCLOAK_AUTH__SERVER__URL:-"http://${CHE_KEYCLOAK_SERVER_ROUTE}/auth"}
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
CHE_IMAGE="${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}"
# Escape slashes in CHE_IMAGE to use it with sed later
# e.g. docker.io/rhchestage => docker.io\/rhchestage
CHE_IMAGE_SANITIZED=$(echo "${CHE_IMAGE}" | sed 's/\//\\\//g')

MULTI_USER_REPLACEMENT_STRING="          - name: \"CHE_WORKSPACE_LOGS\"
            value: \"${CHE_WORKSPACE_LOGS}\"
          - name: \"CHE_KEYCLOAK_AUTH__SERVER__URL\"
            value: \"${CHE_KEYCLOAK_AUTH__SERVER__URL}\"
          - name: \"CHE_KEYCLOAK_REALM\"
            value: \"${CHE_KEYCLOAK_REALM}\"
          - name: \"CHE_KEYCLOAK_CLIENT__ID\"
            value: \"${CHE_KEYCLOAK_CLIENT__ID}\"
          - name: \"CHE_HOST\"
            value: \"${CHE_HOST}\""

echo
if [ "${OPENSHIFT_FLAVOR}" == "minishift" ]; then
  echo "[CHE] Deploying Che on minishift (image ${CHE_IMAGE})"
  DEFAULT_CHE_DEPLOYMENT_FILE_PATH=./che-spi-openshift.yml
  CHE_DEPLOYMENT_FILE_PATH=${CHE_DEPLOYMENT_FILE_PATH:-${DEFAULT_CHE_DEPLOYMENT_FILE_PATH}}

cat "${CHE_DEPLOYMENT_FILE_PATH}" | \
    if [ ! -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${OPENSHIFT_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/          imagePullPolicy:.*/          imagePullPolicy: \"${IMAGE_PULL_POLICY}\"/" | \
    sed "s/    workspaces-memory-limit: 2300Mi/    workspaces-memory-limit: 1300Mi/" | \
    sed "s/    workspaces-memory-request: 1500Mi/    workspaces-memory-request: 500Mi/" | \
    sed "s/    che-openshift-secure-routes: \"true\"/    che-openshift-secure-routes: \"false\"/" | \
    sed "s/    che-secure-external-urls: \"true\"/    che-secure-external-urls: \"false\"/" | \
    sed "s/    che.docker.server_evaluation_strategy.custom.external.protocol: https/    che.docker.server_evaluation_strategy.custom.external.protocol: http/" | \
    sed "s/    che-openshift-precreate-subpaths: \"false\"/    che-openshift-precreate-subpaths: \"true\"/" | \
    sed "s/    che.predefined.stacks.reload_on_start: \"true\"/    che.predefined.stacks.reload_on_start: \"false\"/" | \
    sed "s/    remote-debugging-enabled: \"false\"/    remote-debugging-enabled: \"${CHE_DEBUGGING_ENABLED}\"/" | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    sed "s/    CHE_INFRA_OPENSHIFT_TLS__ENABLED: \"true\"/    CHE_INFRA_OPENSHIFT_TLS__ENABLED: \"false\"/" | \
    sed "s|    CHE_INFRA_OPENSHIFT_PROJECT:.*|    CHE_INFRA_OPENSHIFT_PROJECT: ${CHE_OPENSHIFT_PROJECT}|" | \
    sed "s|    CHE_INFRA_OPENSHIFT_BOOTSTRAPPER_BINARY__URL:.*|    CHE_INFRA_OPENSHIFT_BOOTSTRAPPER_BINARY__URL: ${HTTP_PROTOCOL}://che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}/agent-binaries/linux_amd64/bootstrapper/bootstrapper|" | \
    sed "s|    CHE_WEBSOCKET_ENDPOINT:.*|    CHE_WEBSOCKET_ENDPOINT: ${WS_PROTOCOL}://che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api/websocket|" | \
    sed "s|    CHE_HOST: \${DEFAULT_OPENSHIFT_NAMESPACE_URL}|    CHE_HOST: che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}|" | \
    sed "s|    CHE_API: http://\${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api|    CHE_API: ${HTTP_PROTOCOL}://che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api|" | \
    grep -v -e "tls:" -e "insecureEdgeTerminationPolicy: Redirect" -e "termination: edge" | \
    if [ "${CHE_INFRA_OPENSHIFT_OAUTH__TOKEN+x}" ]; then sed "s|    CHE_INFRA_OPENSHIFT_OAUTH__TOKEN:.*|    CHE_INFRA_OPENSHIFT_OAUTH__TOKEN: ${CHE_INFRA_OPENSHIFT_OAUTH__TOKEN}|"; else cat -;  fi | \
    if [ "${CHE_INFRA_OPENSHIFT_USERNAME+x}" ]; then sed "s|    CHE_INFRA_OPENSHIFT_USERNAME:.*|    CHE_INFRA_OPENSHIFT_USERNAME: ${CHE_INFRA_OPENSHIFT_USERNAME}|"; else cat -;  fi | \
    if [ "${CHE_INFRA_OPENSHIFT_PASSWORD+x}" ]; then sed "s|    CHE_INFRA_OPENSHIFT_PASSWORD:.*|    CHE_INFRA_OPENSHIFT_PASSWORD: ${CHE_INFRA_OPENSHIFT_PASSWORD}|"; else cat -;  fi | \
    if [ "${CHE_KEYCLOAK_DISABLED}" == "true" ]; then sed "s/    keycloak-disabled: \"false\"/    keycloak-disabled: \"true\"/" ; else cat -; fi | \
    if [ "${CHE_DEBUGGING_ENABLED}" == "true" ]; then sed "s/    remote-debugging-enabled: \"false\"/    remote-debugging-enabled: \"true\"/"; else cat -; fi | \
    append_after_match "env:" "${MULTI_USER_REPLACEMENT_STRING}" | \
    oc apply --force=true -f -
elif [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then
  echo "[CHE] Deploying Che on OSIO (image ${CHE_IMAGE})"
  DEFAULT_CHE_DEPLOYMENT_FILE_PATH=./che-spi-openshift.yml
  CHE_DEPLOYMENT_FILE_PATH=${CHE_DEPLOYMENT_FILE_PATH:-${DEFAULT_CHE_DEPLOYMENT_FILE_PATH}}

cat "${CHE_DEPLOYMENT_FILE_PATH}" | \
    if [ ! -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${OPENSHIFT_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    sed "s|    CHE_INFRA_OPENSHIFT_PROJECT:.*|    CHE_INFRA_OPENSHIFT_PROJECT: ${CHE_OPENSHIFT_PROJECT}|" | \
    sed "s|    CHE_INFRA_OPENSHIFT_BOOTSTRAPPER_BINARY__URL:.*|    CHE_INFRA_OPENSHIFT_BOOTSTRAPPER_BINARY__URL: ${HTTP_PROTOCOL}://che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}/agent-binaries/linux_amd64/bootstrapper/bootstrapper|" | \
    sed "s|    CHE_WEBSOCKET_ENDPOINT:.*|    CHE_WEBSOCKET_ENDPOINT: ${WS_PROTOCOL}://che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api/websocket|" | \
    sed "s|    CHE_HOST: \${DEFAULT_OPENSHIFT_NAMESPACE_URL}|    CHE_HOST: che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}|" | \
    sed "s|    CHE_API: http://\${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api|    CHE_API: ${HTTP_PROTOCOL}://che-${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api|" | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/          imagePullPolicy:.*/          imagePullPolicy: \"${IMAGE_PULL_POLICY}\"/" | \
    if [ "${CHE_KEYCLOAK_DISABLED}" == "true" ]; then sed "s/    keycloak-disabled: \"false\"/    keycloak-disabled: \"true\"/" ; else cat -; fi | \
    if [ "${CHE_DEBUGGING_ENABLED}" == "true" ]; then sed "s/    remote-debugging-enabled: \"false\"/    remote-debugging-enabled: \"true\"/"; else cat -; fi | \
    append_after_match "env:" "${MULTI_USER_REPLACEMENT_STRING}" | \
    oc apply --force=true -f -
else
  echo "[CHE] Deploying Che on OpenShift Container Platform (image ${CHE_IMAGE})"
  DEFAULT_CHE_DEPLOYMENT_FILE_PATH=./che-spi-openshift.yml
  CHE_DEPLOYMENT_FILE_PATH=${CHE_DEPLOYMENT_FILE_PATH:-${DEFAULT_CHE_DEPLOYMENT_FILE_PATH}}

cat "${CHE_DEPLOYMENT_FILE_PATH}" | \
    if [ ! -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${OPENSHIFT_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/          imagePullPolicy:.*/          imagePullPolicy: \"${IMAGE_PULL_POLICY}\"/" | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    sed "s/    keycloak-disabled:.*/    keycloak-disabled: \"${CHE_KEYCLOAK_DISABLED}\"/" | \
    sed "s|    CHE_INFRA_OPENSHIFT_PROJECT:.*|    CHE_INFRA_OPENSHIFT_PROJECT: ${CHE_INFRA_OPENSHIFT_PROJECT}|" | \
    sed "s|    CHE_INFRA_OPENSHIFT_BOOTSTRAPPER_BINARY__URL:.*|    CHE_INFRA_OPENSHIFT_BOOTSTRAPPER_BINARY__URL: ${HTTP_PROTOCOL}://che-${OPENSHIFT_NAMESPACE_URL}/agent-binaries/linux_amd64/bootstrapper/bootstrapper|" | \
    sed "s|    CHE_WEBSOCKET_ENDPOINT:.*|    CHE_WEBSOCKET_ENDPOINT: ${WS_PROTOCOL}://che-${OPENSHIFT_NAMESPACE_URL}/api/websocket|" | \
    sed "s|    CHE_HOST: \${DEFAULT_OPENSHIFT_NAMESPACE_URL}|    CHE_HOST: che-${OPENSHIFT_NAMESPACE_URL}|" | \
    sed "s|    CHE_API: http://\${DEFAULT_OPENSHIFT_NAMESPACE_URL}/api|    CHE_API: ${HTTP_PROTOCOL}://che-${OPENSHIFT_NAMESPACE_URL}/api|" | \
    if [ "${CHE_INFRA_OPENSHIFT_OAUTH__TOKEN+x}" ]; then sed "s|    CHE_INFRA_OPENSHIFT_OAUTH__TOKEN:.*|    CHE_INFRA_OPENSHIFT_OAUTH__TOKEN: ${CHE_INFRA_OPENSHIFT_OAUTH__TOKEN}|"; else cat -;  fi | \
    if [ "${CHE_INFRA_OPENSHIFT_USERNAME+x}" ]; then sed "s|    CHE_INFRA_OPENSHIFT_USERNAME:.*|    CHE_INFRA_OPENSHIFT_USERNAME: ${CHE_INFRA_OPENSHIFT_USERNAME}|"; else cat -;  fi | \
    if [ "${CHE_INFRA_OPENSHIFT_PASSWORD+x}" ]; then sed "s|    CHE_INFRA_OPENSHIFT_PASSWORD:.*|    CHE_INFRA_OPENSHIFT_PASSWORD: ${CHE_INFRA_OPENSHIFT_PASSWORD}|"; else cat -;  fi | \
    if [ "${CHE_LOG_LEVEL}" == "DEBUG" ]; then sed "s/    log-level: \"INFO\"/    log-level: \"DEBUG\"/" ; else cat -; fi | \
    if [ "${CHE_DEBUGGING_ENABLED}" == "true" ]; then sed "s/    remote-debugging-enabled: \"false\"/    remote-debugging-enabled: \"true\"/"; else cat -; fi | \
    if [ "${ENABLE_SSL}" == "false" ]; then sed "s/    CHE_INFRA_OPENSHIFT_TLS__ENABLED: \"true\"/    CHE_INFRA_OPENSHIFT_TLS__ENABLED: \"false\"/" ; else cat -; fi | \
    if [ "${ENABLE_SSL}" == "false" ]; then sed "s/    che-openshift-secure-routes: \"true\"/    che-openshift-secure-routes: \"false\"/" ; else cat -; fi | \
    if [ "${ENABLE_SSL}" == "false" ]; then sed "s/    che-secure-external-urls: \"true\"/    che-secure-external-urls: \"false\"/" ; else cat -; fi | \
    if [ "${ENABLE_SSL}" == "false" ]; then grep -v -e "tls:" -e "insecureEdgeTerminationPolicy: Redirect" -e "termination: edge" ; else cat -; fi | \
    if [ "${ENABLE_SSL}" == "false" ]; then sed "s/    che.docker.server_evaluation_strategy.custom.external.protocol: https/    che.docker.server_evaluation_strategy.custom.external.protocol: http/" ; else cat -; fi | \
    if [ "${K8S_VERSION_PRIOR_TO_1_6}" == "true" ]; then sed "s/    che-openshift-precreate-subpaths: \"false\"/    che-openshift-precreate-subpaths: \"true\"/"  ; else cat -; fi | \
    append_after_match "env:" "${MULTI_USER_REPLACEMENT_STRING}" | \
    oc apply --force=true -f -
fi
echo

if [ "${CHE_DEDICATED_KEYCLOAK}" == "true" ]; then
  ${COMMAND_DIR}/multi-user/configure_and_start_keycloak.sh
fi

# --------------------------------
# Setup debugging routes if needed
# --------------------------------
if [ "${CHE_DEBUGGING_ENABLED}" == "true" ]; then

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
echo "[CHE]    http://${che_route}"
echo
echo
