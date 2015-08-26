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

FROM codenvy/jdk7_jetty9

$debug?EXPOSE 8000:$
$debug?ENV CODENVY_APP_PORT_8000_DEBUG 8000:$
$debug?CMD java -Xdebug -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -jar start.jar 2>&1:$

ENV CODENVY_APP_BIND_DIR /home/user/jetty9/webapps/ROOT

VOLUME ["/home/user/jetty9/webapps/ROOT"]
