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

AGENT_BINARIES_URI=http://cdietrich.github.io/mydsl-full.jar
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-mydsl
LS_LAUNCHER=${LS_DIR}/launch.sh

mkdir -p ${CHE_DIR}
mkdir -p ${LS_DIR}

#####################
### Install MyDsl LS ###
#####################

curl --create-dirs -o ${LS_DIR}/mydsl-full.jar ${AGENT_BINARIES_URI}

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
echo "java -jar ${LS_DIR}/mydsl-full.jar" > ${LS_LAUNCHER}
