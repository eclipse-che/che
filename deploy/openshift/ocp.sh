#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

set -e

init() {

LOCAL_IP_ADDRESS=$(detectIP)
BASE_DIR=$(cd "$(dirname "$0")"; pwd)

#OS specific defaults
if [[ "$OSTYPE" == "darwin"* ]]; then
    DEFAULT_OC_PUBLIC_HOSTNAME="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_PUBLIC_IP="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_BINARY_DOWNLOAD_URL="https://github.com/openshift/origin/releases/download/v3.7.0/openshift-origin-client-tools-v3.7.0-7ed6862-mac.zip"
    DEFAULT_JQ_BINARY_DOWNLOAD_URL="https://github.com/stedolan/jq/releases/download/jq-1.5/jq-osx-amd64"
else
    DEFAULT_OC_PUBLIC_HOSTNAME="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_PUBLIC_IP="$LOCAL_IP_ADDRESS"
    DEFAULT_OC_BINARY_DOWNLOAD_URL="https://github.com/openshift/origin/releases/download/v3.7.0/openshift-origin-client-tools-v3.7.0-7ed6862-linux-64bit.tar.gz"
    DEFAULT_JQ_BINARY_DOWNLOAD_URL="https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64"
fi

export OC_PUBLIC_HOSTNAME=${OC_PUBLIC_HOSTNAME:-${DEFAULT_OC_PUBLIC_HOSTNAME}}
export OC_PUBLIC_IP=${OC_PUBLIC_IP:-${DEFAULT_OC_PUBLIC_IP}}

export OC_BINARY_DOWNLOAD_URL=${OC_BINARY_DOWNLOAD_URL:-${DEFAULT_OC_BINARY_DOWNLOAD_URL}}
export JQ_BINARY_DOWNLOAD_URL=${JQ_BINARY_DOWNLOAD_URL:-${DEFAULT_JQ_BINARY_DOWNLOAD_URL}}

DEFAULT_CHE_MULTIUSER="false"
export CHE_MULTIUSER=${CHE_MULTIUSER:-${DEFAULT_CHE_MULTIUSER}}

DEFAULT_OPENSHIFT_USERNAME="developer"
export OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}

DEFAULT_OPENSHIFT_PASSWORD="developer"
export OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}

DNS_PROVIDERS=(
xip.io
nip.codenvy-stg.com
)
DEFAULT_DNS_PROVIDER="nip.io"
export DNS_PROVIDER=${DNS_PROVIDER:-${DEFAULT_DNS_PROVIDER}}

export OPENSHIFT_ROUTING_SUFFIX="${OC_PUBLIC_IP}.${DNS_PROVIDER}"

DEFAULT_CHE_OPENSHIFT_PROJECT="eclipse-che"
export CHE_OPENSHIFT_PROJECT=${CHE_OPENSHIFT_PROJECT:-${DEFAULT_CHE_OPENSHIFT_PROJECT}}

export OPENSHIFT_FLAVOR="ocp"

DEFAULT_OPENSHIFT_ENDPOINT="https://${OC_PUBLIC_HOSTNAME}:8443"
export OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}
export CHE_INFRA_KUBERNETES_MASTER__URL=${CHE_INFRA_KUBERNETES_MASTER__URL:-${OPENSHIFT_ENDPOINT}}

DEFAULT_ENABLE_SSL="false"
export ENABLE_SSL=${ENABLE_SSL:-${DEFAULT_ENABLE_SSL}}

DEFAULT_CHE_IMAGE_TAG="nightly"
export CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}

DEFAULT_IMAGE_PULL_POLICY="Always"
export IMAGE_PULL_POLICY=${IMAGE_PULL_POLICY:-${DEFAULT_IMAGE_PULL_POLICY}}

DEFAULT_CHE_IMAGE_REPO="eclipse/che-server"
export CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}

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
    TOOLS_DIR="/tmp"
    OC_BINARY="$TOOLS_DIR/oc"
    JQ_BINARY="$TOOLS_DIR/jq"
    OC_VERSION=$(echo $DEFAULT_OC_BINARY_DOWNLOAD_URL | cut -d '/' -f 8)
    #OS specific extract archives
    if [[ "$OSTYPE" == "darwin"* ]]; then
        OC_PACKAGE="openshift-origin-client-tools.zip"
        ARCH="unzip -d $TOOLS_DIR"
        EXTRA_ARGS=""
    else
        OC_PACKAGE="openshift-origin-client-tools.tar.gz"
        ARCH="tar --strip 1 -xzf"
        EXTRA_ARGS="-C $TOOLS_DIR"
    fi

    download_oc() {
        echo "download oc client $OC_VERSION"
        wget -q -O $TOOLS_DIR/$OC_PACKAGE $OC_BINARY_DOWNLOAD_URL
        eval "$ARCH" "$TOOLS_DIR"/"$OC_PACKAGE" "$EXTRA_ARGS" &>/dev/null
        rm -f "$TOOLS_DIR"/README.md "$TOOLS_DIR"/LICENSE "${TOOLS_DIR:-/tmp}"/"$OC_PACKAGE"
    }

    if [[ ! -f $OC_BINARY ]]; then
        download_oc
    else
        # here we check is installed version is same version defined in script, if not we update version to one that defined in script.
        if [[ $($OC_BINARY version 2> /dev/null | grep "oc v" | cut -d " " -f2 | cut -d '+' -f1 || true) != *"$OC_VERSION"* ]]; then
            rm -f "$OC_BINARY" "$TOOLS_DIR"/README.md "$TOOLS_DIR"/LICENSE
            download_oc
        fi
    fi

    if [ ! -f $JQ_BINARY ]; then
        echo "download jq..."
        wget -q -O $JQ_BINARY $JQ_BINARY_DOWNLOAD_URL
        chmod +x $JQ_BINARY
    fi
    export PATH=${PATH}:${TOOLS_DIR}
}

ocp_is_booted() {
    # we have to wait before docker registry will be started as it is staring as last container and it should be running before we perform che deploy.
    ocp_registry_container_id=$(docker ps -a  | grep openshift/origin-docker-registry | cut -d ' ' -f1)
    if [ ! -z "$ocp_registry_container_id" ];then
        ocp_registry_container_status=$(docker inspect "$ocp_registry_container_id" | $JQ_BINARY .[0] | $JQ_BINARY -r '.State.Status')
    else
        return 1
    fi
    if [[ "${ocp_registry_container_status}" == "running" ]]; then
        return 0
    else
        return 1
    fi
}

wait_ocp() {
  OCP_BOOT_TIMEOUT=120
  echo "[OCP] wait for ocp full boot..."
  ELAPSED=0
  until ocp_is_booted; do
    if [ ${ELAPSED} -eq "${OCP_BOOT_TIMEOUT}" ];then
        echo "OCP didn't started in $OCP_BOOT_TIMEOUT secs, exit"
        exit 1
    fi
    sleep 2
    ELAPSED=$((ELAPSED+1))
  done
}

run_ocp() {
    test_dns_provider
    $OC_BINARY cluster up --public-hostname="${OC_PUBLIC_HOSTNAME}" --routing-suffix="${OC_PUBLIC_IP}.${DNS_PROVIDER}"
    wait_ocp
}

deploy_che_to_ocp() {
    bash ${BASE_DIR}/deploy_che.sh --wait-che ${DEPLOY_SCRIPT_ARGS}
}

destroy_ocp() {
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
		echo "[CHE] Project \"${CHE_OPENSHIFT_PROJECT}\" does NOT exists."
	fi
	
}

detectIP() {
    docker run --rm --net host eclipse/che-ip:nightly
}

parse_args() {
    HELP="valid args:
    --help - this help menu
    --run-ocp - run ocp cluster
    --destroy - destroy ocp cluster
    --deploy-che - deploy che to ocp
    --multiuser - deploy che in multiuser mode
    --remove-che - remove existing che project
    ===================================
    ENV vars
    CHE_IMAGE_TAG - set che-server image tag, default: nightly
    CHE_MULTIUSER - set CHE multi user mode, default: false (single user)
    OC_PUBLIC_HOSTNAME - set ocp hostname to admin console, default: host ip
    OC_PUBLIC_IP - set ocp hostname for routing suffix, default: host ip
    DNS_PROVIDER - set ocp DNS provider for routing suffix, default: nip.io
    OPENSHIFT_TOKEN - set ocp token for authentication
"

    DEPLOY_SCRIPT_ARGS=""

    if [ $# -eq 0 ]; then
        echo "No arguments supplied"
        echo -e "$HELP"
        exit 1
    fi

    if [[ "$@" == *"--multiuser"* ]]; then
      CHE_MULTIUSER=true
    fi

    if [[ "$@" == *"--update"* ]]; then
      DEPLOY_SCRIPT_ARGS="-c rollupdate"
    fi

    if [[ "$@" == *"--remove-che"* ]]; then
      remove_che_from_ocp
    fi

    for i in "${@}"
    do
        case $i in
           --run-ocp)
               run_ocp
               shift
           ;;
           --destroy)
               destroy_ocp
               shift
           ;;
           --deploy-che)
               deploy_che_to_ocp
               shift
           ;;
           --multiuser)
               shift
           ;;
           --update)
               shift
           ;;
           --remove-che)
               shift
           ;;
           --help)
               echo -e "$HELP"
               exit 1
           ;;
           *)
               echo "You've passed wrong arg."
               echo -e "$HELP"
               exit 1
           ;;
        esac
    done
}

init
get_tools
parse_args "$@"
