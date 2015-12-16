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

REM Check docker-machine env variables have been added.
IF "%DOCKER_TLS_VERIFY%"=="" GOTO docker
IF "%DOCKER_HOST%"=="" GOTO docker
IF "%DOCKER_CERT_PATH%"=="" GOTO docker
IF "%DOCKER_MACHINE_NAME%"=="" GOTO docker

REM Test to see if docker client is working and reaches docker-machine.
call docker ps 2> nul
IF ERRORLEVEL 1 GOTO docker

REM Set CHE_HOME variable if not already set
IF "%CHE_HOME%"=="" (
  FOR %%i in ("%~dp0..") do set "CHE_HOME=%%~fi"
)

IF "%CHE_LOCAL_CONF_DIR%"=="" (
  SET CHE_LOCAL_CONF_DIR=%CHE_HOME%\conf
)

set CATALINA_HOME=%CHE_HOME%\tomcat
set CATALINA_BASE=%CHE_HOME%\tomcat
set ASSEMBLY_BIN_DIR=%CATALINA_HOME%\bin

if "%CHE_LOGS_DIR%"=="" (
	SET CHE_LOGS_DIR=%CATALINA_HOME%\logs)
)

IF exist %ASSEMBLY_BIN_DIR% (
    TITLE "Eclipse Che"
    call %ASSEMBLY_BIN_DIR%\che.bat %1 %2 %3 %4 %5 %6 %7 %8
) else (
    echo Invalid Eclipse Che root directory found.
    GOTO setup
)

GOTO end

:docker
echo !!! Docker is not properly configured.
echo Did you start docker-machine and set DOCKER_ environment variables?
GOTO END

:setup
echo. 
echo Che setup for Windows:
echo REQUIRED: docker-machine must be installed and running a machine
echo REQUIRED: Set DOCKER_ variables to values provided by docker-machine
echo OPTIONAL: Set CHE_LOCAL_CONF_DIR to directory where che.properties located
echo.
echo See "Pre-Reqs" in docs at eclipse.org/che for more setup guidance.
GOTO end

:end