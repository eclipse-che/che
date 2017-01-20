# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Marian Labuda
FROM docker:1.11.2

RUN apk add --no-cache bash

RUN curl -o /tmp/v0.4.0.tar.gz -L https://github.com/sstephenson/bats/archive/v0.4.0.tar.gz \
	&& tar -x -z -f /tmp/v0.4.0.tar.gz -C /tmp \
	&& bash /tmp/bats-0.4.0/install.sh /usr/local \
	&& rm -rf /tmp/*

