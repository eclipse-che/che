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
set DIRNAME=%~dp0%
set FILE=%DIRNAME%\assembly-sdk\target\assembly-sdk-*
for /F %%i in ("%FILE%") do (set VERSION=%%~nxi)

set CATALINA_HOME=%DIRNAME%assembly-sdk\target\%VERSION%\%VERSION%
set ASSEMBLY_BIN_DIR=%CATALINA_HOME%\bin
IF exist %ASSEMBLY_BIN_DIR% (
    call %ASSEMBLY_BIN_DIR%\che.bat %1 %2 %3 %4 %5 %6 %7 %8
) else (
    echo The command 'mvn clean install' needs to be run first. This will build the Eclipse Che assembly.
)
