@REM
@REM CODENVY CONFIDENTIAL
@REM __________________
@REM
@REM [2012] - [2013] Codenvy, S.A.
@REM All Rights Reserved.
@REM
@REM NOTICE:  All information contained herein is, and remains
@REM the property of Codenvy S.A. and its suppliers,
@REM if any.  The intellectual and technical concepts contained
@REM herein are proprietary to Codenvy S.A.
@REM and its suppliers and may be covered by U.S. and Foreign Patents,
@REM patents in process, and are protected by trade secret or copyright law.
@REM Dissemination of this information or reproduction of this material
@REM is strictly forbidden unless prior written permission is obtained
@REM from Codenvy S.A..
@REM

@echo off

set IDE_OPTS="-Dorg.exoplatform.logreader.logpath=%CATALINA_HOME%/logs/logreader"
set LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
set CODENVY_CONFIG_OPTS="-Xshare:auto -Xms512m -Xmx1024m -XX:MaxPermSize=256m"
set JAVA_OPTS=%LOG_OPTS% %CODENVY_CONFIG_OPTS% %IDE_OPTS%
set SERVER_PORT=8080
set CLASSPATH=%CATALINA_HOME%/conf/;%CATALINA_HOME%/lib/jul-to-slf4j.jar;%CATALINA_HOME%/lib/slf4j-api.jar;%CATALINA_HOME%/lib/logback-classic.jar;%CATALINA_HOME%/lib/logback-core.jar;%JAVA_HOME%/lib/tools.jar
echo "======="
echo %JAVA_OPTS%
echo "======="
