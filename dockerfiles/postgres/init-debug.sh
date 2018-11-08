#!/bin/sh
#
# Copyright (c) 2012-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#
if [ -n "${POSTGRESQL_LOG_DEBUG+set}" ] && [ "${POSTGRESQL_LOG_DEBUG}" == "true" ]; then
    echo "POSTGRESQL_LOG_DEBUG is set, enabling additional logging configuration"

    if [ ! -f /opt/app-root/src/postgresql-cfg/postgresql.log.debug.conf ]; then
        echo "postgresql.conf not found!"
        mv /opt/app-root/src/postgresql-cfg/postgresql.conf.debug /opt/app-root/src/postgresql-cfg/postgresql.log.debug.conf
     else
        echo OK
     fi
fi
