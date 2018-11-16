#
# Copyright (c) 2012-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

is_current_user_root() {
    test "$(id -u)" = 0
}

is_current_user_sudoer() {
    sudo -n true > /dev/null 2>&1
}

set_sudo_command() {
    if is_current_user_sudoer && ! is_current_user_root; then SUDO="sudo -E"; else unset SUDO; fi
}

set_sudo_command
unset PACKAGES
command -v tar >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" tar"; }
command -v curl >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" curl"; }
command -v wget >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" wget"; }

CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-clangd
LS_LAUNCHER=${LS_DIR}/launch.sh
CLANGD_VERSION=6.0
CLANGD_BINARY=clangd

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

   command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       echo "LLVM / Clang ${CLANGD_VERSION} not supported on Red Hat Enterprise Linux 7.";
       exit 1;
   }

# Red Hat Enterprise Linux 6
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} yum install ${PACKAGES};
   }

   command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       echo "LLVM / Clang ${CLANGD_VERSION} not supported on Red Hat Enterprise Linux 6.";
       exit 1;
   }


# Ubuntu 14.04 16.04 / Linux Mint 17
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
   }

   command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       {
           wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key|sudo apt-key add -;
           # Fingerprint: 6084 F3CF 814B 57C1 CF12 EFD5 15CF 4D18 AF4F 7421
           ${SUDO} apt-add-repository "deb http://apt.llvm.org/xenial/ llvm-toolchain-xenial-${CLANGD_VERSION} main"; 
       };

       ${SUDO} apt-get update;
       ${SUDO} apt-get install -y clang-tools-${CLANGD_VERSION};
       ${SUDO} ln -s /usr/bin/clangd-${CLANGD_VERSION} /usr/bin/clangd
   }


# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
   }

   command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       {
           wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key|sudo apt-key add -;
           # Fingerprint: 6084 F3CF 814B 57C1 CF12 EFD5 15CF 4D18 AF4F 7421
           ${SUDO} apt-add-repository "deb http://apt.llvm.org/jessie/ llvm-toolchain-jessie-${CLANGD_VERSION} main"; 
       };

       ${SUDO} apt-get update;
       ${SUDO} apt-get install -y clang-tools-${CLANGD_VERSION};
       ${SUDO} ln -s /usr/bin/clangd-${CLANGD_VERSION} /usr/bin/clangd
   }

## Fedora 23
############
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} dnf -y install ${PACKAGES};
    }

    command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       echo "LLVM / Clang ${CLANGD_VERSION} not supported on Fedora 23.";
       exit 1;
    }

## CentOS 7.1 & Oracle Linux 7.1
################################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

    command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       echo "LLVM / Clang ${CLANGD_VERSION} not supported on CentOS.";
       exit 1;
    }

## openSUSE 13.2
################
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
   test "${PACKAGES}" = "" || {
       ${SUDO} zypper install -y ${PACKAGES};
   }

   command -v ${CLANGD_BINARY} >/dev/null 2>&1 || {
       echo "LLVM / Clang ${CLANGD_VERSION} not supported on OpenSUSE 13.2.";
       exit 1;
   }

else
   >&2 echo "Unrecognized Linux Type"
   >&2 cat $FILE
   exit 1
fi


#########################
### Install Clangd LS ###
#########################

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "tee -a ${HOME}/clangd-input.log | clangd -disable-symbolication -pretty -resource-dir=/usr/include/ -enable-snippets | tee -a ${HOME}/clangd-output.log" > ${LS_LAUNCHER}
