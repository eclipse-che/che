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
#   bash get-che.sh
#   ```
#
# For more deployment options: https://www.eclipse.org/che/docs/setup/openshift/index.html
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

HELP="
--help - script help menu
--project | -p - OpenShift namespace to deploy Che (defaults to eclipse-che).  Example: --project=myproject
--multiuser - Deploy che in multiuser mode
--no-pull - IfNotPresent pull policy for Che server deployment
--rolling - Rolling update strategy (Recreate is the default one). With Rolling strategy Che server pvc and volume aren't created
--debug - Deploy Che in a debug mode, create and expose debug route
--image-che - Override default Che image. Example: --image-che=org/repo:tag. Tag is mandatory!
===================================
ENV vars: this script automatically detect envs vars beginning with "CHE_" and passes them to Che deployments:
CHE_IMAGE_REPO - Che server Docker image, defaults to "eclipse-che-server"
CHE_IMAGE_TAG - Set che-server image tag, defaults to "nightly"
CHE_INFRA_OPENSHIFT_PROJECT - namespace for workspace objects (defaults to current namespace of Che pod (CHE_OPENSHIFT_PROJECT which defaults to eclipse-che)). It can be overriden with -p|--project param. A separate ws namespace can be used only if username/password or token is provided
CHE_INFRA_KUBERNETES_USERNAME - OpenShift username to create workspace objects with. Not used by default (service account is used instead)
CHE_INFRA_KUBERNETES_PASSWORD - OpenShift password
CHE_INFRA_KUBERNETES_OAUTH__TOKEN - OpenShift token to create workspace objects with. Not used by default (service account is used instead)
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
    --rolling)
    UPDATE_STRATEGY=Rolling
    shift
    ;;
    --debug)
    CHE_DEBUG_SERVER=true
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

DEFAULT_ENABLE_SSL="false"
export ENABLE_SSL=${ENABLE_SSL:-${DEFAULT_ENABLE_SSL}}

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
command -v jq >/dev/null 2>&1 || { printError "Command line tool jq (https://stedolan.github.io/jq) is required but it's not installed. Aborting."; exit 1; }

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
    printInfo "Active session found. Your current context is: ${CONTEXT}"
  fi
}

wait_for_postgres() {
    available=$(${OC_BINARY} get dc postgres -o=jsonpath={.status.conditions[0].status})
    progressing=$(${OC_BINARY} get dc postgres -o=jsonpath={.status.conditions[1].status})
    DEPLOYMENT_TIMEOUT_SEC=1200
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
    while [[ "${available}" != "\"True\"" || "${progressing}" != "\"True\"" ]] && [ ${SECONDS} -lt ${end} ]; do
      available=$(${OC_BINARY} get dc postgres -o json | jq '.status.conditions[] | select(.type == "Available") | .status')
      progressing=$(${OC_BINARY} get dc postgres -o json | jq '.status.conditions[] | select(.type == "Progressing") | .status')
      timeout_in=$((end-SECONDS))
      printInfo "Deployment is in progress...(Available.status=${available}, Progressing.status=${progressing}, ${timeout_in} seconds remain)"
      sleep ${POLLING_INTERVAL_SEC}
    done
    if [ "${progressing}" == "\"True\"" ]; then
      printInfo "Postgres deployed successfully"
    elif [ "${progressing}" == "False" ]; then
      printError "Postgres deployment failed. Aborting. Run command 'oc rollout status postgres' to get more details."
      exit 1
    elif [ ${SECONDS} -ge ${end} ]; then
      printError "Deployment timeout. Aborting."
      exit 1
    fi
}

wait_for_keycloak() {
    printInfo "Wait for Keycloak pod booting..."
    available=$(${OC_BINARY} get dc keycloak -o=jsonpath={.status.conditions[0].status})
    progressing=$(${OC_BINARY} get dc keycloak -o=jsonpath={.status.conditions[1].status})
    DEPLOYMENT_TIMEOUT_SEC=1200
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
    while [[ "${available}" != "\"True\"" || "${progressing}" != "\"True\"" ]] && [ ${SECONDS} -lt ${end} ]; do
        available=$(${OC_BINARY} get dc keycloak -o json | jq ".status.conditions[] | select(.type == \"Available\") | .status")
        progressing=$(${OC_BINARY} get dc keycloak -o json | jq ".status.conditions[] | select(.type == \"Progressing\") | .status")
        timeout_in=$((end-SECONDS))
        printInfo "Deployment is in progress...(Available.status=${available}, Progressing.status=${progressing}, ${timeout_in} seconds remain)"
        sleep ${POLLING_INTERVAL_SEC}
    done
    if [ "${progressing}" == "\"True\"" ]; then
        printInfo "Keycloak deployed successfully"
    elif [ "${progressing}" == "False" ]; then
        printError "Keycloak deployment failed. Aborting. Run command 'oc rollout status keycloak' to get more details."
    elif [ ${SECONDS} -ge ${end} ]; then
        printError "Deployment timeout. Aborting."
        exit 1
    fi
}

wait_for_che() {
    CHE_ROUTE=$(oc get route/che -o=jsonpath='{.spec.host}')
    available=$(${OC_BINARY} get dc/che -o=jsonpath={.status.conditions[0].status})
    progressing=$(${OC_BINARY} get dc/che -o=jsonpath={.status.conditions[1].status})
    DEPLOYMENT_TIMEOUT_SEC=300
    POLLING_INTERVAL_SEC=5
    end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))

    while [[ "${available}" != "\"True\"" || "${progressing}" != "\"True\"" ]] && [ ${SECONDS} -lt ${end} ]; do
      available=$(${OC_BINARY} get dc che -o json | jq '.status.conditions[] | select(.type == "Available") | .status')
      progressing=$(${OC_BINARY} get dc che -o json | jq '.status.conditions[] | select(.type == "Progressing") | .status')
      timeout_in=$((end-SECONDS))
      printInfo "Deployment is in progress...(Available.status=${available}, Progressing.status=${progressing}, ${timeout_in} seconds remain)"
      sleep ${POLLING_INTERVAL_SEC}
    done

    if [ "${progressing}" != "\"True\""  ]; then
      printError "Che deployment failed. Aborting. Run command 'oc rollout status che' to get more details."
      exit 1

    elif [ "${available}" != "\"True\""  ]; then
      printError "Che successfully deployed but it is not available at ${HTTP_PROTOCOL}://${CHE_ROUTE}"
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
  if ${OC_BINARY} get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then
    printWarning "Namespace \"${CHE_OPENSHIFT_PROJECT}\" exists."
    while $WAIT_FOR_PROJECT_TO_DELETE
    do
    { # try
      echo -n $DELETE_OPENSHIFT_PROJECT_MESSAGE
      if $CHE_REMOVE_PROJECT; then
        ${OC_BINARY} delete project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
        CHE_REMOVE_PROJECT=false
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
  printInfo "Creating namespace \"${CHE_OPENSHIFT_PROJECT}\""
  # sometimes even if the project does not exist creating a new one is impossible as it's apparently exists
  sleep 1
  ${OC_BINARY} new-project "${CHE_OPENSHIFT_PROJECT}" > /dev/null
  OUT=$?
  if [ ${OUT} -eq 1 ]; then
    printError "Failed to create namespace ${CHE_OPENSHIFT_PROJECT}. It may exist in someone else's account or namespace deletion has not been fully completed. Try again in a short while or pick a different project name -p=myProject"
    exit ${OUT}
  else
    printInfo "Namespace \"${CHE_OPENSHIFT_PROJECT}\" successfully created"
  fi
}

exposeDebugService() {
if [ "${CHE_DEBUG_SERVER}" == "true" ]; then
  printInfo "Creating an OS route to debug Che wsmaster..."
  ${OC_BINARY} create service nodeport che-debug --tcp=8000:8000
  ${OC_BINARY}  expose service che-debug
  NodePort=$(oc get service che-debug -o jsonpath='{.spec.ports[0].nodePort}')
  printInfo "Remote wsmaster debugging URL: ${CLUSTER_IP}:${NodePort}"
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


deployChe() {
    CHE_VAR_ARRAY=$(env | grep "^CHE_.")
    if [ ${#CHE_VAR_ARRAY[@]} -gt 0 ]; then
      ENV="-e ${CHE_VAR_ARRAY}"
    fi
    printInfo "Deploying Eclipse Che with the following params:
Multi-User: ${CHE_MULTIUSER}
HTTPS support: ${ENABLE_SSL}
Namespace: ${CHE_OPENSHIFT_PROJECT}
Che version: ${CHE_IMAGE_TAG}
Image: ${CHE_IMAGE_REPO}
Pull policy: ${IMAGE_PULL_POLICY}
Update strategy: ${UPDATE_STRATEGY}
Setup OpenShift oAuth: ${SETUP_OCP_OAUTH}
Environment variables:
${CHE_VAR_ARRAY}"
    CHE_INFRA_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT}
    CHE_INFRA_OPENSHIFT_OAUTH__IDENTITY__PROVIDER=NULL

    if [ "${CHE_MULTIUSER}" == "true" ]; then
      if [ "${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}" == "false" ]; then
        export KEYCLOAK_PARAM="-p CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD=false"
      fi
      ${OC_BINARY} new-app -f ${BASE_DIR}/templates/multi/postgres-template.yaml
      wait_for_postgres

      if [ "${SETUP_OCP_OAUTH}" == "true" ]; then
        # create secret with OpenShift certificate
        $OC_BINARY new-app -f ${BASE_DIR}/templates/multi/openshift-certificate-secret.yaml -p CERTIFICATE="$(cat /var/lib/origin/openshift.local.config/master/ca.crt)"
      fi

      ${OC_BINARY} new-app -f ${BASE_DIR}/templates/multi/keycloak-template.yaml \
        -p ROUTING_SUFFIX=${OPENSHIFT_ROUTING_SUFFIX} \
        -p PROTOCOL=${HTTP_PROTOCOL} \
        -p KEYCLOAK_USER=${KEYCLOAK_USER} \
        -p KEYCLOAK_PASSWORD=${KEYCLOAK_PASSWORD} \
        ${KEYCLOAK_PARAM}
      wait_for_keycloak

      if [ "${SETUP_OCP_OAUTH}" == "true" ]; then
        # register oAuth client in OpenShift
        $OC_BINARY login -u "system:admin" > /dev/null
        KEYCLOAK_ROUTE=$($OC_BINARY get route/keycloak --namespace=${CHE_OPENSHIFT_PROJECT} -o=jsonpath={'.spec.host'})
        $OC_BINARY new-app -f ${BASE_DIR}/templates/multi/oauth-client.yaml \
          -p REDIRECT_URI="http://${KEYCLOAK_ROUTE}/auth/realms/che/broker/${OCP_IDENTITY_PROVIDER_ID}/endpoint" \
          -p OCP_OAUTH_CLIENT_ID=${OCP_OAUTH_CLIENT_ID} \
          -p OCP_OAUTH_CLIENT_SECRET=${OCP_OAUTH_CLIENT_SECRET}

        # register OpenShift Identity Provider in Keycloak
        $OC_BINARY login -u "${OPENSHIFT_USERNAME}" -p "${OPENSHIFT_PASSWORD}" > /dev/null
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

isLoggedIn
createNewProject
getRoutingSuffix
deployChe
