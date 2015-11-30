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
set CHE_LOCAL_CONF_DIR=%CATALINA_HOME%\conf

if not "%JAVA_HOME%"=="" goto javaHomeAlreadyDefined

FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion') DO set CurVer=%%B
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B

:javaHomeAlreadyDefined

if "%JAVA_OPTS%"=="" (set JAVA_OPTS=-Xms256m -Xmx1048m -XX:MaxPermSize=256m -server)

if "%CHE_LOGS_DIR%"=="" (set CHE_LOGS_DIR=%CATALINA_HOME%\logs)

if "%JPDA_ADDRESS%"=="" (set JPDA_ADDRESS=8000)

if "%SERVER_PORT%"=="" (set SERVER_PORT=8080)

if "%CATALINA_OPTS%"=="" (set CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dche.local.conf.dir="%CHE_LOCAL_CONF_DIR%")

if "%CLASSPATH%"=="" (set CLASSPATH=%CATALINA_HOME%\conf\;%JAVA_HOME%\lib\tools.jar)

set LOG_OPTS=-Dche.logs.dir="%CHE_LOGS_DIR%"

set JAVA_OPTS=%JAVA_OPTS% %LOG_OPTS%
echo "======="
echo %JAVA_OPTS%
echo "======="