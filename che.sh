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
  echo "$(tput setaf 2)INFO: No Tomcat found for runners"$(tput sgr0)
  echo "$(tput setaf 2)INFO: Downloading Apache Tomcat for runners"$(tput sgr0)

  sleep 2

  tomcatVersion="7.0.50"
  tomcatDir="apache-tomcat-"${tomcatVersion}
  tomcatBinUrl="http://archive.apache.org/dist/tomcat/tomcat-7/v${tomcatVersion}/bin/apache-tomcat-${tomcatVersion}.zip"

#  curl -# -f -o ${tomcatDir}.zip ${tomcatBinUrl}

  if [ $? -ne 0 ]
  then
    echo "$(tput setaf 1)ERROR: Unable to download Tomcat ${tomcatVersion} from ${tomcatBinUrl}"$(tput sgr0)
    exit 1
  fi

  unzip -q ${tomcatDir}
  rm apache-tomcat-${tomcatVersion}.zip
  mv ${tomcatDir} ${TOMCAT}
  rm -rf ${tomcatDir}

  echo "$(tput setaf 2)INFO: Tomcat ${tomcatVersion} for runners successfully downloaded"$(tput sgr0)
fi

echo "$(tput setaf 2)INFO: Launching Eclipse Che"$(tput sgr0)
sleep 1
pwd
cd bin
pwd
./che.sh $*
