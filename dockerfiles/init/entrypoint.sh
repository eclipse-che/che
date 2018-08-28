#!/bin/sh
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

cp -rf /files/docs /copy

# do not copy che.env if exist
if [ ! -f  /copy/che.env ]; then
    # if exist add addon env values to main env file.
    if [ -f /etc/puppet/addon.env ]; then
        cat /etc/puppet/addon.env >> /etc/puppet/manifests/che.env
    fi
    cp /etc/puppet/manifests/che.env /copy
fi
