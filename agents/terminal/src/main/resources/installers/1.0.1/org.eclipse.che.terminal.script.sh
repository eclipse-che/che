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
CURL_INSTALLED=false
WGET_INSTALLED=false
command -v curl >/dev/null 2>&1 && CURL_INSTALLED=true
command -v wget >/dev/null 2>&1 && WGET_INSTALLED=true

# no curl, no wget, install curl
if [ ${CURL_INSTALLED} = false ] && [ ${WGET_INSTALLED} = false ]; then
  PACKAGES=${PACKAGES}" curl";
  CURL_INSTALLED=true
fi

CHE_DIR=$HOME/che
LOCAL_AGENT_BINARIES_URI='/mnt/che/terminal/websocket-terminal-${PREFIX}.tar.gz'
DOWNLOAD_AGENT_BINARIES_URI='${WORKSPACE_MASTER_URI}/agent-binaries/${PREFIX}/terminal/websocket-terminal-${PREFIX}.tar.gz'
TARGET_AGENT_BINARIES_URI='file://${CHE_DIR}/websocket-terminal-${PREFIX}.tar.gz'

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
SHELL_INTERPRETER="/bin/sh"


mkdir -p ${CHE_DIR}

########################
### Install packages ###
########################

# Red Hat Enterprise Linux 7 
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then
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

# Centos 6.6, 6.7, 6.8
############
elif echo ${LINUX_TYPE} | grep -qi "CentOS"; then
     test "${PACKAGES}" = "" || {
         ${SUDO} yum -y install ${PACKAGES};
    }

# Red Hat Enterprise Linux 6 
############################

elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat $FILE
    exit 1
fi

########################
### Install Terminal ###
########################
if echo ${MACHINE_TYPE} | grep -qi "x86_64"; then
    PREFIX=linux_amd64
elif echo ${MACHINE_TYPE} | grep -qi "arm5"; then
    PREFIX=linux_arm7
elif echo ${MACHINE_TYPE} | grep -qi "arm6"; then
    PREFIX=linux_arm7
elif echo ${MACHINE_TYPE} | grep -qi "arm7"; then
    PREFIX=linux_arm7
elif echo ${MACHINE_TYPE} | grep -qi "armv7l"; then
    PREFIX=linux_arm7
else
    >&2 echo "Unrecognized Machine Type"
    >&2 uname -a
    exit 1
fi

# Compute URI of workspace master
WORKSPACE_MASTER_URI=$(echo $CHE_API | cut -d / -f 1-3)

## Evaluate variables now that prefix is defined
eval "LOCAL_AGENT_BINARIES_URI=${LOCAL_AGENT_BINARIES_URI}"
eval "DOWNLOAD_AGENT_BINARIES_URI=${DOWNLOAD_AGENT_BINARIES_URI}"
eval "TARGET_AGENT_BINARIES_URI=${TARGET_AGENT_BINARIES_URI}"

LOCAL_AGENT_PATH=
if [ -f "${LOCAL_AGENT_BINARIES_URI}" ]; then
    AGENT_BINARIES_URI="file://${LOCAL_AGENT_BINARIES_URI}"
    LOCAL_AGENT_PATH=${LOCAL_AGENT_BINARIES_URI}
elif [ -f $(echo "${LOCAL_AGENT_BINARIES_URI}" | sed "s/-${PREFIX}//g") ]; then
    AGENT_BINARIES_URI="file://"$(echo "${LOCAL_AGENT_BINARIES_URI}" | sed "s/-${PREFIX}//g")
    LOCAL_AGENT_PATH=$(echo "${LOCAL_AGENT_BINARIES_URI}" | sed "s/-${PREFIX}//g")
else
    AGENT_BINARIES_URI=${DOWNLOAD_AGENT_BINARIES_URI}
fi

# If file is already on the filesystem, use it
if [ ! -z ${LOCAL_AGENT_PATH} ]; then
  tar zxf ${LOCAL_AGENT_PATH} -C ${CHE_DIR}
else
  echo "Terminal Agent binary is downloaded remotely"
  # Use curl
  if [ ${CURL_INSTALLED} = true ]; then

    CA_ARG=""
    if [ -f /tmp/che/secret/ca.crt ]; then
      echo "Certificate File /tmp/che/secret/ca.crt will be used for binaries downloading"
      CA_ARG="--cacert /tmp/che/secret/ca.crt"
    fi

    if curl ${CA_ARG} -o /dev/null --silent --head --fail $(echo ${AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g'); then
      curl ${CA_ARG} -o $(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g' | sed 's/file:\/\///g') -s $(echo ${AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g')
    elif curl ${CA_ARG} -o /dev/null --silent --head --fail $(echo ${AGENT_BINARIES_URI} | sed 's/-\${PREFIX}//g'); then
      curl ${CA_ARG} -o $(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g' | sed 's/file:\/\///g') -s $(echo ${AGENT_BINARIES_URI} | sed 's/-\${PREFIX}//g')
    fi
    curl -s $(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g') | tar  xzf - -C ${CHE_DIR}
  else
    CA_ARG=""
    if [ -f /tmp/che/secret/ca.crt ]; then
      echo "Certificate File /tmp/che/secret/ca.crt will be used for binaries downloading"
      CA_ARG="--ca-certificate /tmp/che/secret/ca.crt"
    fi

    # replace https by http as wget may not be able to handle ssl
    AGENT_BINARIES_URI=$(echo ${AGENT_BINARIES_URI} | sed 's/https/http/g')

    # use wget
    WGET_SPIDER="wget --spider"
    if wget  2>&1 | grep -q BusyBox; then
      WGET_SPIDER="wget -s"
    fi
    LOCAL_DOWNLOAD=$(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g' | sed 's/file:\/\///g')
    if ${WGET_SPIDER} ${CA_ARG} -q $(echo ${AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g') >/dev/null; then
      wget ${CA_ARG} -qO ${LOCAL_DOWNLOAD} $(echo ${AGENT_BINARIES_URI} | sed 's/\${PREFIX}/'${PREFIX}'/g')
    elif ${WGET_SPIDER} ${CA_ARG} -q $(echo ${AGENT_BINARIES_URI} | sed 's/-\${PREFIX}//g'); then
      wget ${CA_ARG} -qO- ${LOCAL_DOWNLOAD} $(echo ${AGENT_BINARIES_URI} | sed 's/-\${PREFIX}//g')
    fi
    tar xzf ${LOCAL_DOWNLOAD} -C ${CHE_DIR}
  fi
fi



if [ -f /bin/bash ]; then
    SHELL_INTERPRETER="/bin/bash"
fi

TERMINAL_PORT=${CHE_SERVER_TERMINAL_PORT:-4411}

$HOME/che/terminal/che-websocket-terminal -addr :${TERMINAL_PORT} -cmd ${SHELL_INTERPRETER}
