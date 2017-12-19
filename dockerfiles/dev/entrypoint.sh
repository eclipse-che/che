#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc.- initial API and implementation

set -e

export USER_ID=$(id -u)
export GROUP_ID=$(id -g)

if ! grep -Fq "${USER_ID}" /etc/passwd; then
    # current user is an arbitrary 
    # user (its uid is not in the 
    # container /etc/passwd). Let's fix that    
    cat ${HOME}/passwd.template | \
    sed "s/\${USER_ID}/${USER_ID}/g" | \
    sed "s/\${GROUP_ID}/${GROUP_ID}/g" | \
    sed "s/\${HOME}/\/home\/user/g" > /etc/passwd
    
    cat ${HOME}/group.template | \
    sed "s/\${USER_ID}/${USER_ID}/g" | \
    sed "s/\${GROUP_ID}/${GROUP_ID}/g" | \
    sed "s/\${HOME}/\/home\/user/g" > /etc/group
fi

is_current_user_sudoer() {
    sudo -n true > /dev/null 2>&1
}


if ! is_current_user_sudoer; then
    sed -i "s/che-host/che-host.eclipse-che.svc/g" /home/user/traefik/traefik.toml
fi
exec "$@"
