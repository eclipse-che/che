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
    if is_current_user_sudoer && ! is_current_user_root; then
        SUDO="sudo -E"
    else
        unset SUDO;
    fi
}

set_sudo_command
unset PACKAGES
command -v tar >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" tar"; }
CURL_INSTALLED=false
WGET_INSTALLED=false
command -v curl >/dev/null 2>&1 && CURL_INSTALLED=true
command -v wget >/dev/null 2>&1 && WGET_INSTALLED=true

# no curl, no wget, install curl
if [ ${CURL_INSTALLED} = false ] && [ ${WGET_INSTALLED} = false ]; then
  PACKAGES=${PACKAGES}" curl";
  CURL_INSTALLED=true
fi

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

if is_current_user_sudoer; then
    ${SUDO} mkdir -p /projects
    ${SUDO} sh -c "chown -R $(id -u -n) /projects"
fi

INSTALL_JDK=false
command -v ${JAVA_HOME}/bin/java >/dev/null 2>&1 || {
    INSTALL_JDK=true;
} && {
    java_version=$(${JAVA_HOME}/bin/java -version 2>&1 | grep version  | awk '{print $NF}' | sed 's/"//g' | cut -d '.' -f2)
    if [ -z ${java_version} ] || [ ! "${java_version}" -eq "8" ]; then
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
        ${SUDO} yum install -y ${PACKAGES};
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
        PACKAGES=${PACKAGES}" -t jessie-backports openjdk-8-jdk-headless";
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
        ${SUDO} yum install -y ${PACKAGES};
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
    tar zxf "${LOCAL_AGENT_BINARIES_URI}" -C ${CHE_DIR}/ws-agent
else
    echo "Workspace Agent will be downloaded from Workspace Master"
    AGENT_BINARIES_URI=${DOWNLOAD_AGENT_BINARIES_URI}
    if [ ${CURL_INSTALLED} = true ]; then
       CA_ARG=""
      if [ -f /tmp/che/secret/ca.crt ]; then
        echo "Certificate File /tmp/che/secret/ca.crt will be used for binaries downloading"
        CA_ARG="--cacert /tmp/che/secret/ca.crt"
      fi

      curl -s ${CA_ARG} ${AGENT_BINARIES_URI} | tar  xzf - -C ${CHE_DIR}/ws-agent
    else
      # replace https by http as wget may not be able to handle ssl
      AGENT_BINARIES_URI=$(echo ${AGENT_BINARIES_URI} | sed 's/https/http/g')

      CA_ARG=""
      if [ -f /tmp/che/secret/ca.crt ]; then
        echo "Certificate File /tmp/che/secret/ca.crt will be used for binaries downloading"
        CA_ARG="--ca-certificate /tmp/che/secret/ca.crt"
      fi

      # use wget
      wget ${CA_ARG} -qO- ${AGENT_BINARIES_URI} | tar xzf - -C ${CHE_DIR}/ws-agent
    fi

fi

DEFAULT_WSAGENT_DEBUG=false
WSAGENT_DEBUG=${WSAGENT_DEBUG:-${DEFAULT_WSAGENT_DEBUG}}

if [ "${WSAGENT_DEBUG}" = true ]; then
   export DEFAULT_WSAGENT_DEBUG_PORT="4403"
   export JPDA_ADDRESS=${WSAGENT_DEBUG_PORT:-${DEFAULT_WSAGENT_DEBUG_PORT}}

   export DEFAULT_WSAGENT_DEBUG_SUSPEND="n"
   export JPDA_SUSPEND=${WSAGENT_DEBUG_SUSPEND:-${DEFAULT_WSAGENT_DEBUG_SUSPEND}}

   ~/che/ws-agent/bin/catalina.sh jpda run
else
   ~/che/ws-agent/bin/catalina.sh run
fi
