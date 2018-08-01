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
unset PYTHON_DEPS
command -v tar >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" tar"; }
command -v curl >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" curl"; }
command -v python3.5 >/dev/null 2>&1 || { PYTHON_DEPS=${PYTHON_DEPS}" python3.5"; }
command -v pip3 >/dev/null 2>&1 || { PYTHON_DEPS=${PYTHON_DEPS}" pip3"; }

AGENT_BINARIES_URI=https://codenvy.com/update/repository/public/download/org.eclipse.che.ls.python.binaries/1.0.3
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-python
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

########################
### Install packages ###
########################

# Red Hat Enterprise Linux 7
############################
if echo ${LINUX_TYPE} | grep -qi "rhel"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

    command -v python3.5 >/dev/null 2>&1 || {
        ${SUDO} yum-config-manager --enable rhel-server-rhscl-7-rpms;
        ${SUDO} yum -y install rh-python35 bzip2;
        export LD_LIBRARY_PATH="/opt/rh/rh-python35/root/usr/lib64"
        export PATH="/opt/rh/rh-python35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
        echo "export LD_LIBRARY_PATH=/opt/rh/rh-python35/root/usr/lib64" >> $HOME/.bashrc
        echo "export PATH=/opt/rh/rh-python35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" >> $HOME/.bashrc
    }

# Red Hat Enterprise Linux 6
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

    test "${PYTHON_DEPS}" = "" || {
        ${SUDO} yum-config-manager --enable rhel-server-rhscl-7-rpms;
        ${SUDO} yum -y install rh-python35 bzip2;
        export LD_LIBRARY_PATH="/opt/rh/rh-python35/root/usr/lib64"
        export PATH="/opt/rh/rh-python35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
        echo "export LD_LIBRARY_PATH=/opt/rh/rh-python35/root/usr/lib64" >> $HOME/.bashrc
        echo "export PATH=/opt/rh/rh-python35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" >> $HOME/.bashrc
    }


# Ubuntu 14.04 16.04 / Linux Mint 17
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

    test "${PYTHON_DEPS}" = "" || {
        if echo ${LINUX_VERSION} | grep -qi "14.04"; then
            DEADSNAKES="/etc/apt/sources.list.d/deadsnakes.list";
            ${SUDO} touch ${DEADSNAKES};
            echo "deb http://ppa.launchpad.net/fkrull/deadsnakes/ubuntu trusty main" | ${SUDO} tee --append ${DEADSNAKES};
            echo "deb-src http://ppa.launchpad.net/fkrull/deadsnakes/ubuntu trusty main" | ${SUDO} tee --append ${DEADSNAKES};
            ${SUDO} gpg --keyserver keyserver.ubuntu.com --recv-keys DB82666C;
            ${SUDO} gpg --export DB82666C | ${SUDO} apt-key add -;

            ${SUDO} apt-get update;
            ${SUDO} apt-get install -y python3.5 bzip2;
            ${SUDO} curl https://bootstrap.pypa.io/ez_setup.py -o - | ${SUDO} python3.5
            ${SUDO} easy_install pip
        else
           ${SUDO} apt-get update;
           ${SUDO} apt-get install -y python3.5;
           ${SUDO} apt-get install -y python3-pip;
        fi
    }


# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

    test "${PYTHON_DEPS}" = "" || {
        DEADSNAKES="/etc/apt/sources.list.d/deadsnakes.list";
        ${SUDO} touch ${DEADSNAKES};
        echo "deb http://ppa.launchpad.net/fkrull/deadsnakes/ubuntu trusty main" | ${SUDO} tee --append ${DEADSNAKES};
        echo "deb-src http://ppa.launchpad.net/fkrull/deadsnakes/ubuntu trusty main" | ${SUDO} tee --append ${DEADSNAKES};
        ${SUDO} gpg --keyserver keyserver.ubuntu.com --recv-keys DB82666C;
        ${SUDO} gpg --export DB82666C | ${SUDO} apt-key add -;

        ${SUDO} apt-get update;
        ${SUDO} apt-get install -y python3.5 bzip2;
        ${SUDO} curl https://bootstrap.pypa.io/ez_setup.py -o - | ${SUDO} python3.5
        ${SUDO} easy_install pip
    }

# Fedora 23
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    PACKAGES=${PACKAGES}" procps-ng"
    test "${PACKAGES}" = "" || {
        ${SUDO} dnf -y install ${PACKAGES};
    }

    test "${PYTHON_DEPS}" = "" || {
        ${SUDO} dnf -y install python35 bzip2;
        ${SUDO} curl https://bootstrap.pypa.io/ez_setup.py -o - | ${SUDO} python3.5
        ${SUDO} easy_install pip
    }


# CentOS 7.1 & Oracle Linux 7.1
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

    test "${PYTHON_DEPS}" = "" || {

        ${SUDO} yum -y install centos-release-scl;
        ${SUDO} yum -y install rh-python35 bzip2;
        export LD_LIBRARY_PATH="/opt/rh/rh-python35/root/usr/lib64"
        export PATH="/opt/rh/rh-python35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
        echo "export LD_LIBRARY_PATH=/opt/rh/rh-python35/root/usr/lib64" >> $HOME/.bashrc
        echo "export PATH=/opt/rh/rh-python35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" >> $HOME/.bashrc
    }

# openSUSE 13.2
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} zypper install -y ${PACKAGES};
    }

    test "${PYTHON_DEPS}" = "" || {
        ${SUDO} zypper ar -f http://download.opensuse.org/repositories/home:/Ledest:/bashisms/openSUSE_13.2/ home:Ledest:bashisms
        ${SUDO} zypper --no-gpg-checks ref
        ${SUDO} zypper install -y python3
        ${SUDO} zypper install -y python3-pip
    }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat $FILE
    exit 1
fi


#########################
### Install Python LS ###
#########################

curl -s ${AGENT_BINARIES_URI} | tar xzf - -C ${LS_DIR}

cd ${LS_DIR} && ${SUDO} pip3 install --process-dependency-links .

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "pyls" > ${LS_LAUNCHER}
