@echo off

SET CDIR=%CD%

cd assembly-sdk/target/tomcat-ide/bin/

echo Launching Codenvy SDK

call che.bat %1 %2 %3 %4 %5 %6 %7 %8

chdir %CDIR%


















