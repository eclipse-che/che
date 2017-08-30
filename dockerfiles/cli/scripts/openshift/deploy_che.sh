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

DEFAULT_COMMAND="deploy"
COMMAND=${COMMAND:-${DEFAULT_COMMAND}}
DEFAULT_CHE_IMAGE_REPO="docker.io/eclipse/che-server"
CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}
DEFAULT_CHE_IMAGE_TAG="nightly-centos"
CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}
DEFAULT_CHE_LOG_LEVEL="INFO"
CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}

# Keycloak production endpoints are used by default
DEFAULT_KEYCLOAK_OSO_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token"
KEYCLOAK_OSO_ENDPOINT=${KEYCLOAK_OSO_ENDPOINT:-${DEFAULT_KEYCLOAK_OSO_ENDPOINT}}
DEFAULT_KEYCLOAK_GITHUB_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/github/token"
KEYCLOAK_GITHUB_ENDPOINT=${KEYCLOAK_GITHUB_ENDPOINT:-${DEFAULT_KEYCLOAK_GITHUB_ENDPOINT}}

# OPENSHIFT_FLAVOR can be minishift or openshift
# TODO Set flavour via a parameter
DEFAULT_OPENSHIFT_FLAVOR=minishift
OPENSHIFT_FLAVOR=${OPENSHIFT_FLAVOR:-${DEFAULT_OPENSHIFT_FLAVOR}}


if [ "${OPENSHIFT_FLAVOR}" == "minishift" ]; then
  # ---------------------------
  # Set minishift configuration
  # ---------------------------
  echo -n "[CHE] Checking if minishift is running..."
  minishift status | grep -q "Running" ||(echo "Minishift is not running. Aborting"; exit 1)
  echo "done!"  

  DEFAULT_OPENSHIFT_ENDPOINT="https://$(minishift ip):8443/"
  OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
  DEFAULT_OPENSHIFT_USERNAME="developer"
  OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}
  DEFAULT_OPENSHIFT_PASSWORD="developer"
  OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_OPENSHIFT_NAMESPACE_URL="${CHE_OPENSHIFT_PROJECT}.$(minishift ip).nip.io"
  OPENSHIFT_NAMESPACE_URL=${OPENSHIFT_NAMESPACE_URL:-${DEFAULT_OPENSHIFT_NAMESPACE_URL}}
  DEFAULT_CHE_KEYCLOAK_DISABLED="true"
  CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
  DEFAULT_CHE_DEBUGGING_ENABLED="true"
  CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}

elif [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then
  # ----------------------
  # Set osio configuration
  # ----------------------
  if [ -z "${OPENSHIFT_TOKEN+x}" ]; then echo "[CHE] **ERROR** Env var OPENSHIFT_TOKEN is unset. You need to set it with your OSO token to continue. To retrieve your token: https://console.starter-us-east-2.openshift.com/console/command-line. Aborting"; exit 1; fi
  
  DEFAULT_OPENSHIFT_ENDPOINT="https://api.starter-us-east-2.openshift.com"
  OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
  DEFAULT_CHE_OPENSHIFT_PROJECT="$(oc get projects -o=custom-columns=NAME:.metadata.name --no-headers | grep "\-che$")"
  CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_OPENSHIFT_NAMESPACE_URL="${CHE_OPENSHIFT_PROJECT}.8a09.starter-us-east-2.openshiftapps.com"
  OPENSHIFT_NAMESPACE_URL=${OPENSHIFT_NAMESPACE_URL:-${DEFAULT_OPENSHIFT_NAMESPACE_URL}}
  DEFAULT_CHE_KEYCLOAK_DISABLED="false"
  CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
  DEFAULT_CHE_DEBUGGING_ENABLED="false"
  CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}

elif [ "${OPENSHIFT_FLAVOR}" == "ocp" ]; then
  # ----------------------
  # Set ocp configuration
  # ----------------------
  DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
  CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}
  DEFAULT_CHE_KEYCLOAK_DISABLED="true"
  CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
  DEFAULT_CHE_DEBUGGING_ENABLED="false"
  CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}

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
  oc login "${OPENSHIFT_ENDPOINT}" --insecure-skip-tls-verify=false -u "${OPENSHIFT_USERNAME}" -p "${OPENSHIFT_PASSWORD}" > /dev/null
  OPENSHIFT_TOKEN=$(oc whoami -t)
else
  oc login "${OPENSHIFT_ENDPOINT}" --insecure-skip-tls-verify=false --token="${OPENSHIFT_TOKEN}"  > /dev/null
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
  ## TODO we should consider oc apply the latest http://central.maven.org/maven2/io/fabric8/online/packages/fabric8-online-che-quotas-oso/
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
# Get latest version of fabric8-online templates
# ----------------------------------------------
# TODO make it possible to use a local Che template instead of always downloading it from maven central
echo -n "[CHE] Retrieving latest version of fabric8 online Che template..."
OSIO_VERSION=$(curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/maven-metadata.xml | grep latest | sed -e 's,.*<latest>\([^<]*\)</latest>.*,\1,g')
echo "done! (v.${OSIO_VERSION})"

# ----------------------------------------------
# Start the deployment
# ----------------------------------------------
CHE_IMAGE="${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}"
# Escape slashes in CHE_IMAGE to use it with sed later
# e.g. docker.io/rhchestage => docker.io\/rhchestage
CHE_IMAGE_SANITIZED=$(echo "${CHE_IMAGE}" | sed 's/\//\\\//g')

echo
if [ "${OPENSHIFT_FLAVOR}" == "minishift" ]; then
  echo "[CHE] Deploying Che on minishift (image ${CHE_IMAGE})"
  curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/"${OSIO_VERSION}"/che-"${OSIO_VERSION}"-openshift.yml | \
    if [ ! -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${OPENSHIFT_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/    workspaces-memory-limit: 2300Mi/    workspaces-memory-limit: 1300Mi/" | \
    sed "s/    workspaces-memory-request: 1500Mi/    workspaces-memory-request: 500Mi/" | \
    sed "s/    che-openshift-secure-routes: \"true\"/    che-openshift-secure-routes: \"false\"/" | \
    sed "s/    che-secure-external-urls: \"true\"/    che-secure-external-urls: \"false\"/" | \
    sed "s/    che.docker.server_evaluation_strategy.custom.external.protocol: https/    che.docker.server_evaluation_strategy.custom.external.protocol: http/" | \
    sed "s/    che-openshift-precreate-subpaths: \"false\"/    che-openshift-precreate-subpaths: \"true\"/" | \
    sed "s/    che.predefined.stacks.reload_on_start: \"true\"/    che.predefined.stacks.reload_on_start: \"false\"/" | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    grep -v -e "tls:" -e "insecureEdgeTerminationPolicy: Redirect" -e "termination: edge" | \
    if [ "${CHE_KEYCLOAK_DISABLED}" == "true" ]; then sed "s/    keycloak-disabled: \"false\"/    keycloak-disabled: \"true\"/" ; else cat -; fi | \
    oc apply --force=true -f -
elif [ "${OPENSHIFT_FLAVOR}" == "osio" ]; then
  echo "[CHE] Deploying Che on OSIO (image ${CHE_IMAGE})"
  curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/"${OSIO_VERSION}"/che-"${OSIO_VERSION}"-openshift.yml | \
    if [ ! -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${OPENSHIFT_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    if [ "${CHE_KEYCLOAK_DISABLED}" == "true" ]; then sed "s/    keycloak-disabled: \"false\"/    keycloak-disabled: \"true\"/" ; else cat -; fi | \
    oc apply --force=true -f -
else
  echo "[CHE] Deploying Che on OpenShift Container Platform (image ${CHE_IMAGE})"
  curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/"${OSIO_VERSION}"/che-"${OSIO_VERSION}"-openshift.yml | \
    if [ ! -z "${OPENSHIFT_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${OPENSHIFT_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    sed "s/    keycloak-disabled:.*/    keycloak-disabled: \"${CHE_KEYCLOAK_DISABLED}\"/" | \
    if [ "${CHE_LOG_LEVEL}" == "DEBUG" ]; then sed "s/    log-level: \"INFO\"/    log-level: \"DEBUG\"/" ; else cat -; fi | \
    oc apply --force=true -f -
fi
echo

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
  echo "[CHE] Remote wsmaster debugging URL: $(minishift ip):${NodePort}"
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
