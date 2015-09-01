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

FROM codenvy/ruby210_rails403

RUN mkdir /tmp/application /home/user/application

ENV CODENVY_APP_BIND_DIR /home/user/application

VOLUME ["/home/user/application"]

ADD $app$/Gemfile /tmp/application/Gemfile

RUN cd /tmp/application && /bin/bash -l -c "bundle install" && \
    echo 'source /etc/profile.d/rvm.sh' >> /home/user/.bashrc

# 1. Update permissions recursively
# 2. Make newly created files accessible for anyone
# 3. Copy Gemfile.lock to the application folder
# 4. Make file in bin directory executable
# 5. Start application
CMD sudo chmod a+rw -R /home/user/application/ && \
    umask 0 && \
    cp /tmp/application/Gemfile.lock /home/user/application/ && \
    sudo chmod +x /home/user/application/bin/* && \
    /home/user/application/bin/rails server
