#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# define network interface variable
NETWORK_IF=
for i in $( ls /sys/class/net ); do
  if [ ${i:0:3} = eth ]
  then
    NETWORK_IF=${i}
  fi
done

# if not found, throw error
if test -z ${NETWORK_IF}
  then
    echo unable to find a eth* interface
    exit 1
fi

ip a show "${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{print $2}'
