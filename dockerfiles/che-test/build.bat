@echo off
set currentfolder=%cd%
for %%i in ("%~dp0..") do set "folder=%%~fi"
echo "Building Docker Image from %folder% directory with tag %tag%"
cd %folder%
echo current folder is %cd%
docker build -t codenvy/che-test:nightly -f che-test/Dockerfile .

if %ERRORLEVEL% == 0 goto :end

:error
echo Docker image has failed"
exit /b 1


:end
echo "Docker image successfully built"
cd %currentfolder%