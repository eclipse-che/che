#
# Copyright (c) 2012-2016 Codenvy, S.A.
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

CHE_DIR=$HOME/che
AGENT_BINARIES_URI="file:///mnt/che/terminal/websocket-terminal-\\${PREFIX}.tar.gz"
TARGET_AGENT_BINARIES_URI="file://"${CHE_DIR}"/websocket-terminal-\\${PREFIX}.tar.gz"
LINUX_TYPE=$(cat /etc/os-release | grep ^ID= | tr '[:upper:]' '[:lower:]')
LINUX_VERSION=$(cat /etc/os-release | grep ^VERSION_ID=)
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
    PACKAGES=${PACKAGES}" procps-ng"
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
############$$
elif echo ${LINUX_TYPE} | grep -qi "alpine"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apk update
        ${SUDO} apk add ${PACKAGES};
    }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat /etc/os-release
    exit 1
fi

command -v pidof >/dev/null 2>&1 && {
    pidof che-websocket-terminal >/dev/null 2>&1 && exit
} || {
    ps -fC che-websocket-terminal >/dev/null 2>&1 && exit
}


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
else
    >&2 echo "Unrecognized Machine Type"
    >&2 uname -a
    exit 1
fi

if curl -o /dev/null --silent --head --fail $(echo ${AGENT_BINARIES_URI} | sed 's/\\${PREFIX}/'${PREFIX}'/g'); then
    curl -o $(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\\${PREFIX}/'${PREFIX}'/g' | sed 's/file:\\/\\///g') -s $(echo ${AGENT_BINARIES_URI} | sed 's/\\${PREFIX}/'${PREFIX}'/g')
elif curl -o /dev/null --silent --head --fail $(echo ${AGENT_BINARIES_URI} | sed 's/-\\${PREFIX}//g'); then
    curl -o $(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\\${PREFIX}/'${PREFIX}'/g' | sed 's/file:\\/\\///g') -s $(echo ${AGENT_BINARIES_URI} | sed 's/-\\${PREFIX}//g')
fi

curl -s $(echo ${TARGET_AGENT_BINARIES_URI} | sed 's/\\${PREFIX}/'${PREFIX}'/g') | tar  xzf - -C ${CHE_DIR}

if [ -f /bin/bash ]; then
  SHELL_INTERPRETER="/bin/bash"
fi

$HOME/che/terminal/che-websocket-terminal -addr :4411 -cmd ${SHELL_INTERPRETER} -static $HOME/che/terminal/
