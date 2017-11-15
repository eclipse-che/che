#!/bin/bash

set -e

init() {
#OS specific defaults
if [[ "$OSTYPE" == "darwin"* ]]; then
    DEFAULT_OC_PUBLIC_HOSTNAME="192.168.65.2"
    DEFAULT_OC_PUBLIC_IP="192.168.65.2"
    [ -z "$CHE_OAUTH_GITHUB_CLIENTID" ] && export CHE_OAUTH_GITHUB_CLIENTID=d210c15f1a0c026a808c;
    [ -z "$CHE_OAUTH_GITHUB_CLIENTSECRET" ] && export CHE_OAUTH_GITHUB_CLIENTSECRET=cc2589e6eed89ca460c8dbd13b9342cfddbe8098;
else
    DEFAULT_OC_PUBLIC_HOSTNAME="127.0.0.1"
    DEFAULT_OC_PUBLIC_IP="127.0.0.1"
    [ -z "$CHE_OAUTH_GITHUB_CLIENTID" ] && export CHE_OAUTH_GITHUB_CLIENTID=23638e66bf0d53bdc777;
    [ -z "$CHE_OAUTH_GITHUB_CLIENTSECRET" ] && export CHE_OAUTH_GITHUB_CLIENTSECRET=e20edd408a4becda5f6ccb83abb4b1e7a432fe46;
fi
export OC_PUBLIC_HOSTNAME=${OC_PUBLIC_HOSTNAME:-${DEFAULT_OC_PUBLIC_HOSTNAME}}
export OC_PUBLIC_IP=${OC_PUBLIC_IP:-${DEFAULT_OC_PUBLIC_IP}}

DEFAULT_CHE_MULTIUSER="false"
export CHE_MULTIUSER=${CHE_MULTIUSER:-${DEFAULT_CHE_MULTIUSER}}

DEFAULT_OPENSHIFT_USERNAME="developer"
export OPENSHIFT_USERNAME=${OPENSHIFT_USERNAME:-${DEFAULT_OPENSHIFT_USERNAME}}

DEFAULT_OPENSHIFT_PASSWORD="developer"
export OPENSHIFT_PASSWORD=${OPENSHIFT_PASSWORD:-${DEFAULT_OPENSHIFT_PASSWORD}}

DEFAULT_OPENSHIFT_NAMESPACE_URL="eclipse-che.${OC_PUBLIC_IP}.nip.io"
export OPENSHIFT_NAMESPACE_URL=${OPENSHIFT_NAMESPACE_URL:-${DEFAULT_OPENSHIFT_NAMESPACE_URL}}

DEFAULT_OPENSHIFT_FLAVOR="ocp"
export OPENSHIFT_FLAVOR=${OPENSHIFT_FLAVOR:-${DEFAULT_OPENSHIFT_FLAVOR}}

DEFAULT_OPENSHIFT_ENDPOINT="https://${OC_PUBLIC_HOSTNAME}:8443"
export OPENSHIFT_ENDPOINT=${OPENSHIFT_ENDPOINT:-${DEFAULT_OPENSHIFT_ENDPOINT}}

DEFAULT_ENABLE_SSL="false"
export ENABLE_SSL=${ENABLE_SSL:-${DEFAULT_ENABLE_SSL}}

DEFAULT_CHE_IMAGE_TAG="nightly"
export CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}

DEFAULT_IMAGE_PULL_POLICY="Always"
export IMAGE_PULL_POLICY=${IMAGE_PULL_POLICY:-${DEFAULT_IMAGE_PULL_POLICY}}

DEFAULT_CHE_IMAGE_REPO="eclipse/che-server"
export CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}

DEFAULT_IMAGE_INIT="eclipse/che-init"
export IMAGE_INIT=${IMAGE_INIT:-${DEFAULT_IMAGE_INIT}}:${CHE_IMAGE_TAG}

DEFAULT_CONFIG_DIR="/tmp/config"
export CONFIG_DIR=${CONFIG_DIR:-${DEFAULT_CONFIG_DIR}}
}

get_tools() {
    TOOLS_DIR="/tmp"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        OC_PACKAGE="openshift-origin-client-tools-v3.6.0-c4dd4cf-mac.zip"
        JQ_PACKAGE="jq-osx-amd64"
        ARCH="unzip -d $TOOLS_DIR"
        EXTRA_ARGS=""
    else
        OC_PACKAGE="openshift-origin-client-tools-v3.6.0-c4dd4cf-linux-64bit.tar.gz"
        JQ_PACKAGE="jq-linux64"
        ARCH="tar --strip 1 -xzf"
        EXTRA_ARGS="-C $TOOLS_DIR"
    fi
    OC_URL=https://github.com/openshift/origin/releases/download/v3.6.0/$OC_PACKAGE
    JQ_URL=https://github.com/stedolan/jq/releases/download/jq-1.5/$JQ_PACKAGE
    OC_BINARY="$TOOLS_DIR/oc"
    JQ_BINARY="$TOOLS_DIR/jq"

    if [ ! -f $OC_BINARY ]; then
        echo "download oc client..."
        wget -q -O $TOOLS_DIR/$OC_PACKAGE $OC_URL
        eval "$ARCH" "$TOOLS_DIR"/"$OC_PACKAGE" "$EXTRA_ARGS" &>/dev/null
        rm -rf "$TOOLS_DIR"/README.md "$TOOLS_DIR"/LICENSE "${TOOLS_DIR:-/tmp}"/"$OC_PACKAGE"
    fi

    if [ ! -f $JQ_BINARY ]; then
        echo "download jq..."
        wget -q -O $JQ_BINARY $JQ_URL
        chmod +x $JQ_BINARY
    fi
    export PATH=${PATH}:${TOOLS_DIR}
}

ocp_is_booted() {
    # for now we check only openshift registry because this is enough
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
    $OC_BINARY cluster up --public-hostname="${OC_PUBLIC_HOSTNAME}" --routing-suffix="${OC_PUBLIC_IP}.nip.io"
    wait_ocp
}

deploy_che_to_ocp() {
    #Repull init image only if DEFAULT_IMAGE_PULL_POLICY is set to Always
    if [ $DEFAULT_IMAGE_PULL_POLICY == "Always" ]; then
        docker pull "$IMAGE_INIT"
    fi
    docker run -t --rm -v /var/run/docker.sock:/var/run/docker.sock -v "${CONFIG_DIR}":/data -e IMAGE_INIT="$IMAGE_INIT" -e CHE_MULTIUSER="$CHE_MULTIUSER" eclipse/che-cli:nightly destroy --quiet --skip:pull --skip:nightly
    docker run -t --rm -v /var/run/docker.sock:/var/run/docker.sock -v "${CONFIG_DIR}":/data -e IMAGE_INIT="$IMAGE_INIT" -e CHE_MULTIUSER="$CHE_MULTIUSER" eclipse/che-cli:nightly config --skip:pull --skip:nightly
    cd "${CONFIG_DIR}/instance/config/openshift/scripts/"
    bash deploy_che.sh
    wait_until_server_is_booted
}

server_is_booted() {
  PING_URL="http://che-$OPENSHIFT_NAMESPACE_URL"
  HTTP_STATUS_CODE=$(curl -I -k "${PING_URL}" -s -o /dev/null --write-out '%{http_code}')
  if [[ "${HTTP_STATUS_CODE}" = "200" ]] || [[ "${HTTP_STATUS_CODE}" = "302" ]]; then
    return 0
  else
    return 1
  fi
}

wait_until_server_is_booted() {
  SERVER_BOOT_TIMEOUT=300
  echo "[CHE] wait CHE pod booting..."
  ELAPSED=0
  until server_is_booted || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    sleep 2
    ELAPSED=$((ELAPSED+1))
  done
}

check_workspace_status() {
  STATUS_URL="http://che-${OPENSHIFT_NAMESPACE_URL}/api/workspace/${ws_id}"
  WS_STATUS=$(curl -s "${STATUS_URL}" | $JQ_BINARY -r '.status')
  if [[ "${WS_STATUS}" == *"$1"* ]]; then
    return 0
  else
    return 1
  fi
}

wait_workspace_status() {
  STATUS=$1
  WS_BOOT_TIMEOUT=300
  echo "[TEST] wait che workspace status is ${STATUS}..."
  ELAPSED=0
  until check_workspace_status "${STATUS}" || [ ${ELAPSED} -eq "${WS_BOOT_TIMEOUT}" ]; do
    sleep 2
    ELAPSED=$((ELAPSED+1))
  done
}

run_test() {
    #TODO FIX for multi user need to handle auth
    echo "[TEST] run CHE workspace test"
    ws_name="ocp-test-$(date +%s)"

    # create workspace
    ws_create=$(curl -s "http://che-${OPENSHIFT_NAMESPACE_URL}/api/workspace?namespace=che&attribute=stackId:java-centos" \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H 'Accept: application/json, text/plain, */*' \
    --data-binary '{"commands":[{"commandLine":"mvn clean install -f ${current.project.path}","name":"build","type":"mvn","attributes":{"goal":"Build","previewUrl":""}}],"projects":[{"tags":["maven","spring","java"],"commands":[{"commandLine":"mvn -f ${current.project.path} clean install \ncp ${current.project.path}/target/*.war $TOMCAT_HOME/webapps/ROOT.war","name":"build","type":"mvn","attributes":{"previewUrl":"","goal":"Build"}},{"commandLine":"$TOMCAT_HOME/bin/catalina.sh run 2>&1","name":"run tomcat","type":"custom","attributes":{"previewUrl":"http://${server.port.8080}","goal":"Run"}},{"commandLine":"$TOMCAT_HOME/bin/catalina.sh stop","name":"stop tomcat","type":"custom","attributes":{"previewUrl":"","goal":"Run"}},{"commandLine":"mvn -f ${current.project.path} clean install \ncp ${current.project.path}/target/*.war $TOMCAT_HOME/webapps/ROOT.war \n$TOMCAT_HOME/bin/catalina.sh run 2>&1","name":"build and run","type":"mvn","attributes":{"previewUrl":"http://${server.port.8080}","goal":"Run"}},{"commandLine":"mvn -f ${current.project.path} clean install \ncp ${current.project.path}/target/*.war $TOMCAT_HOME/webapps/ROOT.war \n$TOMCAT_HOME/bin/catalina.sh jpda run 2>&1","name":"debug","type":"mvn","attributes":{"previewUrl":"http://${server.port.8080}","goal":"Debug"}}],"projects":[],"links":[],"mixins":[],"problems":[],"category":"Samples","projectType":"maven","source":{"location":"https://github.com/che-samples/web-java-spring.git","type":"git","parameters":{}},"description":"A basic example using Spring servlets. The app returns values entered into a submit form.","displayName":"web-java-spring","options":{},"name":"web-java-spring","path":"/web-java-spring","attributes":{"language":["java"]},"type":"maven"}],"defaultEnv":"default","environments":{"default":{"recipe":{"location":"rhche/centos_jdk8","type":"dockerimage"},"machines":{"dev-machine":{"agents":["org.eclipse.che.terminal","org.eclipse.che.ws-agent","com.redhat.bayesian.lsp"],"servers":{},"attributes":{"memoryLimitBytes":"2147483648"}}}}},"name":"'${ws_name}'","links":[]}' \
    --compressed )
    [[ "$ws_create" == *"created"* ]] || exit 1
    [[ "$ws_create" == *"STOPPED"* ]] || exit 1
    ws_id=$(echo "${ws_create}" | $JQ_BINARY -r '.id')
    [[ "$ws_id" == *"workspace"* ]] || exit 1
    echo "[TEST] workspace '$ws_name' created succesfully"

    # start workspace
    curl -s "http://che-${OPENSHIFT_NAMESPACE_URL}/api/workspace/${ws_id}/runtime?environment=default" \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H 'Accept: application/json, text/plain, */*' \
    -H 'Connection: keep-alive' \
    --data-binary '{}' \
    --compressed
   wait_workspace_status "RUNNING"
   echo "[TEST] workspace '$ws_name'started succesfully"
   #TODO maybe add more checks that state is good

   # stop workspace
   ws_stop=$(curl -s "http://che-${OPENSHIFT_NAMESPACE_URL}/api/workspace/${ws_id}/runtime?create-snapshot=false" -X DELETE \
   -H 'Accept: application/json, text/plain, */*' \
   -H 'Connection: keep-alive' \
   --compressed \
   -o /dev/null \
   --write-out '%{http_code}')
   [[ "$ws_stop" = "204" ]] || exit 1
   wait_workspace_status "STOPPED"
   echo "[TEST] workspace '$ws_name' stopped succesfully"

   # remove workspace
   ws_remove=$(curl -s "http://che-${OPENSHIFT_NAMESPACE_URL}/api/workspace/${ws_id}" -X DELETE \
   -H 'Accept: application/json, text/plain, */*' \
   -H 'Connection: keep-alive' \
   --compressed \
   -o /dev/null \
   --write-out '%{http_code}')
   [[ "$ws_remove" = "204" ]] || exit 1
   check_ws_removed=$(curl -s "http://che-${OPENSHIFT_NAMESPACE_URL}/api/workspace" \
   -H 'Accept: application/json, text/plain, */*' \
   -H 'Connection: keep-alive' \
   --compressed)
   [[ "$check_ws_removed" != *"${ws_id}"* ]] || exit 1
   echo "[TEST] workspace '$ws_name' removed succesfully"
}

destroy_ocp() {
    $OC_BINARY login -u system:admin
    $OC_BINARY delete pvc --all
    $OC_BINARY delete all --all
    $OC_BINARY cluster down
}

parse_args() {
    HELP="valid args: \\n
    --run-ocp - run ocp cluster\\n
    --destroy - destroy ocp cluster \\n
    --deploy-che - deploy che to ocp \\n
    --multiuser - deploy che in multiuser mode \\n
    --test -  run simple test which will create > start > stop > remove CHE workspace\\n
    =================================== \\n
    ENV vars \\n
    CHE_IMAGE_TAG - set CHE images tag, default: nightly \\n
    CHE_MULTIUSER - set CHE multi user mode, default: false (single user) \\n
"



    if [ $# -eq 0 ]; then
        echo "No arguments supplied"
        echo -e "$HELP"
        exit 1
    fi

    if [[ "$@" == *"--multiuser"* ]]; then
      CHE_MULTIUSER=true
    fi

    init

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
           --test)
               run_test
               shift
           ;;
        esac
    done
}

get_tools
parse_args "$@"
