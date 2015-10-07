@echo off
rem
rem Copyright (c) 2012-2015 Codenvy, S.A.
rem All rights reserved. This program and the accompanying materials
rem are made available under the terms of the Eclipse Public License v1.0
rem which accompanies this distribution, and is available at
rem http://www.eclipse.org/legal/epl-v10.html
rem
rem Contributors:
rem   Codenvy, S.A. - initial API and implementation
rem
set CHE_APP_DIR="%userprofile%\AppData\Local\che\"
echo %CHE_APP_DIR%
set DIRNAME=%~dp0%
set FILE=%DIRNAME%\assembly-sdk\target\assembly-sdk-*
for /F %%i in ("%FILE%") do (set VERSION=%%~nxi)

set CATALINA_HOME=%DIRNAME%assembly-sdk\target\%VERSION%\%VERSION%
set ASSEMBLY_BIN_DIR=%CATALINA_HOME%\bin
echo %ASSEMBLY_BIN_DIR%
IF exist %ASSEMBLY_BIN_DIR% (
    TITLE "Eclipse Che"
    set CHE_HOME_DIR=%DIRNAME%assembly-sdk\target\%VERSION%\%VERSION%
    echo %CHE_HOME_DIR%
    rem We need to copy ext-server.zip and terminal to the current User directory it because
    rem docker on Windows OS an mount only shared directory in Virtual Box via boot2docker 
    rem https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
    if not exist %CHE_APP_DIR% mkdir %CHE_APP_DIR%
    copy %CHE_HOME_DIR%\ext-server\ext-server.zip "%CHE_APP_DIR%"
    if not exist %CHE_APP_DIR%\terminal mkdir %CHE_APP_DIR%\terminal
    copy %CHE_HOME_DIR%\web-terminal\terminal\* %CHE_APP_DIR%\terminal
    if not exist "%userprofile%\che\projects" mkdir "%userprofile%\che\projects"
    call %ASSEMBLY_BIN_DIR%\catalina.bat %1 %2 %3 %4 %5 %6 %7 %8
) else (
   echo The command 'mvn clean install' needs to be run first. This will build the Eclipse Che assembly.
)