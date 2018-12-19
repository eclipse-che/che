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

DOWNLOAD_AGENT_BINARIES_URI='${WORKSPACE_MASTER_URI}/agent-binaries/jdt.ls.tar.gz'

CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-java
LS_LAUNCHER=${LS_DIR}/launch.sh
LS_DATA_DIR=$HOME/jdtls/data

if [ ! "$CHE_WORKSPACE_JDT_LS_HEAP_OPTIONS" ]; then
    CHE_WORKSPACE_JDT_LS_HEAP_OPTIONS="-Xmx1024M"
fi

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

########################
### Install packages ###
########################

# Red Hat Enterprise Linux 7
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

# Red Hat Enterprise Linux 6
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

# Ubuntu 14.04 16.04 / Linux Mint 17
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }


# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

# Fedora 23
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    command -v ps >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" procps-ng"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} dnf -y install ${PACKAGES};
    }

# CentOS 7.1 & Oracle Linux 7.1
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

# openSUSE 13.2
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} zypper install -y ${PACKAGES};
    }

# Alpine 3.3
############
elif echo ${LINUX_TYPE} | grep -qi "alpine"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apk update
        ${SUDO} apk add ${PACKAGES};
    }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat $FILE
    exit 1
fi


#######################
### Install JDT LS ###
#######################

# Compute URI of workspace master
WORKSPACE_MASTER_URI=$(echo $CHE_API | cut -d / -f 1-3)

## Evaluate variables now that prefix is defined
eval "DOWNLOAD_AGENT_BINARIES_URI=${DOWNLOAD_AGENT_BINARIES_URI}"

CA_ARG=""
if [ -f /tmp/che/secret/ca.crt ]; then
  echo "Certificate File /tmp/che/secret/ca.crt will be used for binaries downloading"
  CA_ARG="--cacert /tmp/che/secret/ca.crt"
fi

echo Downloading java LS
curl ${CA_ARG} -sL ${DOWNLOAD_AGENT_BINARIES_URI} | tar xzf - -C ${LS_DIR}

echo writing start script to ${LS_LAUNCHER}
touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
EQUINOX_LAUNCHER=$(ls ${LS_DIR}/plugins/org.eclipse.equinox.launcher_*.jar)
echo "java -Declipse.application=org.eclipse.jdt.ls.core.id1 -Dosgi.bundles.defaultStartLevel=4  -Declipse.product=org.eclipse.jdt.ls.core.product -noverify ${CHE_WORKSPACE_JDT_LS_HEAP_OPTIONS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=4410 -jar ${EQUINOX_LAUNCHER} -configuration ./config_linux -data ${LS_DATA_DIR}" > ${LS_LAUNCHER}
