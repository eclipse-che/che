#!/bin/sh
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
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
