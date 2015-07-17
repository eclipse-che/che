#!/bin/sh
#
# Startup script for Codenvy SDK.  Downloads Tomcat for running projects within the IDE if necessary.


TOMCAT="tomcat"
TOMCAT_IDE_DIR="assembly-sdk/target/tomcat-ide"

if [ ! -d "${TOMCAT_IDE_DIR}" ]
then
  unzip assembly-sdk/target/*.zip -d assembly-sdk/target/tomcat-ide
 # echo "$(tput setaf 1)ERROR: Looks like you have not installed the Codenvy SDK."$(tput sgr0)
 # echo "$(tput setaf 1)ERROR: Please run 'mvn clean install' and try again."$(tput sgr0)
 # echo "$(tput setaf 1)ERROR: For more information, please see the Codenvy SDK README:"$(tput sgr0)
 # echo "$(tput setaf 1)ERROR:     https://github.com/codenvy/che"$(tput sgr0)
 # exit 1
fi

cd "${TOMCAT_IDE_DIR}"

if [ ! -d "$TOMCAT" ]
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

echo "$(tput setaf 2)INFO: Launching Codenvy SDK"$(tput sgr0)
sleep 1
pwd
cd bin
pwd
./che.sh $*
