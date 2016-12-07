# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t eclipse/che-cli .
#
# use:
#    docker run -v $(pwd):/che eclipse/che-cli [command]
FROM eclipse/che-base:nightly

COPY scripts /scripts/
COPY version /version/

RUN mkdir /che && chmod u+x /scripts/entrypoint.sh
ENTRYPOINT ["/scripts/entrypoint.sh"]
