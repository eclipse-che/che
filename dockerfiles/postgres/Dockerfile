# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

FROM centos/postgresql-96-centos7
ADD init-che-user-and-run.sh.erb init-che-user.sh.erb /var/lib/pgsql/
RUN cat /var/lib/pgsql/init-che-user.sh.erb | \
    sed -e "/exit 0/d" > /var/lib/pgsql/init-che-user-and-run.sh && \
    echo "exec run-postgresql \"\$@\"" >> /var/lib/pgsql/init-che-user-and-run.sh
USER root
RUN chmod +x /var/lib/pgsql/init-che-user-and-run.sh
USER postgres
CMD ["/var/lib/pgsql/init-che-user-and-run.sh"]
