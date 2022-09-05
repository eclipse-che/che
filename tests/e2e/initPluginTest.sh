#!/bin/bash

source ./initDefaultValues.sh

echo ""
echo "Launching the \"${USERSTORY}\" userstory";
echo ""

# TODO: Handle editor switching
npm run init-mocha-opts -- --spec dist/tests/plugins/theia/${USERSTORY}.spec.js
