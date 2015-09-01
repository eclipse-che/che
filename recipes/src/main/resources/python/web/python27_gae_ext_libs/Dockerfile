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

FROM codenvy/python27_gae

RUN mkdir /tmp/application

ENV CODENVY_APP_BIND_DIR /home/user/application

VOLUME ["/home/user/application"]

ADD $app$/requirements.txt /tmp/application/requirements.txt

RUN cd /tmp/application && \
    sudo virtualenv /env && \
    sudo /env/bin/pip install -r requirements.txt -t /tmp/application/lib

CMD sudo cp -a /tmp/application/lib /home/user/application/ && \
    sudo chmod a+rw -R /home/user/application/ && \
    umask 0 && \
    /home/user/google_appengine/dev_appserver.py 2>&1 --host 0.0.0.0 --skip_sdk_update_check true /home/user/application