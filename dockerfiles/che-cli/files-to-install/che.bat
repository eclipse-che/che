@REM
@REM Copyright (c) 2012-2016 Codenvy, S.A.
@REM All rights reserved. This program and the accompanying materials
@REM are made available under the terms of the Eclipse Public License v1.0
@REM which accompanies this distribution, and is available at
@REM http://www.eclipse.org/legal/epl-v10.html
@REM
@REM Contributors:
@REM   Codenvy, S.A. - initial API and implementation
@REM

@echo off

REM Check to ensure bash is installed
CALL bash --help > nul 2>&1
IF %ERRORLEVEL% NEQ 0 goto setup

REM Launch Che and any associated docker machines, if necessary
CALL bash --login -i "%~dp0\che.sh" %*

goto end

:setup
echo. 
echo "REQUIRED: Git bash for Windows. It is typically stored c:\Program Files\Git\bin."
echo.

:end
