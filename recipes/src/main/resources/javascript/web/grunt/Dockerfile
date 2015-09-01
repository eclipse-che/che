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

FROM codenvy/angular-yeoman

ADD $app$/package.json /tmp/application/package.json

RUN cd /tmp/application && npm install

ENV CODENVY_APP_BIND_DIR /home/user/application

VOLUME ["/home/user/application"]

# 1. Update permissions
# 2. Copy nodejs modules to the application folder
# 3. Update permissions recursively
# 4. Makes newly created files accessible for anyone
# 5. Start application
CMD sudo chmod a+rw /home/user/application/ && \
    cp -a /tmp/application/node_modules /home/user/application/ && \
    sudo chmod a+rw -R /home/user/application/ && \
    umask 0 && \
    grunt $taskName:-server$
    
