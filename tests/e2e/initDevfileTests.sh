#!/bin/bash

########################################
############# Methods ##################
########################################

launchAllUserstories(){
    echo ""
    echo "Launching all userstories";
    echo ""

    npm run lint && npm run tsc && mocha --config mocha-all-devfiles-${TS_SELENIUM_EDITOR}.json ;
}

launchSingleUserstory(){
    echo ""
    echo "Launching the \"${USERSTORY}\" userstory";
    echo ""

    tsc && mocha --config mocha-single-devfile.json --spec dist/tests/login/Login.spec.js --spec dist/tests/devfiles/${TS_SELENIUM_EDITOR}/${USERSTORY}.spec.js ;
}

checkUserstoryName(){
    local checkedName="$(ls tests/devfiles/${TS_SELENIUM_EDITOR} | grep ${USERSTORY}.spec.ts)";

    if [ -z "$TS_SELENIUM_EDITOR" ] || [ ! -d "tests/devfiles/${TS_SELENIUM_EDITOR}" ]; then
        echo ""
        if [ -z "$TS_SELENIUM_EDITOR" ]; then
          echo "Variable TS_SELENIUM_EDITOR is unset."
        elif [ ! -d "tests/devfiles/${TS_SELENIUM_EDITOR}" ]; then
          echo "Variable \"${TS_SELENIUM_EDITOR}\" is not a valid directory."
        fi
        echo ""
        echo "Available values are:"
        echo ""
        ls tests/devfiles/ | xargs -n1 echo
        echo ""
        echo "Please assign the variable to a correct editor value."
        echo ""

        exit 1
    fi

    if [ -z "$checkedName" ]; then
        echo ""
        echo "Current value USERSTORY=\"${USERSTORY}\" doesn't match to any of existing tests:"
        echo ""
        echo "$(ls "tests/devfiles/${TS_SELENIUM_EDITOR}" | sed -e 's/.spec.ts//g')"
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
