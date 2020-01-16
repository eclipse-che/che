#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Just a script to get and build eclipse-che locally
# please send PRs to github.com/kbsingh/build-run-che

# update machine, get required deps in place
# this script assumes its being run on CentOS Linux 7/x86_64

#include commont scripts 
. ../../../../.ci/cico_common.sh
. ./cico_tests_common.sh


load_jenkins_vars

install_deps

installJQ

installOC

function archiveArtifacts1(){
  set +e
  JOB_NAME=rhopp
  echo "Archiving artifacts from ${DATE} for ${JOB_NAME}/${BUILD_NUMBER}"
  ls -la ./artifacts.key
  chmod 600 ./artifacts.key
  chown $(whoami) ./artifacts.key
  mkdir -p ./rhche/${JOB_NAME}/${BUILD_NUMBER}
  cp  -R ./report ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
#   cp ./logs/*.log ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
#   cp -R ./logs/artifacts/screenshots/ ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
#   cp -R ./logs/artifacts/failsafe-reports/ ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
#   cp ./events_report.txt ./rhche/${JOB_NAME}/${BUILD_NUMBER}/ | true
  rsync --password-file=./artifacts.key -Hva --partial --relative ./rhche/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
  set -e
}



yum install -y qemu-kvm libvirt libvirt-python libguestfs-tools virt-install

curl -L https://github.com/dhiltgen/docker-machine-kvm/releases/download/v0.10.0/docker-machine-driver-kvm-centos7 -o /usr/local/bin/docker-machine-driver-kvm
chmod +x /usr/local/bin/docker-machine-driver-kvm


systemctl enable libvirtd
systemctl start libvirtd

virsh net-list --all

curl -Lo minishift.tgz https://github.com/minishift/minishift/releases/download/v1.34.2/minishift-1.34.2-linux-amd64.tgz
tar -xvf minishift.tgz --strip-components=1
chmod +x ./minishift
mv ./minishift /usr/local/bin/minishift

minishift version
minishift config set memory 14GB
minishift config set cpus 4

minishift start

oc login -u system:admin
oc adm policy add-cluster-role-to-user cluster-admin developer
oc login -u developer -p pass


bash <(curl -sL  https://www.eclipse.org/che/chectl/) --channel=next



if chectl server:start -a operator -p openshift --k8spodreadytimeout=360000 --listr-renderer=verbose
then
        echo "Started succesfully"
else
        echo "==== oc get events ===="
        oc get events
        echo "==== oc get all ===="
        oc get all 
        # echo "==== docker ps ===="
        # docker ps
        # echo "==== docker ps -q | xargs -L 1 docker logs ===="
        # docker ps -q | xargs -L 1 docker logs | true
        oc logs $(oc get pods --selector=component=che -o jsonpath="{.items[].metadata.name}") || true
        oc logs $(oc get pods --selector=component=keycloak -o jsonpath="{.items[].metadata.name}") || true
        curl -vL http://keycloak-che.${LOCAL_IP_ADDRESS}.nip.io/auth/realms/che/.well-known/openid-configuration
        exit 1337
fi

CHE_ROUTE=$(oc get route che --template='{{ .spec.host }}')

curl -vL $CHE_ROUTE

### Create user and obtain token
KEYCLOAK_URL=$(oc get route/keycloak -o jsonpath='{.spec.host}')
KEYCLOAK_BASE_URL="http://${KEYCLOAK_URL}/auth"

ADMIN_USERNAME=admin
ADMIN_PASS=admin
TEST_USERNAME=testUser1

echo "Getting admin token"
ADMIN_ACCESS_TOKEN=$(curl -X POST $KEYCLOAK_BASE_URL/realms/master/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin" -d "password=admin" -d "grant_type=password" -d "client_id=admin-cli" |jq -r .access_token)

echo $ADMIN_ACCESS_TOKEN

echo "Creating user"

USER_JSON="{\"username\": \"${TEST_USERNAME}\",\"enabled\": true,\"emailVerified\": true,\"email\":\"test1@user.aa\"}"

echo $USER_JSON

curl -X POST $KEYCLOAK_BASE_URL/admin/realms/che/users -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" -H "Content-Type: application/json" -d "${USER_JSON}" -v

USER_ID=$(curl -X GET $KEYCLOAK_BASE_URL/admin/realms/che/users?username=${TEST_USERNAME} -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" | jq -r .[0].id)
echo "User id: $USER_ID"

echo "Updating password"

CREDENTIALS_JSON={\"type\":\"password\",\"value\":\"${TEST_USERNAME}\",\"temporary\":false}
echo $CREDENTIALS_JSON

curl -X PUT $KEYCLOAK_BASE_URL/admin/realms/che/users/${USER_ID}/reset-password -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" -H "Content-Type: application/json" -d "${CREDENTIALS_JSON}" -v

export USER_ACCESS_TOKEN=$(curl -X POST $KEYCLOAK_BASE_URL/realms/che/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "username=${TEST_USERNAME}" -d "password=${TEST_USERNAME}" -d "grant_type=password" -d "client_id=che-public" |jq -r .access_token)

### Create workspace

chectl workspace:start --access-token "$USER_ACCESS_TOKEN" -f https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/happy-path/happy-path-workspace.yaml


### Run tests
mkdir report
REPORT_FOLDER=$(pwd)/report

set +e
docker run --shm-size=256m --network host -v $REPORT_FOLDER:/tmp/e2e/report:Z -e TS_SELENIUM_BASE_URL="http://$CHE_ROUTE" -e TS_SELENIUM_MULTIUSER="true" -e TS_SELENIUM_USERNAME="${TEST_USERNAME}" -e TS_SELENIUM_PASSWORD="${TEST_USERNAME}" eclipse/che-e2e:nightly
set -e

### Archive artifacts
archiveArtifacts1
