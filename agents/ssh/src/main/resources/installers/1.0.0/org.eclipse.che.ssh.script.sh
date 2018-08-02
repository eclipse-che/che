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

if ! is_current_user_root && ! is_current_user_sudoer; then
  (>&2 echo "Current user is not a sudoer and cannot start SSH daemon. SSH service won't be available for this workspace")
  exit 1
fi

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
### Install Needed packages ###
###############################

# Red Hat Enterprise Linux 7 
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# Red Hat Enterprise Linux 6 
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# Ubuntu 14.04 16.04 / Linux Mint 17 
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} apt-get update;
       ${SUDO} apt-get -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# Fedora 23
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    command -v ps >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" procps-ng"; }
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} dnf -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# CentOS 7.1 & Oracle Linux 7.1
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} yum -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# openSUSE 13.2
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openSSH"; }
    test "${PACKAGES}" = "" || {
       ${SUDO} zypper install -y ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# Alpine 3.3
############$$
elif echo ${LINUX_TYPE} | grep -qi "alpine"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} apk update;
        ${SUDO} apk add openssh ${PACKAGES};
    }

# Centos 6.6, 6.7, 6.8
############
elif echo ${LINUX_TYPE} | grep -qi "CentOS"; then
    command -v sshd >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" openssh-server"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

else
    >&2 echo "Unrecognized Linux Type"
    command -v sshd >/dev/null 2>&1 || {
        >&2 cat $FILE;
        exit 1;
    }
    ${SUDO} sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
fi

command -v pidof >/dev/null 2>&1 && {
    pidof sshd >/dev/null 2>&1 && exit
} || {
    ps -fC sshd >/dev/null 2>&1 && exit
}

# generate host keys and running sshd

${SUDO} mkdir -p /var/run/sshd

${SUDO} /usr/bin/ssh-keygen -A

${SUDO} /usr/sbin/sshd -D

