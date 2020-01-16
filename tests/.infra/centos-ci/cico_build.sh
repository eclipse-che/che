#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

function archiveArtifacts1(){
  set +e
  JOB_NAME=che-nightly
  echo "Archiving artifacts from ${DATE} for ${JOB_NAME}/${BUILD_NUMBER}"
  ls -la ./artifacts.key
  chmod 600 ./artifacts.key
  chown $(whoami) ./artifacts.key
  mkdir -p ./che/${JOB_NAME}/${BUILD_NUMBER}
  cp  -R ./report ./che/${JOB_NAME}/${BUILD_NUMBER}/ | true
  rsync --password-file=./artifacts.key -Hva --partial --relative ./che/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
  set -e
}

set -e

echo "****** Starting RH-Che PR check $(date) ******"

total_start_time=$(date +%s)
export PR_CHECK_BUILD="true"
export BASEDIR=$(pwd)
export DEV_CLUSTER_URL=https://devtools-dev.ext.devshift.net:8443/

 eval "$(./env-toolkit load -f jenkins-env.json \
                              CHE_BOT_GITHUB_TOKEN \
                              CHE_MAVEN_SETTINGS \
                              CHE_GITHUB_SSH_KEY \
                              CHE_OSS_SONATYPE_GPG_KEY \
                              CHE_OSS_SONATYPE_PASSPHRASE \
                              QUAY_ECLIPSE_CHE_USERNAME \
                              QUAY_ECLIPSE_CHE_PASSWORD)"

source tests/.infra/centos-ci/functional_tests_utils.sh

echo "Checking credentials:"
checkAllCreds

echo "Installing dependencies:"
start=$(date +%s)
installDependencies
stop=$(date +%s)
instal_dep_duration=$(($stop - $start))
echo "Installing all dependencies lasted $instal_dep_duration seconds."

### DO NOT MERGE!!!


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


# curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && chmod +x minikube

# ./minikube version

# ./minikube start --force

# curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl

# chmod +x ./kubectl
# mv ./kubectl /usr/local/bin/kubectl

# kubectl version

# kubectl get namespaces


bash <(curl -sL  https://www.eclipse.org/che/chectl/) --channel=next


echo "====Replace CRD===="
curl -o org_v1_che_crd.yaml https://raw.githubusercontent.com/eclipse/che-operator/63402ddb5b6ed31c18b397cb477906b4b5cf7c22/deploy/crds/org_v1_che_crd.yaml
cp org_v1_che_crd.yaml /usr/local/lib/chectl/templates/che-operator/crds/

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


# set -x
# nmcli > nmclioutput
# cat nmclioutput

# firewall-cmd --permanent --new-zone dockerc
# firewall-cmd --permanent --zone dockerc --add-source 172.17.0.0/16
# firewall-cmd --permanent --zone dockerc --add-port 8443/tcp
# firewall-cmd --permanent --zone dockerc --add-port 53/udp
# firewall-cmd --permanent --zone dockerc --add-port 8053/udp
# firewall-cmd --reload


# # systemctl stop firewalld


# LOCAL_IP_ADDRESS=$(ip a show | grep -e "scope.*eth0" | grep -v ':' | cut -d/ -f1 | awk 'NR==1{print $2}')
# echo $LOCAL_IP_ADDRESS

# oc cluster up --public-hostname="${LOCAL_IP_ADDRESS}" --routing-suffix="${LOCAL_IP_ADDRESS}.nip.io" --loglevel=6

# # oc cluster up --loglevel=6

# oc login -u system:admin
# oc adm policy add-cluster-role-to-user cluster-admin developer
# oc login -u developer -p pass

# bash <(curl -sL  https://www.eclipse.org/che/chectl/) --channel=next


# echo "====Replace CRD===="
# curl -o org_v1_che_crd.yaml https://raw.githubusercontent.com/eclipse/che-operator/63402ddb5b6ed31c18b397cb477906b4b5cf7c22/deploy/crds/org_v1_che_crd.yaml
# cp org_v1_che_crd.yaml /usr/local/lib/chectl/templates/che-operator/crds/

# if chectl server:start -a operator -p openshift --k8spodreadytimeout=360000 --listr-renderer=verbose
# then
#         echo "Started succesfully"
# else
#         echo "==== oc get events ===="
#         oc get events
#         echo "==== oc get all ===="
#         oc get all
#         echo "==== docker ps ===="
#         docker ps
#         echo "==== docker ps -q | xargs -L 1 docker logs ===="
#         docker ps -q | xargs -L 1 docker logs | true
#         oc logs $(oc get pods --selector=component=che -o jsonpath="{.items[].metadata.name}") || true
#         oc logs $(oc get pods --selector=component=keycloak -o jsonpath="{.items[].metadata.name}") || true
#         curl -vL http://keycloak-che.${LOCAL_IP_ADDRESS}.nip.io/auth/realms/che/.well-known/openid-configuration
#         exit 1337
# fi

# CHE_ROUTE=$(oc get route che --template='{{ .spec.host }}')

# docker run --shm-size=256m -e TS_SELENIUM_BASE_URL="http://$CHE_ROUTE" eclipse/che-e2e:nightly

# set +x
### DO NOT MERGE!!!

# export PROJECT_NAMESPACE=prcheck-${RH_PULL_REQUEST_ID}
# export DOCKER_IMAGE_TAG="${RH_TAG_DIST_SUFFIX}"-"${RH_PULL_REQUEST_ID}"
# CHE_VERSION=$(getVersionFromPom)
# export CHE_VERSION

# echo "Running ${JOB_NAME} PR: #${RH_PULL_REQUEST_ID}, build number #${BUILD_NUMBER} for che-version:${CHE_VERSION}"
# .ci/cico_build_deploy_test_rhche.sh

# end_time=$(date +%s)
# whole_check_duration=$(($end_time - $total_start_time))
# echo "****** PR check ended at $(date) and whole run took $whole_check_duration seconds. ******"
