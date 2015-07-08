@REM
@REM CODENVY CONFIDENTIAL
@REM __________________
@REM
@REM [2012] - [2013] Codenvy, S.A.
@REM All Rights Reserved.
@REM
@REM NOTICE: All information contained herein is, and remains
@REM the property of Codenvy S.A. and its suppliers,
@REM if any. The intellectual and technical concepts contained
@REM herein are proprietary to Codenvy S.A.
@REM and its suppliers and may be covered by U.S. and Foreign Patents,
@REM patents in process, and are protected by trade secret or copyright law.
@REM Dissemination of this information or reproduction of this material
@REM is strictly forbidden unless prior written permission is obtained
@REM from Codenvy S.A..
@REM
@echo off
set CODENVY_LOCAL_CONF_DIR=%CATALINA_HOME%\conf\

if "%JAVA_OPTS%"=="" (set JAVA_OPTS=-Xms256m -Xmx1048m -XX:MaxPermSize=256m -server)

if "%CODENVY_LOGS_DIR%"=="" (set CODENVY_LOGS_DIR=%CATALINA_HOME%\logs)

if "%JPDA_ADDRESS%"=="" (set JPDA_ADDRESS=8000)

if "%SERVER_PORT%"=="" (set SERVER_PORT=8080)

if "%CATALINA_OPTS%"=="" (set CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcodenvy.local.conf.dir=%CODENVY_LOCAL_CONF_DIR%)

if "%CLASSPATH%"=="" (set CLASSPATH=%CATALINA_HOME%\conf\;%JAVA_HOME%\lib\tools.jar)

set LOG_OPTS=-Dcodenvy.logs.dir=%CODENVY_LOGS_DIR%

set JAVA_OPTS=%JAVA_OPTS% %LOG_OPTS%
echo "======="
echo %JAVA_OPTS%
echo "======="