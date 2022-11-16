#!/bin/bash

########################################
############# Methods ##################
########################################

launchAllUserstories(){
    echo ""
    echo "Launching all userstories";
    echo ""

    if [ "${TS_SELENIUM_EDITOR}" == "theia" ]; then
        npm run lint && npm run tsc && mocha --config mocha-all-devfiles-theia.json ;
    elif [ "${TS_SELENIUM_EDITOR}" == "code" ]; then
        npm run lint && npm run tsc && mocha --config mocha-all-devfiles-code.json ;
    fi
}

launchSingleUserstory(){
    echo ""
    echo "Launching the \"${USERSTORY}\" userstory";
    echo ""

    if [ "${TS_SELENIUM_EDITOR}" == "theia" ]; then
        tsc && mocha --config mocha-single-devfile.json --spec dist/tests/login/Login.spec.js --spec dist/tests/devfiles/theia/${USERSTORY}.spec.js ;
    elif [ "${TS_SELENIUM_EDITOR}" == "code" ]; then
        tsc && mocha --config mocha-single-devfile.json --spec dist/tests/login/Login.spec.js --spec dist/tests/devfiles/code/${USERSTORY}.spec.js ;
    fi

}

checkUserstoryName(){
    local checkedName;
    if [ "${TS_SELENIUM_EDITOR}" == "theia" ]; then
        checkedName="$(ls tests/devfiles/theia | grep ${USERSTORY}.spec.ts)";
    elif [ "${TS_SELENIUM_EDITOR}" == "code" ]; then
        checkedName="$(ls tests/devfiles/code | grep ${USERSTORY}.spec.ts)";
    fi

    if [ -z "$checkedName" ]; then
        echo ""
        echo "Current value USERSTORY=\"${USERSTORY}\" doesn't match to any existed test:"
        echo ""
        echo "$(ls tests/devfiles | sed -e 's/.spec.ts/ /g')"
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
    launchAllUserstories ;
else
    checkUserstoryName ;
    launchSingleUserstory ;
fi
