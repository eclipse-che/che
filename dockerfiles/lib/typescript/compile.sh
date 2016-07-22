docker run --rm -v $(pwd):/usr/src/app -w /usr/src/app node:6  /bin/bash -c "cd /usr/src/app/dependencies/compile && npm install && cd /usr/src/app/dependencies/runtime && npm install && npm install -g tsd && cd /usr/src/app/src && tsd install && cd /usr/src/app && /usr/src/app/dependencies/compile/node_modules/typescript/bin/tsc --outDir /usr/src/app/lib /usr/src/app/src/index.ts"

if [ $? -eq 0 ]; then
    echo 'Compilation of TypeScript is OK'
else
    echo 'Compilation has failed, please check TypeScript sources'
    exit 1
fi
