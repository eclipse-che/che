#
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

unset PACKAGES
unset SUDO
command -v tar >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" tar"; }
command -v curl >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" curl"; }
command -v wget >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" wget"; }
test "$(id -u)" = 0 || SUDO="sudo -E"

AGENT_BINARIES_URI=https://codenvy.com/update/repository/public/download/org.eclipse.che.ls.json.binaries
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-clangd
LS_LAUNCHER=${LS_DIR}/launch.sh

if [ -f /etc/centos-release ]; then
    FILE="/etc/centos-release"
    LINUX_TYPE=$(cat $FILE | awk '{print $1}')
 elif [ -f /etc/redhat-release ]; then
    FILE="/etc/redhat-release"
    LINUX_TYPE=$(cat $FILE | cut -c 1-8)
 else
    FILE="/etc/os-release"
    LINUX_TYPE=$(cat $FILE | grep ^ID= | tr '[:upper:]' '[:lower:]')
    LINUX_VERSION=$(cat $FILE | grep ^VERSION_ID=)
fi

MACHINE_TYPE=$(uname -m)

mkdir -p ${CHE_DIR}
mkdir -p ${LS_DIR}

#########################
#### Install packages ###
#########################
#
# Red Hat Enterprise Linux 7
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} yum install ${PACKAGES};
   }

   command -v nodejs >/dev/null 2>&1 || {
       echo "LLVM / Clang 5.0 not supported on Red Hat Enterprise Linux 7.";
       exit 1;
   }

# Red Hat Enterprise Linux 6
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} yum install ${PACKAGES};
   }

   command -v nodejs >/dev/null 2>&1 || {
       echo "LLVM / Clang 5.0 not supported on Red Hat Enterprise Linux 6.";
       exit 1;
   }


# Ubuntu 14.04 16.04 / Linux Mint 17
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
   }

   command -v clangd-5.0 >/dev/null 2>&1 || {
       {
           wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key|sudo apt-key add -;
           # Fingerprint: 6084 F3CF 814B 57C1 CF12 EFD5 15CF 4D18 AF4F 7421
           ${SUDO} apt-add-repository "deb http://apt.llvm.org/xenial/ llvm-toolchain-xenial-5.0 main"; 
       };

       ${SUDO} apt-get update;
       ${SUDO} apt-get install -y clang-tools-5.0;
       # temporary workaround
    #    wget http://llvm-jenkins.debian.net/view/Ubuntu%20Xenial/job/llvm-toolchain-xenial-5.0-binaries/architecture=amd64,distribution=xenial/lastSuccessfulBuild/artifact/libllvm5.0_5.0.1~svn319952-1~exp1_amd64.deb;
    #    wget http://llvm-jenkins.debian.net/view/Ubuntu%20Xenial/job/llvm-toolchain-xenial-5.0-binaries/architecture=amd64,distribution=xenial/lastSuccessfulBuild/artifact/libclang1-5.0_5.0.1~svn319952-1~exp1_amd64.deb;
    #    wget http://llvm-jenkins.debian.net/view/Ubuntu%20Xenial/job/llvm-toolchain-xenial-5.0-binaries/architecture=amd64,distribution=xenial/lastSuccessfulBuild/artifact/libclang-common-5.0-dev_5.0.1~svn319952-1~exp1_amd64.deb;
    #    wget http://llvm-jenkins.debian.net/view/Ubuntu%20Xenial/job/llvm-toolchain-xenial-5.0-binaries/architecture=amd64,distribution=xenial/lastSuccessfulBuild/artifact/clang-5.0_5.0.1~svn319952-1~exp1_amd64.deb;
    #    wget http://llvm-jenkins.debian.net/view/Ubuntu%20Xenial/job/llvm-toolchain-xenial-5.0-binaries/architecture=amd64,distribution=xenial/lastSuccessfulBuild/artifact/clang-tools-5.0_5.0.1~svn319952-1~exp1_amd64.deb;
    #    ${SUDO} apt-get install -f libjsoncpp1 libobjc-5-dev libobjc4 gcc-5-base lib32gcc1 lib32stdc++6 libc6-i386;
    #    ${SUDO} dpkg -i dpkg -i clang-5.0_5.0.1~svn319952-1~exp1_amd64.deb clang-tools-5.0_5.0.1~svn319952-1~exp1_amd64.deb libclang1-5.0_5.0.1~svn319952-1~exp1_amd64.deb libclang-common-5.0-dev_5.0.1~svn319952-1~exp1_amd64.deb libllvm5.0_5.0.1~svn319952-1~exp1_amd64.deb;
       ${SUDO} ln -s /usr/bin/clangd-5.0 /usr/bin/clangd
   }


# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
   }

   command -v nodejs >/dev/null 2>&1 || {
       {
           wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key|sudo apt-key add -;
           # Fingerprint: 6084 F3CF 814B 57C1 CF12 EFD5 15CF 4D18 AF4F 7421
           ${SUDO} apt-add-repository "deb http://apt.llvm.org/jessie/ llvm-toolchain-jessie-5.0 main"; 
       };

       ${SUDO} apt-get update;
       ${SUDO} apt-get install -y clang-tools-5.0;
       ${SUDO} ln -s /usr/bin/clangd-5.0 /usr/bin/clangd
   }

## Fedora 23
############
#elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
#    command -v ps >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" procps-ng"; }
#    test "${PACKAGES}" = "" || {
#        ${SUDO} dnf -y install ${PACKAGES};
#    }
#
#    command -v nodejs >/dev/null 2>&1 || {
#        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
#        ${SUDO} dnf -y install nodejs;
#    }
#
#
## CentOS 7.1 & Oracle Linux 7.1
################################
#elif echo ${LINUX_TYPE} | grep -qi "centos"; then
#    test "${PACKAGES}" = "" || {
#        ${SUDO} yum -y install ${PACKAGES};
#    }
#
#    command -v nodejs >/dev/null 2>&1 || {
#        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
#        ${SUDO} yum -y install nodejs;
#    }
#
## openSUSE 13.2
################
#elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
#    test "${PACKAGES}" = "" || {
#        ${SUDO} zypper install -y ${PACKAGES};
#    }
#
#    command -v nodejs >/dev/null 2>&1 || {
#        ${SUDO} zypper ar http://download.opensuse.org/repositories/devel:/languages:/nodejs/openSUSE_13.1/ Node.js
#        ${SUDO} zypper in nodejs
#    }
#
else
   >&2 echo "Unrecognized Linux Type"
   >&2 cat $FILE
   exit 1
fi


#########################
### Install Clangd LS ###
#########################

#curl -s ${AGENT_BINARIES_URI} | tar xzf - -C ${LS_DIR}

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "tee -a /home/user/clangd-input.log | clangd | tee -a /home/user/clangd-output.log" > ${LS_LAUNCHER}
