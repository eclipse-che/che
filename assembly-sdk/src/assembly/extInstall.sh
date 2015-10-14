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


# -------------------------------------------------------------------
# Script to integrate 3rd-party extensions to existing Codenvy IDE.
# -------------------------------------------------------------------

# Specifies the location of the directory that contains 3rd-party extensions
EXT_DIR_REL_PATH="ext"

# Specifies the location of the directory that contains resource files to re-build Codenvy IDE
EXT_RES_DIR_REL_PATH="sdk-resources"
EXT_RES_WORK_DIR_REL_PATH="sdk-resources/temp"

# Install every 3rd-party extension into local Maven repository
for file in $EXT_DIR_REL_PATH/*.jar
do
    if [ -f $file ]; then
        mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dpackaging=jar -Dfile=$file
    fi
done

# Prepare to re-build Codenvy IDE
java -cp "sdk-tools/che-plugin-sdk-tools.jar" org.eclipse.che.ide.sdk.tools.InstallExtension --extDir=$EXT_DIR_REL_PATH --extResourcesDir=$EXT_RES_DIR_REL_PATH

# Re-build Codenvy IDE
cd $EXT_RES_WORK_DIR_REL_PATH
mvn -B clean package -Dskip-validate-sources=true
if [ "$?" -ne 0 ]; then
    echo "Build of che assembly failed"
    exit 1
fi
cd ../..
cp $EXT_RES_WORK_DIR_REL_PATH/target/*.war webapps/che.war
echo Restart Codenvy IDE if it is currently running
