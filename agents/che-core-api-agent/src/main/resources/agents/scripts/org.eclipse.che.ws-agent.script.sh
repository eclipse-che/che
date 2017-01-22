#
# Copyright (c) 2012-2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

unset PACKAGES
unset SUDO
command -v tar >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" tar"; }
command -v curl >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" curl"; }
test "$(id -u)" = 0 || SUDO="sudo"

LOCAL_AGENT_BINARIES_URI="/mnt/che/ws-agent.tar.gz"
DOWNLOAD_AGENT_BINARIES_URI='${WORKSPACE_MASTER_URI}/agent-binaries/ws-agent.tar.gz'

CHE_DIR=$HOME/che

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
${SUDO} mkdir -p /projects
${SUDO} sh -c "chown -R $(id -u -n) /projects"


INSTALL_JDK=false
command -v ${JAVA_HOME}/bin/java >/dev/null 2>&1 || {
    INSTALL_JDK=true;
} && {
    java_version=$(${JAVA_HOME}/bin/java -version 2>&1 | sed 's/.* version "\\(.*\\)\\.\\(.*\\)\\..*"/\\1\\2/; 1q')
    if [ ! -z "${java_version##*[!0-9]*}" ] && [ "${java_version}" -lt "18" ]; then
        INSTALL_JDK=true;
    fi
}

if [ ${INSTALL_JDK} = true ]; then
    export JAVA_HOME=${CHE_DIR}/jdk1.8
fi


########################
### Install packages ###
########################

# Red Hat Enterprise Linux 7
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then

    if [ ${INSTALL_JDK} = true ]; then
      PACKAGES=${PACKAGES}" java-1.8.0-openjdk-devel.x86_64"
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk $JAVA_HOME
    fi

# Ubuntu 14.04 16.04 / Linux Mint 17
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then

    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" openjdk-8-jdk-headless"
        ${SUDO} apt-get install -y software-properties-common;
        ${SUDO} add-apt-repository ppa:openjdk-r/ppa;
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk-amd64 $JAVA_HOME
    fi

# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then

    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" openjdk-8-jdk-headless";
        echo "deb http://httpredir.debian.org/debian jessie-backports main" | ${SUDO} tee --append /etc/apt/sources.list
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk-amd64 $JAVA_HOME
    fi


# Fedora 23
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then

    command -v ps >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" procps-ng"; }
    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" java-1.8.0-openjdk-devel.x86_64";
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} dnf -y install ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk $JAVA_HOME
    fi

# CentOS 7.1 & Oracle Linux 7.1
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then

    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" java-1.8.0-openjdk-devel";
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk $JAVA_HOME
    fi

# openSUSE 13.2
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then

    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" java-1_8_0-openjdk-devel";
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} zypper install -y ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk $JAVA_HOME
    fi


# Alpine 3.3
############$$
elif echo ${LINUX_TYPE} | grep -qi "alpine"; then

    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" openjdk8";
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} apk update
        ${SUDO} apk add ${PACKAGES};
    }

    # Link OpenJDK to JAVA_HOME
    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8-openjdk $JAVA_HOME
    fi

# Centos 6.6, 6.7, 6.8
############
elif echo ${LINUX_TYPE} | grep -qi "CentOS"; then

     if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" java-1.8.0-openjdk-devel";
     fi

     test "${PACKAGES}" = "" || {
         ${SUDO} yum -y install ${PACKAGES};
     }

     if [ ${INSTALL_JDK} = true ]; then
         ln -s /usr/lib/jvm/java-1.8.0-openjdk $JAVA_HOME
     fi

# Red Hat Enterprise Linux 6 
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    if [ ${INSTALL_JDK} = true ]; then
        PACKAGES=${PACKAGES}" java-1.8.0-openjdk-devel.x86_64";
    fi

    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

    if [ ${INSTALL_JDK} = true ]; then
        ln -s /usr/lib/jvm/java-1.8.0-openjdk $JAVA_HOME
    fi

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat $FILE
    exit 1
fi

########################
### Install ws-agent ###
########################

rm -rf ${CHE_DIR}/ws-agent
mkdir -p ${CHE_DIR}/ws-agent


# Compute URI of workspace master
WORKSPACE_MASTER_URI=$(echo $CHE_API | cut -d / -f 1-3)

## Evaluate variables now that prefix is defined
eval "DOWNLOAD_AGENT_BINARIES_URI=${DOWNLOAD_AGENT_BINARIES_URI}"


if [ -f "${LOCAL_AGENT_BINARIES_URI}" ] && [ -s "${LOCAL_AGENT_BINARIES_URI}" ]
then
    AGENT_BINARIES_URI="file://${LOCAL_AGENT_BINARIES_URI}"
else
    echo "Workspace Agent will be downloaded from Workspace Master"
    AGENT_BINARIES_URI=${DOWNLOAD_AGENT_BINARIES_URI}
fi

curl -s  ${AGENT_BINARIES_URI} | tar  xzf - -C ${CHE_DIR}/ws-agent

###############################################
### ws-agent run command will be added here ###
### ~/che/ws-agent/bin/catalina.sh run      ###
###############################################
