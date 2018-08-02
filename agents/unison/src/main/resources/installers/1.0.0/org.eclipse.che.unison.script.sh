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

###############################
### Install Needed packaged ###
###############################

# Red Hat Enterprise Linux 7 
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

# Red Hat Enterprise Linux 6 
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

# Ubuntu 14.04 16.04 / Linux Mint 17 
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
    }

# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
    }

# Fedora 23
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    command -v ps >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" procps-ng"; }
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} dnf -y install ${PACKAGES};
    }

# CentOS 7.1 & Oracle Linux 7.1
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} yum -y install ${PACKAGES};
    }

# openSUSE 13.2
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} zypper install -y ${PACKAGES};
    }

# Alpine 3.3
############$$
elif echo ${LINUX_TYPE} | grep -qi "alpine"; then
    command -v unison >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" unison"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} apk update;
        ${SUDO} apk add ${PACKAGES};
    }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat /etc/os-release
    exit 1
fi
