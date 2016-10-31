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

AGENT_BINARIES_URI=https://codenvy.com/update/repository/public/download/org.eclipse.che.ls.php.binaries
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-php
LS_LAUNCHER=${LS_DIR}/launch.sh

LINUX_TYPE=$(cat /etc/os-release | grep ^ID= | tr '[:upper:]' '[:lower:]')
LINUX_VERSION=$(cat /etc/os-release | grep ^VERSION_ID=)
MACHINE_TYPE=$(uname -m)

mkdir -p ${CHE_DIR}
mkdir -p ${LS_DIR}

######################
### Install PHP LS ###
######################

curl -s ${AGENT_BINARIES_URI} | tar xzf - -C ${LS_DIR}

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "export LD_LIBRARY_PATH=${LS_DIR}/php7-minimal" > ${LS_LAUNCHER}
echo "${LS_DIR}/php7-minimal/php -c ${LS_DIR}/php7-minimal/php.ini ${LS_DIR}/php-language-server/bin/php-language-server.php" >> ${LS_LAUNCHER}
