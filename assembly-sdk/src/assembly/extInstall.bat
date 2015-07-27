@REM
@REM Copyright (c) 2012-2015 Codenvy, S.A.
@REM All rights reserved. This program and the accompanying materials
@REM are made available under the terms of the Eclipse Public License v1.0
@REM which accompanies this distribution, and is available at
@REM http://www.eclipse.org/legal/epl-v10.html
@REM
@REM Contributors:
@REM   Codenvy, S.A. - initial API and implementation
@REM

@echo off
rem -------------------------------------------------------------------
rem Script to integrate 3rd-party extensions to existing Codenvy IDE.
rem -------------------------------------------------------------------

rem Specifies the location of the directory that contains 3rd-party extensions
SET "EXT_DIR_REL_PATH=ext"

rem Specifies the location of the directory that contains resource files to re-build Codenvy IDE
SET "EXT_RES_DIR_REL_PATH=sdk-resources"
SET "EXT_RES_WORK_DIR_REL_PATH=sdk-resources\temp"

rem Install every 3rd-party extension into local Maven repository
for /R %EXT_DIR_REL_PATH% %%f in (*.jar) do (
if exist %%f call mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=%%f
)

rem Prepare to re-build Codenvy IDE
call java -cp "sdk-tools\che-plugin-sdk-tools.jar" org.eclipse.che.ide.sdk.tools.InstallExtension --extDir=%EXT_DIR_REL_PATH% --extResourcesDir=%EXT_RES_DIR_REL_PATH%

rem Re-build Codenvy IDE
cd %EXT_RES_WORK_DIR_REL_PATH%
call mvn clean package -Dskip-validate-sources=true
cd ../..
1>nul  2>&1 copy /B /Y "%EXT_RES_WORK_DIR_REL_PATH%\target\*.war" "webapps\che.war"
1>nul  2>&1 rmdir /S /Q webapps\che

echo Restart Codenvy IDE if it is currently running
