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

FROM codenvy/jdk7_glassfish4

$debug?EXPOSE 9009:$
$debug?ENV CODENVY_APP_PORT_9009_DEBUG 9009:$

CMD ./asadmin start-domain $debug?--debug:$ && \
    ./asadmin deploydir --contextroot "/" /home/user/application && \
    tail -f /home/user/glassfish4/glassfish/domains/domain1/logs/server.log

ENV CODENVY_APP_BIND_DIR /home/user/application

VOLUME ["/home/user/application"]


