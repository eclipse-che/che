#!/usr/bin/env bash

# Copyright (c) 2020 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

buildAndDeployArtifacts() {
  set -x
  scl enable rh-maven33 'mvn clean install -U -Pintegration'
  if [[ $? -eq 0 ]]; then
    echo 'Build Success!'
    echo 'Going to deploy artifacts'
  else
    die_with 'Build Failed!'
  fi
}

testImages() {
  echo "Going to build and push docker images"
  set -e
  set -o pipefail

  TAG=$1
  if [[ ${TAG} != "nightly" ]]; then #if given tag 'nightly' means that don't need to checkout and going to build master
    git checkout ${TAG}
  fi
  REGISTRY="quay.io"
  ORGANIZATION="eclipse"
  if [[ -n "${QUAY_ECLIPSE_CHE_USERNAME}" ]] && [[ -n "${QUAY_ECLIPSE_CHE_PASSWORD}" ]]; then
    docker login -u "${QUAY_ECLIPSE_CHE_USERNAME}" -p "${QUAY_ECLIPSE_CHE_PASSWORD}" "${REGISTRY}"
  else
    echo "Could not login, missing credentials for pushing to the '${ORGANIZATION}' organization"
    return
  fi

  # stop / rm all containers
  if [[ $(docker ps -aq) != "" ]]; then
    docker rm -f $(docker ps -aq)
  fi

  # KEEP RIGHT ORDER!!!
  DOCKER_FILES_LOCATIONS=(
    dockerfiles/endpoint-watcher
    dockerfiles/keycloak
    dockerfiles/postgres
    dockerfiles/dev
    dockerfiles/che
    dockerfiles/dashboard-dev
    dockerfiles/e2e
  )

  IMAGES_LIST=(
    eclipse/che-endpoint-watcher
    eclipse/che-keycloak
    eclipse/che-postgres
    eclipse/che-dev
    eclipse/che-server
    eclipse/che-dashboard-dev
    eclipse/che-e2e
  )

  # BUILD IMAGES
  for image_dir in ${DOCKER_FILES_LOCATIONS[@]}; do
    bash $(pwd)/${image_dir}/build.sh --tag:${TAG}
    if [[ ${image_dir} == "dockerfiles/che" ]]; then
      #CENTOS SINGLE USER
      BUILD_ASSEMBLY_DIR=$(echo assembly/assembly-main/target/eclipse-che-*/eclipse-che-*/)
      LOCAL_ASSEMBLY_DIR="${image_dir}/eclipse-che"
      if [[ -d "${LOCAL_ASSEMBLY_DIR}" ]]; then
        rm -r "${LOCAL_ASSEMBLY_DIR}"
      fi
      cp -r "${BUILD_ASSEMBLY_DIR}" "${LOCAL_ASSEMBLY_DIR}"
      docker build -t ${ORGANIZATION}/che-server:${TAG}-centos -f $(pwd)/${image_dir}/Dockerfile.centos $(pwd)/${image_dir}/
    fi
    if [[ $? -ne 0 ]]; then
      echo "ERROR:"
      echo "build of '${image_dir}' image is failed!"
      exit 1
    fi
  done

  echo '=========================== LIST OF IMAGES ==========================='
  docker images -a
  echo '=========================== LIST OF IMAGES ==========================='
}

function installOC() {
  OC_DIR_NAME=openshift-origin-client-tools-v3.11.0-0cbc58b-linux-64bit
  curl -vL "https://github.com/openshift/origin/releases/download/v3.11.0/${OC_DIR_NAME}.tar.gz" --output ${OC_DIR_NAME}.tar.gz
  tar -xvf ${OC_DIR_NAME}.tar.gz
  cp ${OC_DIR_NAME}/oc /usr/local/bin
  cp ${OC_DIR_NAME}/oc /tmp
}

function installJQ() {
  installEpelRelease
  yum install --assumeyes -d1 jq
}

function installEpelRelease() {
  if yum repolist | grep epel; then
    echo "Epel already installed, skipping instalation."
  else
    #excluding mirror1.ci.centos.org
    echo "exclude=mirror1.ci.centos.org" >>/etc/yum/pluginconf.d/fastestmirror.conf
    echo "Installing epel..."
    yum install -d1 --assumeyes epel-release
    yum update --assumeyes -d1
  fi
}

function installYQ() {
  installEpelRelease
  yum install --assumeyes -d1 python3-pip
  pip3 install --upgrade setuptools
  pip3 install yq
}

function installStartDocker() {
  yum install --assumeyes -d1 yum-utils device-mapper-persistent-data lvm2
  yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  yum install --assumeyes -d1 docker-ce
  mkdir -p /etc/docker
  echo "{ \"insecure-registries\": [\"172.30.0.0/16\"] }" >/etc/docker/daemon.json
  systemctl start docker
  docker version
}

function installMvn() {
  yum install --assumeyes -d1 centos-release-scl
  yum install --assumeyes -d1 rh-maven33
}

function installNodejs() {
  curl -sL https://rpm.nodesource.com/setup_10.x | bash -
  yum install -y nodejs
}

function insalllYarn() {
  yum-config-manager --add-repo https://dl.yarnpkg.com/rpm/yarn.repo
  yum install -y yarn
}

function installGit() {
  yum install --assumeyes -d1 git
}

function installWget() {
  yum -y install wget
}

function installGssCompiler() {
  yum install -y gcc-c++ make
}

function installDependencies() {
  echo "======== Installing dependencies: ========"
  start=$(date +%s)

  installEpelRelease
  installYQ
  installStartDocker
  installJQ
  installOC
  installGit
  installWget
  # Getting dependencies ready
  yum install --assumeyes -d1 \
    patch \
    pcp \
    bzip2 \
    golang \
    make \
    java-1.8.0-openjdk \
    java-1.8.0-openjdk-devel
  installMvn
  installNodejs
  insalllYarn

  stop=$(date +%s)
  instal_dep_duration=$(($stop - $start))
  echo "======== Installing all dependencies lasted $instal_dep_duration seconds. ========"

}

function checkAllCreds() {
  echo "======== Checking credentials: ========"
  CREDS_NOT_SET="false"

  echo ${#QUAY_ECLIPSE_CHE_USERNAME}
  echo ${#QUAY_ECLIPSE_CHE_PASSWORD}

  if [[ -z "${QUAY_ECLIPSE_CHE_USERNAME}" || -z "${QUAY_ECLIPSE_CHE_PASSWORD}" ]]; then
    echo "Docker registry credentials not set"
    CREDS_NOT_SET="true"
  fi

  if [[ "${CREDS_NOT_SET}" = "true" ]]; then
    echo "Failed to parse jenkins secure store credentials"
    exit 2
  else
    echo "Credentials set successfully."
  fi
}

function installKVM() {
  echo "======== Start to install KVM virtual machine ========"

  yum install -y qemu-kvm libvirt libvirt-python libguestfs-tools virt-install

  curl -L https://github.com/dhiltgen/docker-machine-kvm/releases/download/v0.10.0/docker-machine-driver-kvm-centos7 -o /usr/local/bin/docker-machine-driver-kvm
  chmod +x /usr/local/bin/docker-machine-driver-kvm

  systemctl enable libvirtd
  systemctl start libvirtd

  virsh net-list --all
  echo "======== KVM has been installed successfully ========"
}

function installAndStartMinishift() {
  echo "======== Start to install minishift ========"
  curl -Lo minishift.tgz https://github.com/minishift/minishift/releases/download/v1.34.2/minishift-1.34.2-linux-amd64.tgz
  tar -xvf minishift.tgz --strip-components=1
  chmod +x ./minishift
  mv ./minishift /usr/local/bin/minishift

  #Setup GitHub token for minishift
  if [ -z "$CHE_BOT_GITHUB_TOKEN" ]
  then
    echo "\$CHE_BOT_GITHUB_TOKEN is empty. Minishift start might fail with GitGub API rate limit reached."
  else
    echo "\$CHE_BOT_GITHUB_TOKEN is set, checking limits."
    GITHUB_RATE_REMAINING=$(curl -slL "https://api.github.com/rate_limit?access_token=$CHE_BOT_GITHUB_TOKEN" | jq .rate.remaining)
    if [ "$GITHUB_RATE_REMAINING" -gt 1000 ]
    then
      echo "Github rate greater than 1000. Using che-bot token for minishift startup."
      export MINISHIFT_GITHUB_API_TOKEN=$CHE_BOT_GITHUB_TOKEN
    else
      echo "Github rate is lower than 1000. *Not* using che-bot for minishift startup."
      echo "If minishift startup fails, please try again later."
    fi
  fi

  minishift version
  minishift config set memory 14GB
  minishift config set cpus 4

  echo "======== Lunch minishift ========"
  minishift start
}

function installCheCtl() {
  echo "======== Start to install chectl ========"
  bash <(curl -sL https://www.eclipse.org/che/chectl/) --channel=next
  echo "======== chectl has been installed successfully ========"
}

function getOpenshiftLogs() {
    oc logs $(oc get pods --selector=component=che -o jsonpath="{.items[].metadata.name}")  || true
    oc logs $(oc get pods --selector=component=keycloak -o jsonpath="{.items[].metadata.name}") || true
}

function deployCheIntoCluster() {
  echo "======== Start to install CHE ========"
  if chectl server:start -a operator -p openshift --k8spodreadytimeout=360000 $1 $2; then
    echo "Started succesfully"
    oc get checluster -o yaml
  else
    echo "======== oc get events ========"
    oc get events
    echo "======== oc get all ========"
    oc get all
    # echo "==== docker ps ===="
    # docker ps
    # echo "==== docker ps -q | xargs -L 1 docker logs ===="
    # docker ps -q | xargs -L 1 docker logs | true
    getOpenshiftLogs
    curl -vL http://keycloak-che.${LOCAL_IP_ADDRESS}.nip.io/auth/realms/che/.well-known/openid-configuration || true
    oc get checluster -o yaml || true
    exit 1337
  fi
}

function loginToOpenshiftAndSetDevRole() {
  oc login -u system:admin
  oc adm policy add-cluster-role-to-user cluster-admin developer
  oc login -u developer -p pass
}

function archiveArtifacts() {
  JOB_NAME=$1
  DATE=$(date +"%m-%d-%Y-%H-%M")
  echo "Archiving artifacts from ${DATE} for ${JOB_NAME}/${BUILD_NUMBER}"
  ls -la ./artifacts.key
  chmod 600 ./artifacts.key
  chown $(whoami) ./artifacts.key
  mkdir -p ./che/${JOB_NAME}/${BUILD_NUMBER}
  cp -R ./report ./che/${JOB_NAME}/${BUILD_NUMBER}/ | true
  rsync --password-file=./artifacts.key -Hva --partial --relative ./che/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
}

function defineCheRoute(){
CHE_ROUTE=$(oc get route che --template='{{ .spec.host }}')
  echo "====== Check CHE ROUTE ======"
  curl -vL $CHE_ROUTE
}

createTestWorkspaceAndRunTest() {
  defineCheRoute
   ### Create workspace
  chectl workspace:start --access-token "$USER_ACCESS_TOKEN" -f https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/happy-path/happy-path-workspace.yaml

  ### Create directory for report
  mkdir report
  REPORT_FOLDER=$(pwd)/report
  ### Run tests
  docker run --shm-size=256m --network host -v $REPORT_FOLDER:/tmp/e2e/report:Z -e TS_SELENIUM_BASE_URL="http://$CHE_ROUTE" -e TS_SELENIUM_MULTIUSER="true" -e TS_SELENIUM_USERNAME="${TEST_USERNAME}" -e TS_SELENIUM_PASSWORD="${TEST_USERNAME}" -e TS_SELENIUM_LOAD_PAGE_TIMEOUT=420000 quay.io/eclipse/che-e2e:nightly
}

function createTestUserAndObtainUserToken() {

  ### Create user and obtain token
  KEYCLOAK_URL=$(oc get route/keycloak -o jsonpath='{.spec.host}')
  KEYCLOAK_BASE_URL="http://${KEYCLOAK_URL}/auth"

  ADMIN_USERNAME=admin
  ADMIN_PASS=admin
  TEST_USERNAME=testUser1

  echo "======== Getting admin token ========"
  ADMIN_ACCESS_TOKEN=$(curl -X POST $KEYCLOAK_BASE_URL/realms/master/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin" -d "password=admin" -d "grant_type=password" -d "client_id=admin-cli" | jq -r .access_token)
  echo $ADMIN_ACCESS_TOKEN

  echo "========Creating user========"
  USER_JSON="{\"username\": \"${TEST_USERNAME}\",\"enabled\": true,\"emailVerified\": true,\"email\":\"test1@user.aa\"}"
  echo $USER_JSON

  curl -X POST $KEYCLOAK_BASE_URL/admin/realms/che/users -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" -H "Content-Type: application/json" -d "${USER_JSON}" -v
  USER_ID=$(curl -X GET $KEYCLOAK_BASE_URL/admin/realms/che/users?username=${TEST_USERNAME} -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" | jq -r .[0].id)
  echo "========User id: $USER_ID========"

  echo "========Updating password========"
  CREDENTIALS_JSON={\"type\":\"password\",\"value\":\"${TEST_USERNAME}\",\"temporary\":false}
  echo $CREDENTIALS_JSON

  curl -X PUT $KEYCLOAK_BASE_URL/admin/realms/che/users/${USER_ID}/reset-password -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" -H "Content-Type: application/json" -d "${CREDENTIALS_JSON}" -v
  export USER_ACCESS_TOKEN=$(curl -X POST $KEYCLOAK_BASE_URL/realms/che/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=${TEST_USERNAME}" -d "password=${TEST_USERNAME}" -d "grant_type=password" -d "client_id=che-public" | jq -r .access_token)
  echo "========User Access Token: $USER_ACCESS_TOKEN "
}

function setupEnvs() {
  eval "$(./env-toolkit load -f jenkins-env.json -r \
    CHE_BOT_GITHUB_TOKEN \
    CHE_MAVEN_SETTINGS \
    CHE_GITHUB_SSH_KEY \
    ^BUILD_NUMBER$ \
    CHE_OSS_SONATYPE_GPG_KEY \
    CHE_OSS_SONATYPE_PASSPHRASE \
    QUAY_ECLIPSE_CHE_USERNAME \
    QUAY_ECLIPSE_CHE_PASSWORD)"

  export PATH=$PATH:/opt/rh/rh-maven33/root/bin
}

function configureGithubTestUser() {
  echo "Configure GitHub test users"
  cd /root/payload
  mkdir -p che_local_conf_dir
  export CHE_LOCAL_CONF_DIR=/root/payload/che_local_conf_dir/
  rm -f che_local_conf_dir/selenium.properties
  echo "github.username=che6ocpmulti" >> che_local_conf_dir/selenium.properties
  echo "github.password=CheMain2017" >> che_local_conf_dir/selenium.properties
  echo "github.auxiliary.username=iedexmain1" >> che_local_conf_dir/selenium.properties
  echo "github.auxiliary.password=CodenvyMain15" >> che_local_conf_dir/selenium.properties
}

function installDockerCompose() {
  echo "Install docker compose"
  sudo curl -L "https://github.com/docker/compose/releases/download/1.25.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
}

function seleniumTestsSetup() {
  echo "Start selenium tests"
  cd /root/payload
  export CHE_INFRASTRUCTURE=openshift
  defineCheRoute

  mvn clean install -pl :che-selenium-test -am -DskipTests=true -U
  configureGithubTestUser
}

function saveSeleniumTestResult() {
  mkdir -p /root/payload/report
  mkdir -p /root/payload/report/site
  cp -r /root/payload/tests/legacy-e2e/che-selenium-test/target/site report
}

function createIndentityProvider() {
  CHE_MULTI_USER_GITHUB_CLIENTID_OCP=04cbc0f8172109322223
  CHE_MULTI_USER_GITHUB_SECRET_OCP=a0a9b8602bb0916d322223e71b7ed92036563b7a
  CHE_OPENSHIFT_PROJECT=eclipse-che
  keycloakPodName=$(oc get pod --namespace=$CHE_OPENSHIFT_PROJECT | grep keycloak | awk '{print $1}')
  /tmp/oc exec $keycloakPodName --namespace=$CHE_OPENSHIFT_PROJECT -- /opt/jboss/keycloak/bin/kcadm.sh create identity-provider/instances -r che -s alias=github -s providerId=github -s enabled=true -s storeToken=true -s addReadTokenRoleOnCreate=true -s 'config.useJwksUrl="true"' -s config.clientId=$CHE_MULTI_USER_GITHUB_CLIENTID_OCP -s config.clientSecret=$CHE_MULTI_USER_GITHUB_SECRET_OCP -s 'config.defaultScope="repo,user,write:public_key"' --no-config --server http://localhost:8080/auth --user admin --password admin --realm master
}

function runDevfileTestSuite() {
  defineCheRoute
  ### Create directory for report
  mkdir report
  REPORT_FOLDER=$(pwd)/report
  ### Run tests
  docker run --shm-size=1g --net=host  --ipc=host -v $REPORT_FOLDER:/tmp/e2e/report:Z \
  -e TS_SELENIUM_BASE_URL="http://$CHE_ROUTE" \
  -e TS_SELENIUM_LOG_LEVEL=DEBUG \
  -e TS_SELENIUM_MULTIUSER=true \
  -e TS_SELENIUM_USERNAME="${TEST_USERNAME}" \
  -e TS_SELENIUM_PASSWORD="${TEST_USERNAME}" \
  -e TEST_SUITE=test-all-devfiles -e TS_SELENIUM_DEFAULT_TIMEOUT=300000 \
  -e TS_SELENIUM_LOAD_PAGE_TIMEOUT=240000 \
  -e TS_SELENIUM_WORKSPACE_STATUS_POLLING=20000 \
  quay.io/eclipse/che-e2e:nightly
}
