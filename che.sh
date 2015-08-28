#!/bin/sh
#
# Copyright (c) 2012-2015 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

BASEDIR=$(dirname $0)
VERSION=`basename $BASEDIR/assembly-sdk/target/assembly-sdk-*/`

ASSEMBLY_BIN_DIR=$BASEDIR/assembly-sdk/target/$VERSION/$VERSION/bin

if [ ! -d "${ASSEMBLY_BIN_DIR}" ]
then
  echo "$(tput setaf 1)The command 'mvn clean install' needs to be run first. This will build the Eclipse Che assembly."$(tput sgr0)
  exit 1
fi

$ASSEMBLY_BIN_DIR/che.sh $*

