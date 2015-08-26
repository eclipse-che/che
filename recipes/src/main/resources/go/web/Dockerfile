#
# Copyright (c) 2012-2015 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

FROM codenvy/go

EXPOSE 8080
ENV CODENVY_APP_PORT_8080_HTTP 8080

ENV GOPATH /home/user/application

RUN mkdir -p /home/user/application/src

ENV CODENVY_APP_BIND_DIR /home/user/application/src

VOLUME ["/home/user/application/src"]

WORKDIR /home/user/application/src

# 1. Get dependencies from source code of application
# 2. Start application
CMD go get -d && go run $executable:-main.go$