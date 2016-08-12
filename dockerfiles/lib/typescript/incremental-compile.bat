@echo off
echo "Incremental compiling from directory %cd%"
docker run --rm -v %cd%:/usr/src/app -w /usr/src/app node:6 /bin/bash -c "/usr/src/app/dependencies/compile/node_modules/typescript/bin/tsc --target ES5 --experimentalDecorators --emitDecoratorMetadata --outDir /usr/src/app/lib /usr/src/app/src/index.ts"
if %ERRORLEVEL% == 0 goto :end

:error
echo "Typescript compilation failed"
exit /b 1


:end
echo "Successfully compiled"