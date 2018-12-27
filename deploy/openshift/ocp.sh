#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

set -e

init() {

LOCAL_IP_ADDRESS=$(detectIP)
BASE_DIR=$(cd "$(dirname "$0")"; pwd)

#OS specific defaults
if [[ "$OSTYPE" == "darwin"* ]]; then
    DEFAULT_OC_PUBLIC_HOSTNAME="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_PUBLIC_IP="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_BINARY_DOWNLOAD_URL="https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-mac.zip"
else
    DEFAULT_OC_PUBLIC_HOSTNAME="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_PUBLIC_IP="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_BINARY_DOWNLOAD_URL="https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-linux-64bit.tar.gz"
fi

export OC_PUBLIC_HOSTNAME=${OC_PUBLIC_HOSTNAME:-${DEFAULT_OC_PUBLIC_HOSTNAME}}
export OC_PUBLIC_IP=${OC_PUBLIC_IP:-${DEFAULT_OC_PUBLIC_IP}}

DEFAULT_OCP_TOOLS_DIR="/tmp"
export OCP_TOOLS_DIR=${OCP_TOOLS_DIR:-${DEFAULT_OCP_TOOLS_DIR}}

export OC_BINARY_DOWNLOAD_URL=${OC_BINARY_DOWNLOAD_URL:-${DEFAULT_OC_BINARY_DOWNLOAD_URL}}

DEFAULT_OPENSHIFT_USERNAME="developer"
export OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}

DEFAULT_OPENSHIFT_PASSWORD="developer"
export OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}

DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
export CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}

DNS_PROVIDERS=(
xip.io
nip.codenvy-stg.com
)
DEFAULT_DNS_PROVIDER="nip.io"
export DNS_PROVIDER=${DNS_PROVIDER:-${DEFAULT_DNS_PROVIDER}}

DEFAULT_OPENSHIFT_ENDPOINT="https://${OC_PUBLIC_HOSTNAME}:8443"
export OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
export CHE_INFRA_KUBERNETES_MASTER__URL=${CHE_INFRA_KUBERNETES_MASTER__URL:-${OPENSHIFT_ENDPOINT}}

# OKD config local dir
DEFAULT_OKD_DIR="${HOME}/.okd"
export OKD_DIR=${OKD_DIR:-${DEFAULT_OKD_DIR}}
mkdir -p ${OKD_DIR}

# Docker image and tag for ansible-service-broker. Currently nightly, since latest does not work
DEFAULT_ORIGIN_ANSIBLE_SERVICE_BROKER_IMAGE="ansibleplaybookbundle/origin-ansible-service-broker"
DEFAULT_ORIGIN_ANSIBLE_SERVICE_BROKER_TAG="nightly"
export ORIGIN_ANSIBLE_SERVICE_BROKER_IMAGE=${ORIGIN_ANSIBLE_SERVICE_BROKER_IMAGE:-${DEFAULT_ORIGIN_ANSIBLE_SERVICE_BROKER_IMAGE}}
export ORIGIN_ANSIBLE_SERVICE_BROKER_TAG=${ORIGIN_ANSIBLE_SERVICE_BROKER_TAG:-${DEFAULT_ORIGIN_ANSIBLE_SERVICE_BROKER_TAG}}

DEFAULT_WAIT_FOR_CHE=true
export WAIT_FOR_CHE=${WAIT_FOR_CHE:-${DEFAULT_WAIT_FOR_CHE}}

DEFAULT_SETUP_OCP_OAUTH=false
export SETUP_OCP_OAUTH=${SETUP_OCP_OAUTH:-${DEFAULT_SETUP_OCP_OAUTH}}

DEFAULT_OCP_IDENTITY_PROVIDER_ID=openshift-v3
export OCP_IDENTITY_PROVIDER_ID=${OCP_IDENTITY_PROVIDER_ID:-${DEFAULT_OCP_IDENTITY_PROVIDER_ID}}

DEFAULT_OCP_OAUTH_CLIENT_ID=ocp-client
export OCP_OAUTH_CLIENT_ID=${OCP_OAUTH_CLIENT_ID:-${DEFAULT_OCP_OAUTH_CLIENT_ID}}

DEFAULT_OCP_OAUTH_CLIENT_SECRET=ocp-client-secret
export OCP_OAUTH_CLIENT_SECRET=${OCP_OAUTH_CLIENT_SECRET:-${DEFAULT_OCP_OAUTH_CLIENT_SECRET}}

DEFAULT_KEYCLOAK_USER=admin
export KEYCLOAK_USER=${KEYCLOAK_USER:-${DEFAULT_KEYCLOAK_USER}}

DEFAULT_KEYCLOAK_PASSWORD=admin
export KEYCLOAK_PASSWORD=${KEYCLOAK_PASSWORD:-${DEFAULT_KEYCLOAK_PASSWORD}}

}

test_dns_provider() {
    #add current $DNS_PROVIDER to the providers list to respect environment settings
    DNS_PROVIDERS=("$DNS_PROVIDER" "${DNS_PROVIDERS[@]}")
    for i in ${DNS_PROVIDERS[@]}
        do
        if [[ $(dig +short +time=5 +tries=1 10.0.0.1.$i) = "10.0.0.1" ]]; then
            echo "Test $i - works OK, using it as DNS provider"
            export DNS_PROVIDER="$i"
            break;
         else
            echo "Test $i DNS provider failed, trying next one."
        fi
        done
}

get_tools() {
    OC_BINARY="$OCP_TOOLS_DIR/oc"
    OC_VERSION=$(echo $OC_BINARY_DOWNLOAD_URL | cut -d '/' -f 8)
    #OS specific extract archives
    if [[ "$OSTYPE" == "darwin"* ]]; then
        OC_PACKAGE="openshift-origin-client-tools.zip"
        ARCH="unzip -d $OCP_TOOLS_DIR"
        EXTRA_ARGS=""
    else
        OC_PACKAGE="openshift-origin-client-tools.tar.gz"
        ARCH="tar --strip 1 -xzf"
        EXTRA_ARGS="-C $OCP_TOOLS_DIR"
    fi

    download_oc() {
        echo "download oc client $OC_VERSION"
        wget -O $OCP_TOOLS_DIR/$OC_PACKAGE $OC_BINARY_DOWNLOAD_URL
        eval "$ARCH" "$OCP_TOOLS_DIR"/"$OC_PACKAGE" "$EXTRA_ARGS" &>/dev/null
        rm -f "$OCP_TOOLS_DIR"/README.md "$OCP_TOOLS_DIR"/LICENSE "${OCP_TOOLS_DIR:-/tmp}"/"$OC_PACKAGE"
    }

    if [[ $(oc version 2> /dev/null | grep "oc v" | cut -d " " -f2 | cut -d '+' -f1 || true) == *"$OC_VERSION"* ]]; then
      echo "Found oc ${OC_VERSION} in PATH. Using it"
      export OC_BINARY="oc"
    elif [[ ! -f $OC_BINARY ]]; then
      download_oc
    else
      # here we check is installed version is same version defined in script, if not we update version to one that defined in script.
      if [[ $($OC_BINARY version 2> /dev/null | grep "oc v" | cut -d " " -f2 | cut -d '+' -f1 || true) != *"$OC_VERSION"* ]]; then
        rm -f "$OC_BINARY" "$OCP_TOOLS_DIR"/README.md "$OCP_TOOLS_DIR"/LICENSE
        download_oc
      fi
    fi
    export PATH=${PATH}:${OCP_TOOLS_DIR}
}

add_user_as_admin() {
    $OC_BINARY login -u system:admin >/dev/null
    echo "[OCP] adding cluster-admin role to user developer..."
    $OC_BINARY adm policy add-cluster-role-to-user cluster-admin developer
}

wait_for_automation_service_broker() {

    if [ "${APB}" == "true" ]; then
        $OC_BINARY login -u system:admin
        echo "[OCP] updating automation-broker configMap with admin sandbox role..."
        $OC_BINARY get cm/broker-config -n=openshift-automation-service-broker -o=json | sed 's/edit/admin/g' | oc apply -f -
        echo "[OCP] re-deploying openshift-automation-service-broker..."
        $OC_BINARY rollout cancel dc/openshift-automation-service-broker -n=openshift-automation-service-broker
        sleep 5
        $OC_BINARY rollout latest dc/openshift-automation-service-broker -n=openshift-automation-service-broker
        DESIRED_REPLICA_COUNT=1
        CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/openshift-automation-service-broker -n=openshift-automation-service-broker -o=jsonpath='{.status.availableReplicas}')
        DEPLOYMENT_TIMEOUT_SEC=300
        POLLING_INTERVAL_SEC=5
        end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
        while [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}" ] && [ ${SECONDS} -lt ${end} ]; do
          CURRENT_REPLICA_COUNT=$(${OC_BINARY} get dc/openshift-automation-service-broker -n=openshift-automation-service-broker -o=jsonpath='{.status.availableReplicas}')
          timeout_in=$((end-SECONDS))
          echo "Deployment is in progress...(Current replica count=${CURRENT_REPLICA_COUNT}, ${timeout_in} seconds remain)"
          sleep ${POLLING_INTERVAL_SEC}
        done

        if [ "${CURRENT_REPLICA_COUNT}" -ne "${DESIRED_REPLICA_COUNT}"  ]; then
          printError "Automation Service Broker deployment failed. Aborting"
          exit 1
        elif [ ${SECONDS} -ge ${end} ]; then
          printError "Deployment timeout. Aborting."
          exit 1
        fi
    fi
}

run_ocp() {
    test_dns_provider
    if [ "${APB}" == "true" ]; then
        export  ENABLE_COMPONENTS="--enable=service-catalog,router,registry,web-console,persistent-volumes,rhel-imagestreams,automation-service-broker"
        echo "[OCP] start OKD with '${ENABLE_COMPONENTS}'"
    fi
    $OC_BINARY cluster up --public-hostname="${OC_PUBLIC_HOSTNAME}" \
                          --routing-suffix="${OC_PUBLIC_IP}.${DNS_PROVIDER}"
    wait_for_automation_service_broker
}

deploy_che_to_ocp() {
  if [ "${DEPLOY_CHE}" == "true" ];then
    echo "Logging in to OpenShift cluster..."
    $OC_BINARY login -u "${OPENSHIFT_USERNAME}" -p "${OPENSHIFT_PASSWORD}" > /dev/null
    ${BASE_DIR}/deploy_che.sh
  fi
}

destroy_ocp() {
    if [ -d "${OKD_DIR}" ]; then
      docker run --rm -v ${OKD_DIR}:/to_remove alpine sh -c "rm -rf /to_remove/* > /dev/null 2>&1" || true
    fi
    docker run --rm -v /var/lib/origin:/to_remove alpine sh -c "rm -rf /to_remove/* > /dev/null 2>&1" || true
    $OC_BINARY login -u system:admin
    $OC_BINARY delete pvc --all
    $OC_BINARY delete all --all
    $OC_BINARY cluster down
}

remove_che_from_ocp() {
	echo "[CHE] Checking if project \"${CHE_OPENSHIFT_PROJECT}\" exists before removing..."
	WAIT_FOR_PROJECT_TO_DELETE=true
	CHE_REMOVE_PROJECT=true
	DELETE_OPENSHIFT_PROJECT_MESSAGE="[CHE] Removing Project \"${CHE_OPENSHIFT_PROJECT}\"."
	if $OC_BINARY get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then
		echo "[CHE] Project \"${CHE_OPENSHIFT_PROJECT}\" exists."
		while $WAIT_FOR_PROJECT_TO_DELETE
		do
		{ # try
			echo -n $DELETE_OPENSHIFT_PROJECT_MESSAGE
			if $CHE_REMOVE_PROJECT; then
				$OC_BINARY delete project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null
				CHE_REMOVE_PROJECT=false
			fi
			DELETE_OPENSHIFT_PROJECT_MESSAGE="."
			if ! $OC_BINARY get project "${CHE_OPENSHIFT_PROJECT}" &> /dev/null; then
				WAIT_FOR_PROJECT_TO_DELETE=false
			fi
			echo -n $DELETE_OPENSHIFT_PROJECT_MESSAGE
		} || { # catch
			echo "[CHE] Could not find project \"${CHE_OPENSHIFT_PROJECT}\" to delete."
			WAIT_FOR_PROJECT_TO_DELETE=false
		}
		done
		echo "Done!"
	else
		echo "[CHE] Project \"${CHE_OPENSHIFT_PROJECT}\" does NOT exist."
	fi

}

detectIP() {
    docker run --rm --net host eclipse/che-ip:nightly
}

parse_args() {
    HELP="valid args:
    --help - this help menu
    --run-ocp - run ocp cluster
    --enable-apb - enable ansible service broker (APBs)
    --admin - add cluster-admin role to developer user
    --destroy - destroy ocp cluster
    --deploy-che - deploy che to ocp
    --project | -p - OpenShift namespace to deploy Che (defaults to eclipse-che). Example: --project=myproject
    --secure | -s - Deploy Che with SSL enabled
    --multiuser - deploy Che in multiuser mode
    --postgres-debug - run PostgreSQL DB with Debug logging
    --no-pull - IfNotPresent pull policy for Che server deployment
    --rolling - rolling update strategy (Recreate is the default one)
    --debug - deploy Che in a debug mode, create and expose debug route
    --image-che - override default Che image. Example: --image-che=org/repo:tag. Tag is mandatory!
    --remove-che - remove existing che project
    --setup-ocp-oauth - register OCP oauth client and setup Keycloak and Che to use OpenShift Identity Provider
    --deploy-che-plugin-registry - deploy Che plugin registry
    --enable-tracing - Enable tracing and deploy Jaeger
    ===================================
    ENV vars
    CHE_IMAGE_TAG - set che-server image tag, default: nightly
    CHE_MULTIUSER - set CHE multi user mode, default: false (single user)
    OC_PUBLIC_HOSTNAME - set ocp hostname to admin console, default: host ip
    OC_PUBLIC_IP - set ocp hostname for routing suffix, default: host ip
    DNS_PROVIDER - set ocp DNS provider for routing suffix, default: nip.io
    OPENSHIFT_TOKEN - set ocp token for authentication
"
    if [ $# -eq 0 ]; then
        echo "No arguments supplied"
        echo -e "$HELP"
        exit 1
    fi

    if [[ "$@" == *"--remove-che"* ]]; then
      remove_che_from_ocp
    fi

    if [[ "$@" == *"--enable-apb"* ]]; then
       export APB="true"
    fi

    for i in "${@}"
    do
        case $i in
           --run-ocp)
               run_ocp
               shift
           ;;
           --enable-apb)
               shift
           ;;
           --admin)
               add_user_as_admin
               shift
           ;;
           --destroy)
               destroy_ocp
               shift
           ;;
           --deploy-che)
               DEPLOY_CHE=true
               shift
           ;;
           --multiuser)
               export CHE_MULTIUSER=true
               shift
           ;;
           --postgres-debug)
               export POSTGRESQL_LOG_DEBUG=true
               shift
           ;;
           --update)
               shift
           ;;
           -p=*| --project=*)
               export CHE_OPENSHIFT_PROJECT="${i#*=}"
               shift
           ;;
           --no-pull)
               export IMAGE_PULL_POLICY=IfNotPresent
               export CHE_WORKSPACE_PLUGIN__BROKER_PULL__POLICY=IfNotPresent
               shift
           ;;
           --rolling)
               export UPDATE_STRATEGY=Rolling
               shift
           ;;
           --debug)
               export CHE_DEBUG_SERVER=true
               shift
           ;;
           --image-che=*)
               export CHE_IMAGE_REPO=$(echo "${i#*=}" | sed 's/:.*//')
               export CHE_IMAGE_TAG=$(echo "${i#*=}" | sed 's/.*://')
               shift
           ;;
           -s | --secure)
               export ENABLE_SSL=true
               shift
           ;;
           --remove-che)
           shift
           ;;
           --setup-ocp-oauth)
               export SETUP_OCP_OAUTH=true
               shift
           ;;
           --help)
               echo -e "$HELP"
               exit 1
           ;;
           --deploy-che-plugin-registry)
               export DEPLOY_CHE_PLUGIN_REGISTRY=true
               shift
           ;;
           --enable-tracing)
               export CHE_TRACING_ENABLED=true
               shift
           ;;
           *)
               echo "You've passed wrong arg '$i'."
               echo -e "$HELP"
               exit 1
           ;;
        esac
    done
}

init
get_tools
parse_args "$@"
deploy_che_to_ocp
