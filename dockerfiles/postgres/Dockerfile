# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM centos/postgresql-96-centos7
ADD init-che-user-and-run.sh.erb init-che-user.sh.erb /var/lib/pgsql/
RUN cat /var/lib/pgsql/init-che-user.sh.erb | \
    sed -e "/exit 0/d" > /var/lib/pgsql/init-che-user-and-run.sh && \
    echo "exec run-postgresql \"\$@\"" >> /var/lib/pgsql/init-che-user-and-run.sh
USER root
RUN chmod +x /var/lib/pgsql/init-che-user-and-run.sh
USER postgres
ADD --chown=postgres postgresql.conf.debug /opt/app-root/src/postgresql-cfg/
ADD init-debug.sh /opt/app-root/src/postgresql-pre-start/
RUN chgrp -R 0 /opt/app-root/src/postgresql-cfg/ && chmod -R g+rwX /opt/app-root/src/postgresql-cfg/
CMD ["/var/lib/pgsql/init-che-user-and-run.sh"]
