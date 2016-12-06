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

unset SUDO
unset PACKAGES
test "$(id -u)" = 0 || SUDO="sudo"

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
    PACKAGES=${PACKAGES}" procps-ng"
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
    >&2 cat $FILE
    exit 1
fi

command -v pidof >/dev/null 2>&1 && {
    pidof sshd >/dev/null 2>&1 && exit
} || {
    ps -fC sshd >/dev/null 2>&1 && exit
}


${SUDO} mkdir -p /var/run/sshd

if echo ${LINUX_TYPE} | grep -qi "CentOS"; then
    ${SUDO} /usr/bin/ssh-keygen -q -P '' -t rsa -f ~/.ssh/id_rsa
else
    ${SUDO} /usr/bin/ssh-keygen -A
fi

${SUDO} /usr/sbin/sshd -D

