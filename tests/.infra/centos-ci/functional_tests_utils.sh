#!/usr/bin/env bash

# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

function installOC() {
  OC_DIR_NAME=openshift-origin-client-tools-v3.11.0-0cbc58b-linux-64bit
  curl -vL  "https://github.com/openshift/origin/releases/download/v3.11.0/${OC_DIR_NAME}.tar.gz" --output ${OC_DIR_NAME}.tar.gz
  tar -xvf ${OC_DIR_NAME}.tar.gz
  cp ${OC_DIR_NAME}/oc /usr/local/bin
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
    echo "exclude=mirror1.ci.centos.org" >> /etc/yum/pluginconf.d/fastestmirror.conf
    echo "Installing epel..."
    yum install -d1 --assumeyes epel-release
    yum update --assumeyes -d1
  fi
}

function installYQ() {
  installEpelRelease
  yum install --assumeyes -d1 python-pip
  pip install yq
}

function installStartDocker() {
  yum install --assumeyes -d1 yum-utils device-mapper-persistent-data lvm2
  yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  yum install --assumeyes -d1 docker-ce
  mkdir -p /etc/docker
  echo "{ \"insecure-registries\": [\"172.30.0.0/16\"] }" > /etc/docker/daemon.json
  systemctl start docker
  docker version
}

function installMvn() {
  yum install --assumeyes -d1 centos-release-scl
  yum install --assumeyes -d1 rh-maven33
}

function installNodejs() {
  yum install --assumeyes -d1 rh-nodejs8
}

function installGit(){
  yum install --assumeyes -d1 git
}

function installWget() {
  yum -y install  wget
}

function installDependencies() {
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
}

function checkAllCreds() {
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

function archiveArtifacts() {
  set +e
  echo "Archiving artifacts from ${DATE} for ${JOB_NAME}/${BUILD_NUMBER}"
  ls -la ./artifacts.key
  chmod 600 ./artifacts.key
  chown $(whoami) ./artifacts.key
  mkdir -p ./rhche/${JOB_NAME}/${BUILD_NUMBER}/surefire-reports
  cp ./logs/*.log ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
  cp -R ./logs/artifacts/screenshots/ ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
  cp -R ./logs/artifacts/failsafe-reports/ ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
  cp ./events_report.txt ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
  rsync --password-file=./artifacts.key -Hva --partial --relative ./rhche/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
  set -e
}

function getVersionFromPom() {
  version=$(scl enable rh-maven33 "mvn -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn org.apache.maven.plugins:maven-help-plugin:evaluate -q -Dexpression=project.parent.version -DforceStdout")
  echo $version
}

function getActiveToken() {
  rm -rf cookie-file loginfile.html
  if [[ "$USERNAME" == *"preview"* ]] || [[ "$USERNAME" == *"saas"* ]]; then
    preview="prod-preview."
  else
    preview=""
  fi

  response=$(curl -s -g -X GET --header 'Accept: application/json' "https://api.${preview}openshift.io/api/users?filter[username]=$USERNAME")
  data=$(echo "$response" | jq .data)
  if [ "$data" == "[]" ]; then
    exit 1
  fi        

  #get html of developers login page
  curl -sX GET -L -c cookie-file -b cookie-file "https://auth.${preview}openshift.io/api/login?redirect=https://che.openshift.io" > loginfile.html

  #get url for login from form
  url=$(grep "form id" loginfile.html | grep -o 'http.*.tab_id=.[^\"]*')
  dataUrl="username=$USERNAME&password=$PASSWORD&login=Log+in"
  url=${url//\&amp;/\&}

  #send login and follow redirects  
  set +e
  url=$(curl -w '%{redirect_url}' -s -X POST -c cookie-file -b cookie-file -d "$dataUrl" "$url")
  found=$(echo "$url" | grep "token_json")

  while true 
  do
    url=$(curl -c cookie-file -b cookie-file -s -o /dev/null -w '%{redirect_url}' "$url")
    if [[ ${#url} == 0 ]]; then
      #all redirects were done but token was not found
      break
    fi
    found=$(echo "$url" | grep "token_json")
    if [[ ${#found} -gt 0 ]]; then
      #some redirects were done and token was found as a part of url
      break
    fi
  done
  set -e

  #extract active token
  token=$(echo "$url" | grep -o "ey.[^%]*" | head -1)
  if [[ ${#token} -gt 0 ]]; then
    echo ${token}
  else
    exit 1
  fi
}

function getVersionFromProdPreview() {
  token=$(getActiveToken)
  version=$(curl -s -X OPTIONS --header "Content-Type: application/json" --header "Authorization: Bearer ${token}" https://che.prod-preview.openshift.io/api/ | jq '.buildInfo')
  echo ${version//\"/}
}

function getVersionFromProd() {
  token=$(getActiveToken)
  version=$(curl -s -X OPTIONS --header "Content-Type: application/json" --header "Authorization: Bearer ${token}" https://che.openshift.io/api/ | jq '.buildInfo')
  echo ${version//\"/}
}
