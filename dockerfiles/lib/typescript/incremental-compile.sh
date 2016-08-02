#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

docker run --rm -v $(pwd):/usr/src/app -w /usr/src/app node:6  /bin/bash -c "/usr/src/app/dependencies/compile/node_modules/typescript/bin/tsc --outDir /usr/src/app/lib /usr/src/app/src/index.ts && groupadd user && useradd -g user user && (chown --silent -R user.user /usr/src/app || true)"

if [ $? -eq 0 ]; then
    echo 'Compilation of TypeScript is OK'
else
    echo 'Compilation has failed, please check TypeScript sources'
    exit 1
fi
