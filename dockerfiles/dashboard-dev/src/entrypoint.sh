#!/bin/bash
#
# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

export USER_ID=$(id -u)
export GROUP_ID=$(id -g)

if ! grep -Fq "${USER_ID}" /etc/passwd; then
    # current user is an arbitrary
    # user (its uid is not in the
    # container /etc/passwd). Let's fix that
    sed \
        -e "s/\${USER_ID}/${USER_ID}/g" \
        -e "s/\${GROUP_ID}/${GROUP_ID}/g" \
        -e "s/\${HOME}/\/home\/theia/g" \
        /.passwd.template > /etc/passwd
    sed \
        -e "s/\${GROUP_ID}/${GROUP_ID}/g" \
        /.group.template > /etc/group

    # now the user `user` (that have uid:gid == $USER_ID,$GROUP_ID) can use `sudo`.
fi

# Grant access to projects volume in case of non root user with sudo rights
if [ "$USER_ID" -ne 0 ] && command -v sudo >/dev/null 2>&1 && sudo -n true > /dev/null 2>&1; then
    sudo chmod 644 /etc/passwd /etc/group
    sudo chown root:root /etc/passwd /etc/group
    sudo chown ${USER_ID}:${GROUP_ID} /projects ${HOME}
fi

exec "$@"
