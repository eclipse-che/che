# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t eclipse/che-dir .
#
# use:
#    docker run -v /var/run/docker.sock:/var/run/docker.sock eclipse/che-dir [command]

FROM eclipse/che-lib:nightly

ENTRYPOINT ["node", "/che-lib/index.js", "che-dir"]
