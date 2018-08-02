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

command -v go >/dev/null 2>&1 || {
    echo "<<<<<<<<<<< ERROR >>>>>>>>>>>>"
    echo "Go not found in PATH"
    echo "Use an image with Go installed"
    echo "Exiting"
    exit 1
  }
GOLANG_LS_VERSION="0.1.7"
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-golang
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

mkdir -p /projects/.che
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

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
        ${SUDO} yum -y install nodejs;
    }


# Ubuntu 14.04 16.04 / Linux Mint 17
####################################
elif echo ${LINUX_TYPE} | grep -qi "ubuntu"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} apt-get update;
        ${SUDO} apt-get -y install ${PACKAGES};
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

    command -v nodejs >/dev/null 2>&1 || {
        {
            curl -sL https://deb.nodesource.com/setup_6.x | ${SUDO} bash -;
        };

        ${SUDO} apt-get update;
        ${SUDO} apt-get install -y nodejs;
    }

# Fedora 23
###########
elif echo ${LINUX_TYPE} | grep -qi "fedora"; then
    command -v ps >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" procps-ng"; }
    test "${PACKAGES}" = "" || {
        ${SUDO} dnf -y install ${PACKAGES};
    }

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
        ${SUDO} dnf -y install nodejs;
    }


# CentOS 7.1 & Oracle Linux 7.1
###############################
elif echo ${LINUX_TYPE} | grep -qi "centos"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} yum -y install ${PACKAGES};
    }

    command -v nodejs >/dev/null 2>&1 || {
        curl --silent --location https://rpm.nodesource.com/setup_6.x | ${SUDO} bash -;
        ${SUDO} yum -y install nodejs;
    }

# openSUSE 13.2
###############
elif echo ${LINUX_TYPE} | grep -qi "opensuse"; then
    test "${PACKAGES}" = "" || {
        ${SUDO} zypper install -y ${PACKAGES};
    }

    command -v nodejs >/dev/null 2>&1 || {
        ${SUDO} zypper ar http://download.opensuse.org/repositories/devel:/languages:/nodejs/openSUSE_13.1/ Node.js
        ${SUDO} zypper in nodejs
    }

else
    >&2 echo "Unrecognized Linux Type"
    >&2 cat $FILE
    exit 1
fi


#######################
## Install Golang LS ##
#######################

if [ ! -d "${LS_DIR}/node_modules" ]; then
    cd ${LS_DIR}
    echo "Installing Golang Language Server..."
    npm i go-language-server@${GOLANG_LS_VERSION}
fi

## Install LS dependencies

echo "Installing Golang LS dependencies..."

go get -v github.com/nsf/gocode
go get -v github.com/uudashr/gopkgs/cmd/gopkgs
go get -v github.com/ramya-rao-a/go-outline
go get -v github.com/acroca/go-symbols
go get -v golang.org/x/tools/cmd/guru
go get -v golang.org/x/tools/cmd/gorename
go get -v github.com/fatih/gomodifytags
go get -v github.com/haya14busa/goplay/cmd/goplay
go get -v github.com/josharian/impl
go get -v github.com/tylerb/gotype-live
go get -v github.com/rogpeppe/godef
go get -v golang.org/x/tools/cmd/godoc
go get -v github.com/zmb3/gogetdoc
go get -v golang.org/x/tools/cmd/goimports
go get -v sourcegraph.com/sqs/goreturns
go get -v github.com/golang/lint/golint
go get -v github.com/cweill/gotests/...
go get -v github.com/alecthomas/gometalinter
go get -v honnef.co/go/tools/...
go get -v github.com/sourcegraph/go-langserver
go get -v github.com/derekparker/delve/cmd/dlv

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}

echo "nodejs ${LS_DIR}/node_modules/go-language-server/out/src-vscode-mock/cli.js --stdio" > ${LS_LAUNCHER}
