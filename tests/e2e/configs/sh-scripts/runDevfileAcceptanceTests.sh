#!/bin/bash
# shellcheck source=/dev/null

validateParameters(){
# Validate required parameters
  if [ -z "$TS_API_ACCEPTANCE_TEST_REGISTRY_URL" ] || [ -z "$TS_SELENIUM_BASE_URL" ]; then
      echo "The TS_API_ACCEPTANCE_TEST_REGISTRY_URL or TS_SELENIUM_BASE_URL is not set!";
      echo "Please, set all required environment variable parameters"
      exit 1
  fi
}

########################################
############# Methods ##################
########################################

launchDynamicallyGeneratingAPITests() {
  export MOCHA_SUITE="DynamicallyGeneratingAPITest"
  export RP_LAUNCH_NAME="Devfile Acceptance tests suite"
  echo "MOCHA_SUITE = ${MOCHA_SUITE}"
  echo "suites/$MOCHA_DIRECTORY/$MOCHA_SUITE"
  npm run delayed-test
}

initTestValues() {
export MOCHA_DIRECTORY="devfile-acceptance-test-suite"
if [[ "$TS_SELENIUM_BASE_URL" =~ "airgap" || (-n "$IS_CLUSTER_DISCONNECTED" && "$IS_CLUSTER_DISCONNECTED" == "true") ]]
  then
     echo "Disconnected environment"
  else
     echo "Online environment"
  fi

  export TEST_ENVIRONMENT="$OCP_INFRA $MOCHA_DIRECTORY $OCP_VERSION"
  export DELETE_WORKSPACE_ON_FAILED_TEST=${DELETE_WORKSPACE_ON_FAILED_TEST:-'false'}
  export DELETE_SCREENCAST_IF_TEST_PASS=${DELETE_SCREENCAST_IF_TEST_PASS:-'true'}
  export NODE_TLS_REJECT_UNAUTHORIZED=${NODE_TLS_REJECT_UNAUTHORIZED:-'0'}
  export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=${TS_OCP_LOGIN_PAGE_PROVIDER_TITLE:-'htpasswd'}
  export TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS=${TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS:-'1000'}
  export TS_SELENIUM_EDITOR=${TS_SELENIUM_EDITOR:-'che-code'}
  export TS_SELENIUM_EXECUTION_SCREENCAST=${TS_SELENIUM_EXECUTION_SCREENCAST:-'false'}
  export TS_SELENIUM_HEADLESS=${TS_SELENIUM_HEADLESS:-'false'}
  export TS_SELENIUM_LAUNCH_FULLSCREEN=${TS_SELENIUM_LAUNCH_FULLSCREEN:-'true'}
  export TS_SELENIUM_LOG_LEVEL=${TS_SELENIUM_LOG_LEVEL:-'TRACE'}
  export TS_SELENIUM_OCP_PASSWORD=${TS_SELENIUM_OCP_PASSWORD:-''}
  export TS_SELENIUM_OCP_USERNAME=${TS_SELENIUM_OCP_USERNAME:-'admin'}
  export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=${TS_SELENIUM_VALUE_OPENSHIFT_OAUTH:-'true'}
  export TS_SELENIUM_REPORT_FOLDER=${TS_SELENIUM_REPORT_FOLDER:-'./report'}
  export MOCHA_BAIL=${MOCHA_BAIL:-'false'}
  export MOCHA_RETRIES=${MOCHA_RETRIES:-'1'}

  echo "TS_SELENIUM_BASE_URL=${TS_SELENIUM_BASE_URL}"
  echo "TEST_ENVIRONMENT=${TEST_ENVIRONMENT}"
  echo "DELETE_WORKSPACE_ON_FAILED_TEST=${DELETE_WORKSPACE_ON_FAILED_TEST}"
  echo "DELETE_SCREENCAST_IF_TEST_PASS=${DELETE_SCREENCAST_IF_TEST_PASS}"
  echo "NODE_TLS_REJECT_UNAUTHORIZED=${NODE_TLS_REJECT_UNAUTHORIZED}"
  echo "TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=${TS_OCP_LOGIN_PAGE_PROVIDER_TITLE}"
  echo "TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS=${TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS}"
  echo "TS_SELENIUM_EDITOR=${TS_SELENIUM_EDITOR}"
  echo "TS_SELENIUM_EXECUTION_SCREENCAST=${TS_SELENIUM_EXECUTION_SCREENCAST}"
  echo "TS_SELENIUM_HEADLESS=${TS_SELENIUM_HEADLESS}"
  echo "TS_SELENIUM_LAUNCH_FULLSCREEN=${TS_SELENIUM_LAUNCH_FULLSCREEN}"
  echo "TS_SELENIUM_LOG_LEVEL=${TS_SELENIUM_LOG_LEVEL}"
  echo "TS_SELENIUM_OCP_USERNAME=${TS_SELENIUM_OCP_USERNAME}"
  echo "TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=${TS_SELENIUM_VALUE_OPENSHIFT_OAUTH}"
  echo "TS_SELENIUM_REPORT_FOLDER=${TS_SELENIUM_REPORT_FOLDER}"
  echo "MOCHA_BAIL=${MOCHA_BAIL}"
  echo "MOCHA_RETRIES=${MOCHA_RETRIES}"
}

########################################
############# Launching ################
########################################

validateParameters
  initTestValues
  echo ""
  echo "Launching devfile acceptance test suite for $TEST_ENVIRONMENT and registry $TS_API_ACCEPTANCE_TEST_REGISTRY_URL"
  echo ""
  launchDynamicallyGeneratingAPITests

