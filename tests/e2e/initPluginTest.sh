#!/bin/bash

source ./initDefaultValues.sh

echo ""
echo "Launching the \"${USERSTORY}\" userstory";
echo ""

if [ "${TS_SELENIUM_EDITOR}" == "theia" ]; then
    npm run init-mocha-opts -- --spec dist/tests/plugins/theia/${USERSTORY}.spec.js
elif [ "${TS_SELENIUM_EDITOR}" == "code" ]; then
    npm run init-mocha-opts -- --spec dist/tests/plugins/code/${USERSTORY}.spec.js
fi
