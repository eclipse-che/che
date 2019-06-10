#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# This script is meant for quick & easy install of Che on OpenShift via:
#
#  ``` bash
#   DEPLOY_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/deploy/openshift/deploy_che.sh
#   curl -fsSL ${DEPLOY_SCRIPT_URL} -o get-che.sh
#   bash get-che.sh
#   ```
#
# For more deployment options: https://www.eclipse.org/che/docs/setup/openshift/index.html
# --------------
# Print Che logo
# --------------

set -e

export TERM=xterm

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

HELP="
--help - script help menu
--project | -p - OpenShift namespace to deploy Che (defaults to eclipse-che).  Example: --project=myproject
--multiuser - Deploy che in multiuser mode
--no-pull - IfNotPresent pull policy for Che server deployment
--rolling - Rolling update strategy (Recreate is the default one). With Rolling strategy Che server pvc and volume aren't created
--debug - Deploy Che in a debug mode, create and expose debug route
--image-che - Override default Che image. Example: --image-che=org/repo:tag. Tag is mandatory!
--secure | -s - Deploy Che with SSL enabled
--setup-ocp-oauth - register OCP oauth client and setup Keycloak and Che to use OpenShift Identity Provider
--deploy-che-plugin-registry - deploy Che plugin registry
--deploy-che-devfile-registry - deploy Che devfile registry
--tracing - Deploy jaeger and enable tracing collection
--monitoring - Deploy Grafana + Prometheus and enable metrics collection
===================================
ENV vars: this script automatically detect envs vars beginning with "CHE_" and passes them to Che deployments:
CHE_IMAGE_REPO - Che server Docker image, defaults to "eclipse-che-server"
CHE_IMAGE_TAG - Set che-server image tag, defaults to "nightly"
CHE_INFRA_OPENSHIFT_PROJECT - namespace for workspace objects (defaults to current namespace of Che pod (CHE_OPENSHIFT_PROJECT which defaults to eclipse-che)). It can be overriden with -p|--project param. A separate ws namespace can be used only if username/password or token is provided
CHE_INFRA_KUBERNETES_PVC_STRATEGY - One PVC per workspace (unique) or one PVC shared by all workspaced (common). Defaults to unique
CHE_INFRA_KUBERNETES_PVC_QUANTITY - PVC default claim. Set to 1Gi.
CHE_KEYCLOAK_AUTH__SERVER__URL - URL of a Keycloak auth server. Defaults to route of a Keycloak deployment
"

for key in "$@"
do
case $key in
    -s | --secure)
    ENABLE_SSL=true
    shift
    ;;
    -p=*| --project=*)
    CHE_OPENSHIFT_PROJECT="${key#*=}"
    shift
    ;;
    --image-che=*)
    CHE_IMAGE_REPO=$(echo "${key#*=}" | sed 's/:.*//')
    CHE_IMAGE_TAG=$(echo "${key#*=}" | sed 's/.*://')
    shift
    ;;
    --multiuser)
    CHE_MULTIUSER=true
    shift
    ;;
    --no-pull)
    IMAGE_PULL_POLICY=IfNotPresent
    shift
    ;;
    --setup-ocp-oauth)
    export SETUP_OCP_OAUTH=true
    shift
    ;;
    --postgres-debug)
    POSTGRESQL_LOG_DEBUG=true
    shift
    ;;
    --rolling)
    UPDATE_STRATEGY=Rolling
    shift
    ;;
    --debug)
    CHE_DEBUG_SERVER=true
    shift
    ;;
    --tracing)
    CHE_TRACING_ENABLED=true
    shift
    ;;
    --monitoring)
    CHE_METRICS_ENABLED=true
    shift
    ;;
    --deploy-che-plugin-registry)
    DEPLOY_CHE_PLUGIN_REGISTRY=true
    shift
    ;;
    --deploy-che-devfile-registry)
    DEPLOY_CHE_DEVFILE_REGISTRY=true
    shift
    ;;
    --help)
        echo -e "$HELP"
        exit 1
    ;;
    *)
    echo "You've passed wrong arg '$key'."
    echo -e "$HELP"
    exit 1
    ;;
esac
done

# util, helper functions and default env values

BASE_DIR=$(cd "$(dirname "$0")"; pwd)

# if available use oc binary provided by ocp.sh script
if [ ! -f /tmp/oc ]; then
    OC_BINARY="oc"
else
    OC_BINARY="/tmp/oc"
fi

DEFAULT_IMAGE_PULL_POLICY="Always"
export IMAGE_PULL_POLICY=${IMAGE_PULL_POLICY:-${DEFAULT_IMAGE_PULL_POLICY}}

DEFAULT_UPDATE_STRATEGY="Recreate"
export UPDATE_STRATEGY=${UPDATE_STRATEGY:-${DEFAULT_UPDATE_STRATEGY}}

DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
export CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}

DEFAULT_CHE_IMAGE_REPO="eclipse/che-server"
export CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}

DEFAULT_CHE_IMAGE_TAG="nightly"
export CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}

DEFAULT_IMAGE_KEYCLOAK="eclipse/che-keycloak"
export IMAGE_KEYCLOAK=${IMAGE_KEYCLOAK:-${DEFAULT_IMAGE_KEYCLOAK}}

DEFAULT_KEYCLOAK_IMAGE_TAG="nightly"
export KEYCLOAK_IMAGE_TAG=${KEYCLOAK_IMAGE_TAG:-${DEFAULT_KEYCLOAK_IMAGE_TAG}}

DEFAULT_KEYCLOAK_IMAGE_PULL_POLICY="Always"
export KEYCLOAK_IMAGE_PULL_POLICY=${KEYCLOAK_IMAGE_PULL_POLICY:-${DEFAULT_KEYCLOAK_IMAGE_PULL_POLICY}}

DEFAULT_ENABLE_SSL="false"
export ENABLE_SSL=${ENABLE_SSL:-${DEFAULT_ENABLE_SSL}}

DEFAULT_OPENSHIFT_USERNAME="developer"
export OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}

DEFAULT_OPENSHIFT_PASSWORD="developer"
export OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}


DEFAULT_OCP_OAUTH_CLIENT_ID=ocp-client
export OCP_OAUTH_CLIENT_ID=${OCP_OAUTH_CLIENT_ID:-${DEFAULT_OCP_OAUTH_CLIENT_ID}}

DEFAULT_OCP_OAUTH_CLIENT_SECRET=ocp-client-secret
export OCP_OAUTH_CLIENT_SECRET=${OCP_OAUTH_CLIENT_SECRET:-${DEFAULT_OCP_OAUTH_CLIENT_SECRET}}

DEFAULT_OCP_IDENTITY_PROVIDER_ID=openshift-v3
export OCP_IDENTITY_PROVIDER_ID=${OCP_IDENTITY_PROVIDER_ID:-${DEFAULT_OCP_IDENTITY_PROVIDER_ID}}

DEFAULT_KEYCLOAK_USER=admin
export KEYCLOAK_USER=${KEYCLOAK_USER:-${DEFAULT_KEYCLOAK_USER}}

DEFAULT_KEYCLOAK_PASSWORD=admin
export KEYCLOAK_PASSWORD=${KEYCLOAK_PASSWORD:-${DEFAULT_KEYCLOAK_PASSWORD}}

###
### Plugin Registry settings
###
DEFAULT_PLUGIN_REGISTRY_IMAGE_TAG="latest"
export PLUGIN_REGISTRY_IMAGE_TAG=${PLUGIN_REGISTRY_IMAGE_TAG:-${DEFAULT_PLUGIN_REGISTRY_IMAGE_TAG}}

DEFAULT_PLUGIN_REGISTRY_IMAGE="quay.io/openshiftio/che-plugin-registry"
export PLUGIN_REGISTRY_IMAGE=${PLUGIN_REGISTRY_IMAGE:-${DEFAULT_PLUGIN_REGISTRY_IMAGE}}

DEFAULT_PLUGIN_REGISTRY_IMAGE_PULL_POLICY="Always"
export PLUGIN_REGISTRY_IMAGE_PULL_POLICY=${PLUGIN_REGISTRY_IMAGE_PULL_POLICY:-${DEFAULT_PLUGIN_REGISTRY_IMAGE_PULL_POLICY}}

DEFAULT_PLUGIN__REGISTRY__URL="https://che-plugin-registry.openshift.io/v3"
export PLUGIN__REGISTRY__URL=${PLUGIN__REGISTRY__URL:-${DEFAULT_PLUGIN__REGISTRY__URL}}

###
### Devfile Registry settings
###
DEFAULT_DEVFILE_REGISTRY_IMAGE_TAG="latest"
export DEVFILE_REGISTRY_IMAGE_TAG=${DEVFILE_REGISTRY_IMAGE_TAG:-${DEFAULT_DEVFILE_REGISTRY_IMAGE_TAG}}

DEFAULT_DEVFILE_REGISTRY_IMAGE="quay.io/openshiftio/che-devfile-registry"
export DEVFILE_REGISTRY_IMAGE=${DEVFILE_REGISTRY_IMAGE:-${DEFAULT_DEVFILE_REGISTRY_IMAGE}}

DEFAULT_DEVFILE_REGISTRY_IMAGE_PULL_POLICY="Always"
export DEVFILE_REGISTRY_IMAGE_PULL_POLICY=${DEVFILE_REGISTRY_IMAGE_PULL_POLICY:-${DEFAULT_DEVFILE_REGISTRY_IMAGE_PULL_POLICY}}

DEFAULT_DEVFILE__REGISTRY__URL="https://che-devfile-registry.openshift.io/"
export DEVFILE__REGISTRY__URL=${DEVFILE__REGISTRY__URL:-${DEFAULT_DEVFILE__REGISTRY__URL}}


DEFAULT_CHE_METRICS_ENABLED="false"
export CHE_METRICS_ENABLED=${CHE_METRICS_ENABLED:-${DEFAULT_CHE_METRICS_ENABLED}}

DEFAULT_CHE_TRACING_ENABLED="false"
export CHE_TRACING_ENABLED=${CHE_TRACING_ENABLED:-${DEFAULT_CHE_TRACING_ENABLED}}

DEFAULT_JAEGER_ENDPOINT="http://jaeger-collector:14268/api/traces"
export JAEGER_ENDPOINT=${JAEGER_ENDPOINT:-${DEFAULT_JAEGER_ENDPOINT}}

DEFAULT_JAEGER_SERVICE_NAME="che-server"
export JAEGER_SERVICE_NAME=${JAEGER_SERVICE_NAME:-${DEFAULT_JAEGER_SERVICE_NAME}}

DEFAULT_JAEGER_SAMPLER_MANAGER_HOST_PORT="jaeger:5778"
export JAEGER_SAMPLER_MANAGER_HOST_PORT=${JAEGER_SAMPLER_MANAGER_HOST_PORT:-${DEFAULT_JAEGER_SAMPLER_MANAGER_HOST_PORT}}

DEFAULT_JAEGER_SAMPLER_TYPE="const"
export JAEGER_SAMPLER_TYPE=${JAEGER_SAMPLER_TYPE:-${DEFAULT_JAEGER_SAMPLER_TYPE}}

DEFAULT_JAEGER_SAMPLER_PARAM="1"
export JAEGER_SAMPLER_PARAM=${JAEGER_SAMPLER_PARAM:-${DEFAULT_JAEGER_SAMPLER_PARAM}}

DEFAULT_JAEGER_REPORTER_MAX_QUEUE_SIZE="10000"
export JAEGER_REPORTER_MAX_QUEUE_SIZE=${JAEGER_REPORTER_MAX_QUEUE_SIZE:-${DEFAULT_JAEGER_REPORTER_MAX_QUEUE_SIZE}}


if [ "${ENABLE_SSL}" == "true" ]; then
    HTTP_PROTOCOL="https"
    WS_PROTOCOL="wss"
    TLS="true"
else
    HTTP_PROTOCOL="http"
    WS_PROTOCOL="ws"
    TLS="false"
fi

DEFAULT_CHE_MULTIUSER="false"
export CHE_MULTIUSER=${CHE_MULTIUSER:-${DEFAULT_CHE_MULTIUSER}}

DEFAULT_POSTGRESQL_LOG_DEBUG="false"
export POSTGRESQL_LOG_DEBUG=${POSTGRESQL_LOG_DEBUG:-${DEFAULT_POSTGRESQL_LOG_DEBUG}}

printInfo() {
  green=`tput setaf 2`
  reset=`tput sgr0`
  echo "${green}[INFO]: ${1} ${reset}"
}

printWarning() {
  yellow=`tput setaf 3`
  reset=`tput sgr0`
  echo "${yellow}[WARNING]: ${1} ${reset}"
}

printError() {
  red=`tput setaf 1`
  reset=`tput sgr0`
  echo "${red}[ERROR]: ${1} ${reset}"
}

# --------------------------------------------------------
# Check pre-requisites
# --------------------------------------------------------
command -v oc >/dev/null 2>&1 || { printError "Command line tool oc (https://docs.openshift.org/latest/cli_reference/get_started_cli.html) is required but it's not installed. Aborting."; exit 1; }

# check if oc client has an active session
isLoggedIn() {
  printInfo "Checking if you are currently logged in..."
  ${OC_BINARY} whoami > /dev/null
  OUT=$?
  if [ ${OUT} -eq 1 ]; then
    printError "Log in to your OpenShift cluster: oc login --server=openshiftIP"
    exit 1
  else
    CONTEXT=$(${OC_BINARY} whoami -c)
    OPENSHIFT_ENDPOINT=$(oc whoami -c --show-context=false --show-server=true)
    printInfo "Active session found. Your current context is: ${CONTEXT}"
  fi
}

getTemplates(){
  if [ ! -d "${BASE_DIR}/templates" ]; then
    printInfo "Local templates directory not found. Downloading templates..."
    curl -s https://codeload.github.com/eclipse/che/tar.gz/master | tar -xz --strip=3 che-master/deploy/openshift/templates -C ${BASE_DIR}
    echo ${BASE_DIR}
    OUT=$?
    if [ ${OUT} -ne 0 ]; then
      printError "Failed to curl and untar Eclipse Che repo because of an error"
      printInfo "You may need to manually clone or download content of https://github.com/eclipse/che/tree/master/deploy/openshift and re-run the script"
      exit ${OUT}
    else
      printInfo "Templates have been successfully saved to ${BASE_DIR}/templates"
    fi
  else printInfo "Templates directory found at ${BASE_DIR}/templates. Applying templates from this directory. Delete it to get the latest templates if necessary"
  fi
}

wait_for_postgres() {
    DESIRED_REPLICA_COUNT=1
    CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/postgres -o=jsonpath='{.status.availableReplicas}')
    DEPLOYMENT_TIMEOUT_SEC=300
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
    while [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}" ] && [ ${SECONDS} -lt ${end} ]; do
      CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/postgres -o=jsonpath='{.status.availableReplicas}')
      timeout_in=$((end-SECONDS))
      printInfo "Deployment is in progress...(Current replica count=${CURRENT_REPLICA_COUNT}, ${timeout_in} seconds remain)"
      sleep ${POLLING_INTERVAL_SEC}
    done

    if [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}"  ]; then
      printError "Postgres deployment failed. Aborting. Run command 'oc rollout status postgres' to get more details."
      exit 1
    elif [ ${SECONDS} -ge ${end} ]; then
      printError "Deployment timeout. Aborting."
      exit 1
    fi
    printInfo "Postgres successfully deployed"
}

wait_for_keycloak() {
    DESIRED_REPLICA_COUNT=1
    CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/keycloak -o=jsonpath='{.status.availableReplicas}')
    DEPLOYMENT_TIMEOUT_SEC=300
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
    while [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}" ] && [ ${SECONDS} -lt ${end} ]; do
      CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/keycloak -o=jsonpath='{.status.availableReplicas}')
      timeout_in=$((end-SECONDS))
      printInfo "Deployment is in progress...(Current replica count=${CURRENT_REPLICA_COUNT}, ${timeout_in} seconds remain)"
      sleep ${POLLING_INTERVAL_SEC}
    done

    if [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}"  ]; then
      printError "Keycloak deployment failed. Aborting. Run command 'oc rollout status keycloak' to get more details."
      exit 1
    elif [ ${SECONDS} -ge ${end} ]; then
      printError "Deployment timeout. Aborting."
      exit 1
    fi
    printInfo "Keycloak successfully deployed"
}

wait_for_che() {
    CHE_ROUTE=$(oc get route/che -o=jsonpath='{.spec.host}')
    DESIRED_REPLICA_COUNT=1
    CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/che -o=jsonpath='{.status.availableReplicas}')
    DEPLOYMENT_TIMEOUT_SEC=300
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
    while [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}" ] && [ ${SECONDS} -lt ${end} ]; do
      CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/che -o=jsonpath='{.status.availableReplicas}')
      timeout_in=$((end-SECONDS))
      printInfo "Deployment is in progress...(Current replica count=${CURRENT_REPLICA_COUNT}, ${timeout_in} seconds remain)"
      sleep ${POLLING_INTERVAL_SEC}
    done

    if [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}"  ]; then
      printError "Che deployment failed. Aborting. Run command 'oc rollout status che' to get more details."
      exit 1
    elif [ ${SECONDS} -ge ${end} ]; then
      printError "Deployment timeout. Aborting."
      exit 1
    fi
    printInfo "Che successfully deployed and is available at ${HTTP_PROTOCOL}://${CHE_ROUTE}"
}

createNewProject() {

  WAIT_FOR_PROJECT_TO_DELETE=true
  CHE_REMOVE_PROJECT=true
  DELETE_OPENSHIFT_PROJECT_MESSAGE=$(printInfo "Removing namespace ${CHE_OPENSHIFT_PROJECT}")
  CREATE_ATTEMPTS=1
  if ${OC_BINARY} get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then
    printWarning "Namespace \"${CHE_OPENSHIFT_PROJECT}\" exists."
    while $WAIT_FOR_PROJECT_TO_DELETE
    do
    { # try
      echo -n $DELETE_OPENSHIFT_PROJECT_MESSAGE
      if $CHE_REMOVE_PROJECT; then
        ${OC_BINARY} delete project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
        CHE_REMOVE_PROJECT=false
        CREATE_ATTEMPTS=50
      fi
      DELETE_OPENSHIFT_PROJECT_MESSAGE="`tput setaf 2`.`tput sgr0`"
      if ! ${OC_BINARY} get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then
        WAIT_FOR_PROJECT_TO_DELETE=false
      fi
      echo -n $DELETE_OPENSHIFT_PROJECT_MESSAGE
    }
    done
    echo "`tput setaf 2` Done!`tput sgr0`"
  fi

  CURRENT_ATTEMPT=0
  CREATE_SUCCESS=1

  while [[ ${CURRENT_ATTEMPT} -lt ${CREATE_ATTEMPTS} ]]; do
    CURRENT_ATTEMPT=$((CURRENT_ATTEMPT + 1));
    printInfo "Creating namespace \"${CHE_OPENSHIFT_PROJECT}\" (attempt ${CURRENT_ATTEMPT}/${CREATE_ATTEMPTS})"

    OUT=`${OC_BINARY} new-project "${CHE_OPENSHIFT_PROJECT}" > /dev/null 2>&1; echo $?`
    if [[ ${OUT} -ne 0 ]]; then
      sleep 5 
    else
      CREATE_SUCCESS=0
      CURRENT_ATTEMPT=${CREATE_ATTEMPTS}
    fi
  done

  if [[ ${CREATE_SUCCESS} ]]; then
    printInfo "Namespace \"${CHE_OPENSHIFT_PROJECT}\" successfully created"
  else
    printError "Failed to create namespace ${CHE_OPENSHIFT_PROJECT}. It may exist in someone else's account or namespace deletion has not been fully completed. Try again in a short while or pick a different project name -p=myProject"
    exit 1
  fi
}

exposeDebugService() {
if [ "${CHE_DEBUG_SERVER}" == "true" ]; then
  printInfo "Creating an OS route to debug Che wsmaster..."
  ${OC_BINARY} create service nodeport che-debug --tcp=8000:8000
  ${OC_BINARY}  expose service che-debug
  NodePort=$(oc get service che-debug -o jsonpath='{.spec.ports[0].nodePort}')
  printInfo "Remote wsmaster debugging URL: ${CLUSTER_IP}:${NodePort}"
  printInfo "Increasing failure threshold for probes of Che deployment"
  oc set probe dc/che --readiness --liveness --failure-threshold=99999
fi
}

getRoutingSuffix() {
  if [ -z ${OPENSHIFT_ROUTING_SUFFIX+x} ]; then
  printInfo "Computing routing suffix"
  ${OC_BINARY} create service clusterip test --tcp=80:80 > /dev/null
  ${OC_BINARY} expose service test > /dev/null
  ROUTE=$(oc get route test -o=jsonpath='{.spec.host}')
  WORDTOREMOVE="test-${CHE_OPENSHIFT_PROJECT}."
  export OPENSHIFT_ROUTING_SUFFIX="${ROUTE//$WORDTOREMOVE/}"
  printInfo "Routing suffix identified as: ${OPENSHIFT_ROUTING_SUFFIX}"
  ${OC_BINARY} delete service test > /dev/null
  ${OC_BINARY} delete route test > /dev/null
fi
}

deployChePluginRegistry() {
if [ "${DEPLOY_CHE_PLUGIN_REGISTRY}" == "true" ]; then
  echo "Deploying Che plugin registry..."
  ${OC_BINARY} new-app -f ${BASE_DIR}/templates/che-plugin-registry.yml \
             -p IMAGE=${PLUGIN_REGISTRY_IMAGE} \
             -p IMAGE_TAG=${PLUGIN_REGISTRY_IMAGE_TAG} \
             -p PULL_POLICY=${PLUGIN_REGISTRY_IMAGE_PULL_POLICY}

  PLUGIN_REGISTRY_ROUTE=$($OC_BINARY get route/che-plugin-registry --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})
  echo "Che plugin registry deployment complete. $PLUGIN_REGISTRY_ROUTE"
fi
}

deployCheDevfileRegistry() {
if [ "${DEPLOY_CHE_DEVFILE_REGISTRY}" == "true" ]; then
  echo "Deploying Che devfile registry..."
  ${OC_BINARY} new-app -f ${BASE_DIR}/templates/che-devfile-registry.yml \
             -p IMAGE=${DEVFILE_REGISTRY_IMAGE} \
             -p IMAGE_TAG=${DEVFILE_REGISTRY_IMAGE_TAG} \
             -p PULL_POLICY=${DEVFILE_REGISTRY_IMAGE_PULL_POLICY}

  DEVFILE_REGISTRY_ROUTE=$($OC_BINARY get route/che-devfile-registry --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})
  echo "Che devfile registry deployment complete. $DEVFILE_REGISTRY_ROUTE"
fi
}

deployJaeger(){
    if [ "${CHE_TRACING_ENABLED}" == "true" ]; then
      echo "Deploying Jaeger..."
      ${OC_BINARY} new-app -f ${BASE_DIR}/templates/jaeger-all-in-one-template.yml
      JAEGER_ROUTE=$($OC_BINARY get route/jaeger-query --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})
      echo "Jaeger deployment complete. $JAEGER_ROUTE"
    fi
}

deployMetrics(){
    if [ "${CHE_METRICS_ENABLED}" == "true" ]; then
      echo "Deploying Grafana and Prometheus..."
      ${OC_BINARY} apply -f ${BASE_DIR}/templates/monitoring/grafana-dashboards.yaml
      ${OC_BINARY} apply -f ${BASE_DIR}/templates/monitoring/grafana-datasources.yaml
      ${OC_BINARY} apply -f ${BASE_DIR}/templates/monitoring/prometheus-config.yaml
      ${OC_BINARY} new-app -f ${BASE_DIR}/templates/monitoring/che-monitoring.yaml
      echo "Grafana deployment complete. $($OC_BINARY get route/grafana --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})"
      echo "Prometheus deployment complete. $($OC_BINARY get route/prometheus --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})"
    fi
}


deployChe() {
    if [ "${CHE_TRACING_ENABLED}" == "true" ]; then
      CHE_VAR_ARRAY=$(env | grep -e "^CHE_." -e "^JAEGER_.")
    else
      CHE_VAR_ARRAY=$(env | grep "^CHE_.")
    fi
    if [ ${#CHE_VAR_ARRAY[@]} -gt 0 ]; then
      ENV="-e ${CHE_VAR_ARRAY}"
    fi
    printInfo "Deploying Eclipse Che with the following params:
Multi-User: ${CHE_MULTIUSER}
HTTPS support: ${ENABLE_SSL}
Namespace: ${CHE_OPENSHIFT_PROJECT}
Che version: ${CHE_IMAGE_TAG}
Che server debug: ${CHE_DEBUG_SERVER}
Image: ${CHE_IMAGE_REPO}
Pull policy: ${IMAGE_PULL_POLICY}
Update strategy: ${UPDATE_STRATEGY}
Setup OpenShift oAuth: ${SETUP_OCP_OAUTH}
Enable Jaeger based tracing: ${CHE_TRACING_ENABLED}
Enable metrics collection: ${CHE_METRICS_ENABLED}
Environment variables:
${CHE_VAR_ARRAY}"
    CHE_INFRA_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT}
    CHE_INFRA_OPENSHIFT_OAUTH__IDENTITY__PROVIDER=NULL

    if [ "${CHE_MULTIUSER}" == "true" ]; then
      if [ "${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}" == "false" ]; then
        export KEYCLOAK_PARAM="-p CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD=false"
      fi
      ${OC_BINARY} new-app -f ${BASE_DIR}/templates/multi/postgres-template.yaml \
        -p POSTGRESQL_LOG_DEBUG=${POSTGRESQL_LOG_DEBUG}
      wait_for_postgres

      if [ "${SETUP_OCP_OAUTH}" == "true" ]; then
        # create secret with OpenShift certificate

        if [[ -z ${OPENSHIFT_CERT} ]]; then
          DEFAULT_OPENSHIFT_CERT_PATH=/var/lib/origin/openshift.local.config/master/ca.crt
          export OPENSHIFT_CERT_PATH=${OPENSHIFT_CERT_PATH:-${DEFAULT_OPENSHIFT_CERT_PATH}}
          OPENSHIFT_CERT="$(cat $OPENSHIFT_CERT_PATH)"
        fi

        $OC_BINARY new-app -f ${BASE_DIR}/templates/multi/openshift-certificate-secret.yaml -p CERTIFICATE="$OPENSHIFT_CERT"
      fi

      ${OC_BINARY} new-app -f ${BASE_DIR}/templates/multi/keycloak-template.yaml \
        -p ROUTING_SUFFIX=${OPENSHIFT_ROUTING_SUFFIX} \
        -p PROTOCOL=${HTTP_PROTOCOL} \
        -p KEYCLOAK_USER=${KEYCLOAK_USER} \
        -p KEYCLOAK_PASSWORD=${KEYCLOAK_PASSWORD} \
        -p IMAGE_KEYCLOAK=${IMAGE_KEYCLOAK} \
        -p KEYCLOAK_IMAGE_TAG=${KEYCLOAK_IMAGE_TAG} \
        -p KEYCLOAK_IMAGE_PULL_POLICY=${KEYCLOAK_IMAGE_PULL_POLICY} \
         ${KEYCLOAK_PARAM}
      wait_for_keycloak

      if [ "${SETUP_OCP_OAUTH}" == "true" ]; then
        printInfo "Registering oAuth client in OpenShift"
        # register oAuth client in OpenShift
        printInfo "Logging as \"system:admin\""
        $OC_BINARY login -u "system:admin"
        KEYCLOAK_ROUTE=$($OC_BINARY get route/keycloak --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})

        $OC_BINARY process -f ${BASE_DIR}/templates/multi/oauth-client.yaml \
          -p REDIRECT_URI="${HTTP_PROTOCOL}://${KEYCLOAK_ROUTE}/auth/realms/che/broker/${OCP_IDENTITY_PROVIDER_ID}/endpoint" \
          -p OCP_OAUTH_CLIENT_ID=${OCP_OAUTH_CLIENT_ID} \
          -p OCP_OAUTH_CLIENT_SECRET=${OCP_OAUTH_CLIENT_SECRET} | oc apply -f -

        # register OpenShift Identity Provider in Keycloak
        printInfo "Registering oAuth client in Keycloak"
        printInfo "Logging as \"${OPENSHIFT_USERNAME}\""
        $OC_BINARY login -u "${OPENSHIFT_USERNAME}" -p "${OPENSHIFT_PASSWORD}"
        KEYCLOAK_POD_NAME=$(${OC_BINARY} get pod --namespace=${CHE_OPENSHIFT_PROJECT} -l app=keycloak --no-headers | awk '{print $1}')
        ${OC_BINARY} exec ${KEYCLOAK_POD_NAME} -- /opt/jboss/keycloak/bin/kcadm.sh create identity-provider/instances -r che \
          -s alias=${OCP_IDENTITY_PROVIDER_ID} \
          -s providerId=${OCP_IDENTITY_PROVIDER_ID} \
          -s enabled=true \
          -s storeToken=true \
          -s addReadTokenRoleOnCreate=true \
          -s config.useJwksUrl="true" \
          -s config.clientId=${OCP_OAUTH_CLIENT_ID} \
          -s config.clientSecret=${OCP_OAUTH_CLIENT_SECRET} \
          -s config.baseUrl="${OPENSHIFT_ENDPOINT}" \
          -s config.defaultScope="user:full" \
          --no-config --server http://localhost:8080/auth --user ${KEYCLOAK_USER} --password ${KEYCLOAK_PASSWORD} --realm master

        # setup Che variables related to oAuth identity provider
        CHE_INFRA_OPENSHIFT_PROJECT=
        CHE_INFRA_OPENSHIFT_OAUTH__IDENTITY__PROVIDER=${OCP_IDENTITY_PROVIDER_ID}
      fi
    fi

    if [ "${DEPLOY_CHE_PLUGIN_REGISTRY}" == "true" ]; then
        PLUGIN_REGISTRY_ROUTE=$($OC_BINARY get route/che-plugin-registry --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})
        PLUGIN__REGISTRY__URL="${HTTP_PROTOCOL}://${PLUGIN_REGISTRY_ROUTE}/v3"
    fi

    if [ "${DEPLOY_CHE_DEVFILE_REGISTRY}" == "true" ]; then
        DEVFILE_REGISTRY_ROUTE=$($OC_BINARY get route/che-devfile-registry --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})
        DEVFILE__REGISTRY__URL="${HTTP_PROTOCOL}://${DEVFILE_REGISTRY_ROUTE}/"
    fi

    if [ ! -z ${CHE_INFRA_OPENSHIFT_PROJECT} ]; then
        # create workspace service account in the predefined workspace
        ${OC_BINARY} new-app -f ${BASE_DIR}/templates/che-workspace-service-account.yaml \
          -p SERVICE_ACCOUNT_NAME='che-workspace' \
          -p SERVICE_ACCOUNT_NAMESPACE=${CHE_INFRA_OPENSHIFT_PROJECT}
    fi
    WORKSPACE_SERVICE_ACCOUNT_NAME="che-workspace"

    ${OC_BINARY} new-app -f ${BASE_DIR}/templates/che-server-template.yaml \
                         -p ROUTING_SUFFIX=${OPENSHIFT_ROUTING_SUFFIX} \
                         -p IMAGE_CHE=${CHE_IMAGE_REPO} \
                         -p CHE_VERSION=${CHE_IMAGE_TAG} \
                         -p PULL_POLICY=${IMAGE_PULL_POLICY} \
                         -p STRATEGY=${UPDATE_STRATEGY} \
                         -p CHE_MULTIUSER=${CHE_MULTIUSER} \
                         -p PROTOCOL=${HTTP_PROTOCOL} \
                         -p WS_PROTOCOL=${WS_PROTOCOL} \
                         -p CHE_INFRA_OPENSHIFT_PROJECT=${CHE_INFRA_OPENSHIFT_PROJECT} \
                         -p CHE_INFRA_OPENSHIFT_OAUTH__IDENTITY__PROVIDER=${CHE_INFRA_OPENSHIFT_OAUTH__IDENTITY__PROVIDER} \
                         -p TLS=${TLS} \
                         -p CHE_WORKSPACE_PLUGIN__REGISTRY__URL=${PLUGIN__REGISTRY__URL} \
                         -p CHE_WORKSPACE_DEVFILE__REGISTRY__URL=${DEVFILE__REGISTRY__URL} \
                         -p CHE_INFRA_KUBERNETES_SERVICE__ACCOUNT__NAME=${WORKSPACE_SERVICE_ACCOUNT_NAME} \
                         -p CHE_DEBUG_SERVER=${CHE_DEBUG_SERVER} \
                         -p CHE_TRACING_ENABLED=${CHE_TRACING_ENABLED} \
                         -p CHE_METRICS_ENABLED=${CHE_METRICS_ENABLED} \
                         ${ENV}

    if [ ${UPDATE_STRATEGY} == "Recreate" ]; then
      ${OC_BINARY} apply -f ${BASE_DIR}/templates/pvc/che-server-pvc.yaml
      ${OC_BINARY} set volume dc/che --add -m "/data" --name=che-data-volume --claim-name=che-data-volume
    fi

    if [ "${ENABLE_SSL}" == "true" ]; then
      ${OC_BINARY} apply -f ${BASE_DIR}/templates/https/che-route-tls.yaml
      if [ "${CHE_MULTIUSER}" == "true" ]; then
        ${OC_BINARY} apply -f ${BASE_DIR}/templates/https/keycloak-route-tls.yaml
      fi
    fi
    CHE_ROUTE=$(oc get route/che -o=jsonpath='{.spec.host}')
    exposeDebugService
    if [ "${WAIT_FOR_CHE}" == "true" ]; then
      wait_for_che
    else
      printInfo "Che successfully deployed and will be soon available at ${HTTP_PROTOCOL}://${CHE_ROUTE}"
    fi
}
getTemplates
isLoggedIn
createNewProject
getRoutingSuffix
deployChePluginRegistry
deployCheDevfileRegistry
deployJaeger
deployMetrics
deployChe
