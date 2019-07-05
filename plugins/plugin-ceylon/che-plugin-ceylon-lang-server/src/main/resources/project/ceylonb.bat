@echo off
setlocal ENABLEDELAYEDEXPANSION

:: Check if we should use a distribution bootstrap
pushd "%~dp0"
set "DIR=%CD%"
popd
if NOT exist "%DIR%\.ceylon\bootstrap\ceylon-bootstrap.properties" (
    goto :normal
)
if NOT exist "%DIR%\.ceylon\bootstrap\ceylon-bootstrap.jar" (
    goto :normal
)

:: Using bootstrap
set "LIB=%DIR%\.ceylon\bootstrap"

goto :endbs

:normal

:: Normal execution

:: Find CEYLON_HOME
pushd "%~dp0.."
set "CEYLON_HOME=%CD%"
popd
set "LIB=%CEYLON_HOME%\lib"

if "%~1" == "--show-home" (
    @echo %CEYLON_HOME%
    exit /b 1
)

:endbs

:: Find Java

:: Only check the registry if JAVA_HOME is not already set
IF NOT "%JAVA_HOME%" == "" (
    goto :javaend
)

:: Find Java in the registry
set "KEY_NAME=HKLM\SOFTWARE\JavaSoft\Java Runtime Environment"
set "KEY_NAME2=HKLM\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment"

:: get the current version
FOR /F "usebackq skip=2 tokens=3" %%A IN (`REG QUERY "%KEY_NAME%" /v CurrentVersion 2^>nul`) DO (
    set "ValueValue=%%A"
)

if "%ValueValue%" NEQ "" (
    set "JAVA_CURRENT=%KEY_NAME%\%ValueValue%"
) else (
    rem Try again for 64bit systems

    FOR /F "usebackq skip=2 tokens=3" %%A IN (`REG QUERY "%KEY_NAME2%" /v CurrentVersion 2^>nul`) DO (
        set "JAVA_CURRENT=%KEY_NAME2%\%%A"
    )
)

if "%ValueValue%" NEQ "" (
    set "JAVA_CURRENT=%KEY_NAME%\%ValueValue%"
) else (
    rem Try again for 64bit systems from a 32-bit process

    FOR /F "usebackq skip=2 tokens=3" %%A IN (`REG QUERY "%KEY_NAME%" /v CurrentVersion /reg:64 2^>nul`) DO (
        set "JAVA_CURRENT=%KEY_NAME%\%%A"
    )
)

if "%JAVA_CURRENT%" == "" (
    @echo Java not found, you must install Java in order to compile and run Ceylon programs
    @echo Go to http://www.java.com/getjava/ to download the latest version of Java
    exit /b 1
)

:: get the javahome
FOR /F "usebackq skip=2 tokens=3*" %%A IN (`REG QUERY "%JAVA_CURRENT%" /v JavaHome 2^>nul`) DO (
    set "JAVA_HOME=%%A %%B"
)

if "%JAVA_HOME%" EQU "" (
    rem Try again for 64bit systems from a 32-bit process
    FOR /F "usebackq skip=2 tokens=3*" %%A IN (`REG QUERY "%JAVA_CURRENT%" /v JavaHome /reg:64 2^>nul`) DO (
        set "JAVA_HOME=%%A %%B"
    )
)

:javaend

set "JAVA=%JAVA_HOME%\bin\java.exe"

:: Check that Java executable actually exists
if not exist "%JAVA%" (
    @echo "Cannot find java.exe at %JAVA%, check that your JAVA_HOME variable is pointing to the right place"
    exit /b 1
)

rem set JAVA_DEBUG_OPTS="-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"

if NOT "%PRESERVE_JAVA_OPTS%" == "true" (
    set PREPEND_JAVA_OPTS=%JAVA_DEBUG_OPTS%
    rem Other java opts go here
)

rem Find any --java options and add their values to JAVA_OPTS
for %%x in (%*) do (
    set ARG=%%~x
    if "!ARG:~0,7!" EQU "--java=" (
        set OPT=!ARG:~7!
        set "JAVA_OPTS=!JAVA_OPTS! !OPT!"
    ) else if "!ARG!" EQU "--java" (
        @echo Error: use --java options with an equal sign and quotes, eg: "--java=-Xmx500m"
        exit /b 1
    ) else if "!ARG:~0,1!" NEQ "-" (
        goto :breakloop
    )
)
:breakloop

set "JAVA_OPTS=%PREPEND_JAVA_OPTS% %JAVA_OPTS%"

"%JAVA%" ^
    %JAVA_OPTS% ^
    -jar "%LIB%\ceylon-bootstrap.jar" ^
    %*

endlocal

if %errorlevel% neq 0 exit /B %errorlevel%
