# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# To build, in this directory:
#  `docker build -t codenvy/che-ip .`
#
# To use it:
#  `docker run --rm --net=host codenvy/che-ip`

FROM alpine:3.4

ADD getip.sh /bin/getip.sh

CMD /bin/getip.sh
