#!/bin/bash

export CHE_INFRASTRUCTURE=openshift

pwd
#cd ../../
#pwd
#git clone https://github.com/eclipse/che-parent.git
#cd che-parent
#echo "Building che-parent:"
#git checkout 6.5.0
#mvn clean install
#cd ../
#git clone https://github.com/eclipse/che-dependencies.git
#cd che-dependencies
#echo "Building che-dependencies:"
#git checkout 6.5.0
#mvn clean install
#cd ../selenium/che-selenium-test
#pwd

echo "Building and running che-selenium-test:"
export CHE_TESTUSER_NAME=developer
export CHE_TESTUSER_EMAIL="developer"
export CHE_TESTUSER_PASSWORD="developer"
#export CHE_TESTUSER_OFFLINE__TOKEN=""
#export CHE_OFFLINE_TO_ACCESS_TOKEN_EXCHANGE_POINT=https://auth.prod-preview.openshift.io/api/token/refresh
export CHE_INFRASTRUCTURE=openshift # docker | openshift
./selenium-tests.sh -Mlocal --threads=1 \
                    --test=DialogAboutTest \
                    --exclude=github \
                    --host=$1 --port=$2

#cd ../../
#rm -rf che-dependencies
#rm -rf che-parent