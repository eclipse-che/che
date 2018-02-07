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

AGENT_BINARIES_URI=https://github.com/lhein/camel-language-server/releases/download/untagged-d42064681113e838bd59/camel-lsp-server-1.0.0-SNAPSHOT.jar
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-camel
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


###############################
### Install Apache Camel LS ###
###############################

curl -L -s ${AGENT_BINARIES_URI} -o ${LS_DIR}/camel-lsp-server.jar

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "java -jar ${LS_DIR}/camel-lsp-server.jar" > ${LS_LAUNCHER}
