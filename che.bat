@echo off

SET CDIR=%CD%

set TOMCAT_IDE_DIR="assembly-sdk/target/tomcat-ide"

if exist "%TOMCAT_IDE_DIR%" (
    cd assembly-sdk/target/tomcat-ide/bin
) else (
    cd assembly-sdk/target
    mkdir tomcat-ide
    cd tomcat-ide
    jar xf ../*.zip
    cd bin
)

echo Launching Codenvy SDK

call che.bat %1 %2 %3 %4 %5 %6 %7 %8

chdir %CDIR%


















