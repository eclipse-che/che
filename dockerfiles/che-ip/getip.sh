#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A., Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html


# return true if system is using boot2docker
is_boot2docker() {
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

# network interface to use to grab local ip
NETWORK_IF="eth0"
if is_boot2docker; then
  NETWORK_IF="eth1"
fi

# grab ip from the following command
ip a show "${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{print $2}'

