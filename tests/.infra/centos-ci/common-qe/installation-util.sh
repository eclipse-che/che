#!/bin/bash
#
# Copyright (c) 2012-2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

set -e

SCRIPT_PATH="${BASH_SOURCE[0]}"
SCRIPT_DIR="$(dirname $SCRIPT_PATH)"

. $SCRIPT_DIR/common-util.sh

function setupTestEnvironment(){
    SCRIPT_PATH=$(readConfigProperty env.setup.environment.script.path)
    
    if [[ ! -z "$SCRIPT_PATH" ]]
    then
        ROOT_DIR_PATH=$(readConfigProperty env.root.dir.path)
        SETUP_ENV_SCRIPT_PATH=$ROOT_DIR_PATH/$SCRIPT_PATH
        SETUP_ENV_METHOD_NAME=$(readConfigProperty env.setup.environment.method.name)
        
        . $SETUP_ENV_SCRIPT_PATH
        
        eval $SETUP_ENV_METHOD_NAME
        
    else
        echo "The 'env.setup.environment.script.path' property is not set, the 'setupTestEnvironment' method is ignored"
        
    fi
}

function load_jenkins_vars() {
    if [ -e "jenkins-env.json" ]; then
        eval "$(./env-toolkit load -f jenkins-env.json \
            DEVSHIFT_TAG_LEN \
            QUAY_USERNAME \
            QUAY_PASSWORD \
            QUAY_ECLIPSE_CHE_USERNAME \
            QUAY_ECLIPSE_CHE_PASSWORD \
            JENKINS_URL \
            GIT_BRANCH \
            GIT_COMMIT \
            BUILD_NUMBER \
            ghprbSourceBranch \
            ghprbActualCommit \
            BUILD_URL \
        ghprbPullId)"
    fi
}

function install_tools() {
    # We need to disable selinux for now, XXX
    /usr/sbin/setenforce 0  || true
    
    # Get all the deps in
    yum install -d1 -y yum-utils device-mapper-persistent-data lvm2
    yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    yum install -d1 -y docker-ce \
    git
    
    service docker start
    echo 'CICO: Dependencies installed'
}

function installOC() {
    OC_DIR_NAME=openshift-origin-client-tools-v3.11.0-0cbc58b-linux-64bit
    curl -vL "https://github.com/openshift/origin/releases/download/v3.11.0/${OC_DIR_NAME}.tar.gz" --output ${OC_DIR_NAME}.tar.gz
    tar -xvf ${OC_DIR_NAME}.tar.gz
    cp ${OC_DIR_NAME}/oc /usr/local/bin
    cp ${OC_DIR_NAME}/oc /tmp
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

function installJQ() {
    installEpelRelease
    yum install --assumeyes -d1 jq
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
    
    echo "======== Launch minishift ========"
    minishift start
    
    oc login -u system:admin
    oc adm policy add-cluster-role-to-user cluster-admin developer
    oc login -u developer -p developer
    
    . "${SCRIPT_DIR}"/che-cert-generation.sh
    
    oc project default
    oc delete secret router-certs
    
    cat domain.crt domain.key > minishift.crt
    oc create secret tls router-certs --key=domain.key --cert=minishift.crt
    oc rollout status dc router
    oc rollout latest router
    oc rollout status dc router
    
    oc create namespace che
    
    cp rootCA.crt ca.crt
    oc create secret generic self-signed-certificate --from-file=ca.crt -n=che
    oc project che
}

function setup_environment(){
    load_jenkins_vars
    install_tools
    setupTestEnvironment
    installOC
    installKVM
    installAndStartMinishift
    installJQ
}
