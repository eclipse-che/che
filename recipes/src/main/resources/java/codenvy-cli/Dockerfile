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

FROM codenvy/jdk7

RUN mkdir /home/user/application

ENV CODENVY_APP_BIND_DIR /home/user/application

VOLUME ["/home/user/application"]

# 1. Build application is unpacked zip archive, so move zip content into application dir
# 2. Remove unnecessary empty unzipped folder
# 3. Make main CLI scripts executable
# 4. Allow CLI starts automatically when shell opens
# 5. Disallow image stop automatically
CMD mv /home/user/application/codenvy-cli-*/* /home/user/application && \
    rm -R /home/user/application/codenvy-cli-* && \
    sudo chmod +x /home/user/application/bin/* && \
    echo "/home/user/application/bin/codenvy-cli" >> /home/user/.bashrc && \
    while true;do true; done
