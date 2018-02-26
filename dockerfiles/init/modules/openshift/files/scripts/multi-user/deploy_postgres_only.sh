#!/bin/bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

if [ -z "${IMAGE_POSTGRES+x}" ]; then echo "[CHE] **ERROR**Env var IMAGE_POSTGRES is unset. You need to set it to continue. Aborting"; exit 1; fi

COMMAND_DIR=$(dirname "$0")
export CHE_EPHEMERAL=${CHE_EPHEMERAL:-false}

for i in $(ls "$COMMAND_DIR"/postgres ); do
    cat "${COMMAND_DIR}"/postgres/"${i}" | sed "s#\${IMAGE_POSTGRES}#${IMAGE_POSTGRES}#" | oc apply -f -
done

if [ "${CHE_EPHEMERAL}" == "true" ]; then
  oc volume dc/postgres --remove --confirm
  oc delete pvc/postgres-data
fi

"$COMMAND_DIR"/wait_until_postgres_is_available.sh
