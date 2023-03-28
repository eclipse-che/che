#!/bin/bash

########################################
############# Methods ##################
########################################

launchUserstories(){
    export MOCHA_DIRECTORY=${MOCHA_DIRECTORY:-"dashboard-samples"}
    echo "MOCHA_DIRECTORY = ${MOCHA_DIRECTORY}"
    npm run lint && npm run tsc && mocha --config dist/configs/mocharc.js
}

checkUserstoryName(){
    local checkedName="$(ls specs/devfiles | grep ${USERSTORY}.spec.ts)";

    if [ -z "$TS_SELENIUM_EDITOR" ]; then
        echo ""
        echo "Variable TS_SELENIUM_EDITOR is unset. It will be assign to default value."
        echo ""
        export TS_SELENIUM_EDITOR=${TS_SELENIUM_EDITOR:-"che-code"}
        echo "TS_SELENIUM_EDITOR = ${TS_SELENIUM_EDITOR}"
        echo ""
    fi

    if [ -z "$checkedName" ]; then
        echo ""
        echo "Current value USERSTORY=\"${USERSTORY}\" doesn't match to any of existing tests:"
        echo ""
        echo "$(ls specs/devfiles | sed -e 's/.spec.ts//g')"
        echo ""
        echo "Please choose one of the tests above, or unset the \"USERSTORY\" variable for launching all of them."
        echo ""

        exit 1;
    fi
}

########################################
############# Launching ################
########################################

if [ -z "$USERSTORY" ]; then
    echo ""
    echo "Launching all userstories";
    echo ""
    launchUserstories ;
else
    checkUserstoryName ;
    echo ""
    echo "Launching the \"${USERSTORY}\" userstory";
    echo ""
    launchUserstories ;
fi
