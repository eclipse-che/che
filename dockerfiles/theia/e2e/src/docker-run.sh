#!/bin/sh
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

echo "Starting Theia..."
rm -rf /root/logs/*
HOME=/home/theia /entrypoint.sh > /root/logs/theia.log 2>/root/logs/theia-error.log&
sleep 10s
echo "Cleaning videos folder..."
# Cleanup previous videos
rm -rf /root/cypress/videos

# Run tests
echo "Run the tests"
cd /root && unset LD_LIBRARY_PATH && /root/node_modules/.bin/cypress run -c trashAssetsBeforeRuns=false