#!/bin/bash

########################################
############# Methods ##################
########################################

launchSingleUserstory(){
    echo ""
    echo "Launching the \"${USERSTORY}\" userstory";
    echo ""
    
    tsc && mocha --opts mocha.single.plugin.opts --spec ./dist/tests/login/Login.spec.js --spec ./dist/tests/plugins/${USERSTORY}.spec.js
}

########################################
############# Launching ################
########################################

launchSingleUserstory ;
