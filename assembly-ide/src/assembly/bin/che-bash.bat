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


REM Check to see if bash is installed
CALL bash --help > nul 2>&1
IF %ERRORLEVEL% NEQ 0 goto setup

REM Reset CHE_HOME if it does not exist
IF NOT EXIST "%CHE_HOME%" SET "%CHE_HOME%"=""

REM Finds location of this script, and then sets the parent directory as the value
IF "%CHE_HOME%"=="" (
  FOR %%i in ("%~dp0..") do set "CHE_HOME=%%~fi"
)

REM Program to create docker VM & environment variables
CALL bash --login "%CHE_HOME%\bin\che-bash.sh" %1 %2 %3 %4 %5 %6 %7 %8

goto end

:setup
echo. 
echo REQUIRED: Git bash. Please re-run Docker Toolbox Installer and add bash.exe to your PATH.
echo           This is typically located at c:\Program Files\Git\bin.
echo.

:end
