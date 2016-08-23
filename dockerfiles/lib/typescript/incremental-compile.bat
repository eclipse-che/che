@echo off
echo "Incremental compiling from directory %cd%"
docker run --rm -v %cd%:/usr/src/app -w /usr/src/app node:6 /bin/bash -c "cd /usr/src/app/src && find . -name "*.properties" -exec install -D {} /usr/src/app/lib/{} \; && cd /usr/src/app && /usr/src/app/dependencies/compile/node_modules/typescript/bin/tsc --target ES5 --experimentalDecorators --emitDecoratorMetadata --outDir /usr/src/app/lib /usr/src/app/src/index.ts"
if %ERRORLEVEL% == 0 goto :end

:error
echo "Typescript compilation failed"
exit /b 1


:end
echo "Successfully compiled"