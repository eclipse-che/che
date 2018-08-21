# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Marian Labuda
FROM docker:1.11.2

# ncurses is used to use --pretty option of bats
RUN apk add --no-cache bash ncurses

RUN curl -o /tmp/v0.4.0.tar.gz -L https://github.com/sstephenson/bats/archive/v0.4.0.tar.gz \
	&& tar -x -z -f /tmp/v0.4.0.tar.gz -C /tmp \
	&& bash /tmp/bats-0.4.0/install.sh /usr/local \
	&& rm -rf /tmp/* \
    && curl -o /tmp/bats-support-v0.3.0.tar.gz -L https://github.com/ztombol/bats-support/archive/v0.3.0.tar.gz \
	&& tar -x -z -f /tmp/bats-support-v0.3.0.tar.gz -C /tmp \
	&& mv /tmp/bats-support-0.3.0/ /bats-support  \
	&& rm -rf /bats-support/test && rm -rf /bats-support/script \
	&& rm -rf /tmp/* \
    && curl -o /tmp/bats-assert-v0.3.0.tar.gz -L https://github.com/ztombol/bats-assert/archive/v0.3.0.tar.gz \
   	&& tar -x -z -f /tmp/bats-assert-v0.3.0.tar.gz -C /tmp \
   	&& mv /tmp/bats-assert-0.3.0 /bats-assert  \
	&& rm -rf /bats-assert/test && rm -rf /bats-assert/script \
    && rm -rf /tmp/*
