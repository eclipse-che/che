#!/bin/bash
# set -x


. common-util.sh

setConfigProperty test.username admin

echo "=========================================="
echo
echo
echo "$(readConfigProperty test.username)" 
echo
echo
echo "=========================================="


