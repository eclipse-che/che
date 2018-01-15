#
# Copyright (c) 2012-2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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

AGENT_BINARIES_URI=https://codenvy.com/update/repository/public/download/org.eclipse.che.ls.csharp.binaries/1.0.1
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-csharp
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

     command -v dotnet >/dev/null 2>&1 || {
        ${SUDO} subscription-manager repos --enable=rhel-7-server-dotnet-rpms;
        ${SUDO} yum install scl-utils rh-dotnetcore20;
        ${SUDO} scl enable rh-dotnetcore20 bash;
    }

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
        ${SUDO} yum -y install nodejs;
    }

# Red Hat Enterprise Linux 6
############################
elif echo ${LINUX_TYPE} | grep -qi "Red Hat"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum install ${PACKAGES};
    }

     command -v dotnet >/dev/null 2>&1 || {
        ${SUDO} subscription-manager repos --enable=rhel-7-server-dotnet-rpms;
        ${SUDO} yum install scl-utils rh-dotnetcore20;
        ${SUDO} scl enable rh-dotnetcore20 bash;
    }

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
        ${SUDO} yum -y install nodejs;
    }




# Install for Ubuntu 14.04, 16.04, 16.10 & Linux Mint 17, 18 (64 bit)
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

    RELEASE_NAME="trusty"
    {
        if echo ${LINUX_VERSION} | grep -qi "14.04"; then
            RELEASE_NAME="trusty"
        fi
        if echo ${LINUX_VERSION} | grep -qi "16.04"; then
            RELEASE_NAME="xenial"
        fi
        if echo ${LINUX_VERSION} | grep -qi "16.10"; then
            RELEASE_NAME="yakkety"
        fi
    };

    command -v dotnet >/dev/null 2>&1 || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install apt-transport-https;

        ${SUDO} sh -c 'echo "deb [arch=amd64] http://apt-mo.trafficmanager.net/repos/dotnet-release/ '${RELEASE_NAME}' main" > /etc/apt/sources.list.d/dotnetdev.list'
        ${SUDO} apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 417A0893

        ${SUDO} apt-get update
        ${SUDO} apt-get -y install dotnet-sdk-2.0.0-preview2-006497
    }

    command -v nodejs >/dev/null 2>&1 || {
        {
            curl -sL https://deb.nodesource.com/setup_6.x | ${SUDO} bash -;
        };

        ${SUDO} apt-get update;
        ${SUDO} apt-get install -y nodejs;
    }


# Debian 8
##########
elif echo ${LINUX_TYPE} | grep -qi "debian"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
    }

    command -v dotnet >/dev/null 2>&1 || {
        curl -L -o dotnet.tar.gz https://aka.ms/dotnet-sdk-2.0.0-preview2-linux-x64-bin;
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install libunwind8 gettext;
        ${SUDO} mkdir -p /opt/dotnet;
        ${SUDO} tar zxf dotnet.tar.gz -C /opt/dotnet;
        rm dotnet.tar.gz;
        ${SUDO} ln -s /opt/dotnet/dotnet /usr/local/bin;
    }

    command -v nodejs >/dev/null 2>&1 || {
        {
            curl -sL https://deb.nodesource.com/setup_6.x | ${SUDO} bash -;
        };

        ${SUDO} apt-get update;
        ${SUDO} apt-get install -y nodejs;
    }

# Fedora 24, 25, 26
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    PACKAGES=${PACKAGES}" procps-ng"
    test "${PACKAGES}" = "" || {
        ${SUDO} dnf -y install ${PACKAGES};
    }

    command -v dotnet >/dev/null 2>&1 || {
        curl -L -o dotnet.tar.gz https://aka.ms/dotnet-sdk-2.0.0-preview2-linux-x64-bin;
        ${SUDO} dnf -y install libunwind libicu;
        ${SUDO} mkdir -p /opt/dotnet;
        ${SUDO} tar zxf dotnet.tar.gz -C /opt/dotnet;
        rm dotnet.tar.gz;
        ${SUDO} ln -s /opt/dotnet/dotnet /usr/local/bin;
    }

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
        ${SUDO} dnf -y install nodejs;
    }


# CentOS 7.1 (64 bit) & Oracle Linux 7.1 (64 bit)
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

    command -v dotnet >/dev/null 2>&1 || {
        curl -L -o dotnet.tar.gz https://aka.ms/dotnet-sdk-2.0.0-preview2-linux-x64-bin;
        ${SUDO} yum -y install libunwind libicu;
        ${SUDO} mkdir -p /opt/dotnet;
        ${SUDO} tar zxf dotnet.tar.gz -C /opt/dotnet;
        rm dotnet.tar.gz;
        ${SUDO} ln -s /opt/dotnet/dotnet /usr/local/bin;
    }

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | bash -;
        ${SUDO} yum -y install nodejs;
    }


# SUSE Linux Enterprise Server (64 bit), openSUSE (64 bit)
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} zypper install -y ${PACKAGES};
    }

     command -v dotnet >/dev/null 2>&1 || {
        curl -L -o dotnet.tar.gz https://aka.ms/dotnet-sdk-2.0.0-preview2-linux-x64-bin
        ${SUDO} zypper install -y libunwind libicu;
        ${SUDO} mkdir -p /opt/dotnet;
        ${SUDO} tar zxf dotnet.tar.gz -C /opt/dotnet;
        rm dotnet.tar.gz;
        ${SUDO} ln -s /opt/dotnet/dotnet /usr/local/bin;
     }

     command -v nodejs >/dev/null 2>&1 || {
        ${SUDO} zypper ar http://download.opensuse.org/repositories/devel:/languages:/nodejs/openSUSE_13.1/ Node.js
        ${SUDO} zypper in nodejs
     }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat /etc/os-release
    exit 1
fi


#####################
### Install C# LS ###
#####################

curl -s ${AGENT_BINARIES_URI} | tar xzf - -C ${CHE_DIR}

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "nodejs ${LS_DIR}/node_modules/omnisharp-client/languageserver/server.js" > ${LS_LAUNCHER}
