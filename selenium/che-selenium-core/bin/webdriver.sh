#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

getRecommendedThreadCount() {
    local threadCount=$MIN_THREAD_COUNT

    if [[ "$OSTYPE" == "darwin"* ]]; then
        local totalMemory=$(sysctl -a | awk '/hw./' | grep hw.memsize | awk '{print $2}')
        if [[ -n "$totalNumber" ]]; then
            threadCount=$(( ${totalMemory} / 6000000 ))
        fi
    else
        local freeMemory=$(grep MemFree /proc/meminfo | awk '{print $2}')
        if [[ -n "$freeMemory" ]]; then
            threadCount=$(( ${freeMemory} / 4000000 ))
        fi
    fi

    if [[ $threadCount < ${MIN_THREAD_COUNT} ]]; then
        threadCount=${MIN_THREAD_COUNT}

    elif [[ $threadCount > ${MAX_THREAD_COUNT} ]]; then
        threadCount=${MAX_THREAD_COUNT}
    fi

    echo $threadCount
}

detectDockerInterfaceIp() {
    docker run --rm --net host eclipse/che-ip:nightly
}

initVariables() {
    # we need to have at least 2 threads for tests which start several WebDriver instances at once, for example, tests of File Watcher
    readonly MIN_THREAD_COUNT=2
    # having more than 5 threads doesn't impact on performance significantly
    readonly MAX_THREAD_COUNT=5

    readonly FAILSAFE_DIR="target/failsafe-reports"
    readonly TESTNG_FAILED_SUITE=${FAILSAFE_DIR}"/testng-failed.xml"
    readonly FAILSAFE_REPORT="target/site/failsafe-report.html"

    readonly SINGLE_TEST_MSG="single test/package"

    export CHE_MULTIUSER=${CHE_MULTIUSER:-false}
    export CHE_INFRASTRUCTURE=${CHE_INFRASTRUCTURE:-docker}

    # CALLER variable contains parent caller script name
    # CUR_DIR variable contains the current directory where CALLER is executed
    [[ -z ${CALLER+x} ]] && { CALLER=$(basename $0); }
    [[ -z ${CUR_DIR+x} ]] && { CUR_DIR=$(cd "$(dirname "$0")"; pwd); }

    [[ -z ${API_SUFFIX+x} ]] && { API_SUFFIX="/api/"; }

    MODE="grid"
    GRID_OPTIONS="-Dgrid.mode=true"
    RERUN_ATTEMPTS=0
    BROWSER="GOOGLE_CHROME"
    WEBDRIVER_VERSION=$(curl -s http://chromedriver.storage.googleapis.com/LATEST_RELEASE)
    WEBDRIVER_PORT="9515"
    NODE_CHROME_DEBUG_SUFFIX=
    THREADS=$(getRecommendedThreadCount)
    WORKSPACE_POOL_SIZE=0

    ACTUAL_RESULTS=()
    COMPARE_WITH_CI=false

    PRODUCT_PROTOCOL="http"
    PRODUCT_HOST=$(detectDockerInterfaceIp)
    PRODUCT_PORT=8080

    SUPPORTED_INFRASTRUCTURES=(docker openshift)

    unset DEBUG_OPTIONS
    unset MAVEN_OPTIONS
    unset TMP_SUITE_PATH
    unset ORIGIN_TESTS_SCOPE
    unset TMP_DIR
    unset NEW_DEFAULT_USER_ID
    unset NEW_SECOND_USER_ID
    unset EXCLUDE_PARAM
}

cleanUpEnvironment() {
    if [[ ${MODE} == "grid" ]]; then
        stopWebDriver
        stopSeleniumDockerContainers
    fi

    if [[ -n ${NEW_USER_ID} ]]; then
       echo "[TEST] Removing default test user with name '${CHE_TESTUSER_NAME}'..."
       removeUser ${NEW_USER_ID}  ${CHE_TESTUSER_NAME}
    fi

    if [[ -n ${NEW_SECOND_USER_ID} ]]; then
       echo "[TEST] Removing second test user with name '${CHE_SECOND_TESTUSER_NAME}'..."
       removeUser ${NEW_SECOND_USER_ID} ${CHE_SECOND_TESTUSER_NAME}
    fi
}

checkParameters() {
    for var in "$@"; do
        if [[ "$var" =~ --web-driver-version=.* ]]; then :
        elif [[ "$var" =~ --web-driver-port=[0-9]+$ ]]; then :
        elif [[ "$var" == --http ]]; then :
        elif [[ "$var" == --https ]]; then :
        elif [[ "$var" == --che ]]; then :
        elif [[ "$var" =~ --host=.* ]]; then :
        elif [[ "$var" =~ --port=.* ]]; then :
        elif [[ "$var" =~ --threads=[0-9]+$ ]]; then :

        elif [[ "$var" == --rerun ]]; then :
        elif [[ "$var" =~ ^[0-9]+$ ]] && [[ $@ =~ --rerun[[:space:]]$var ]]; then :

        elif [[ "$var" == --debug ]]; then :
        elif [[ "$var" == --all-tests ]]; then
            echo "[WARN] '--all-tests' parameter is outdated and is being ignored"

        elif [[ "$var" =~ --test=.* ]]; then
            local fileName=$(basename $(echo "$var" | sed -e "s/--test=//g"))
            find ${CUR_DIR} | grep "${fileName}.[class|java]" > /dev/null
            [[ $? != 0 ]] && {
                echo "[TEST] Test "${fileName}" not found";
                echo "[TEST] Proper way to use --test parameter:";
                echo -e "[TEST] \t--test=DialogAboutTest";
                echo -e "[TEST] \t--test=org.eclipse.che.selenium.miscellaneous.DialogAboutTest";
                echo -e "[TEST] \t--test=org.eclipse.che.selenium.miscellaneous.**";
                exit 1;
            }

        elif [[ "$var" =~ --suite=.* ]]; then
            local suite=$(basename $(echo "$var" | sed -e "s/--suite=//g"))
            find ${CUR_DIR}/src/test/resources/suites | grep ${suite} > /dev/null
            [[ $? != 0 ]] && {
                echo "[TEST] Suite "${suite}" not found";
                echo "[TEST] Proper way to use --suite parameter:";
                echo -e "[TEST] \t--suite=CheSuite.xml";
                exit 1;
            }

        elif [[ "$var" == --failed-tests ]]; then :
        elif [[ "$var" == --regression-tests ]]; then :
        elif [[ "$var" =~ -M.* ]]; then :
        elif [[ "$var" =~ -P.* ]]; then :
        elif [[ "$var" == --help ]]; then :

        elif [[ "$var" == --compare-with-ci ]]; then :
        elif [[ "$var" =~ ^[0-9]+$ ]] && [[ $@ =~ --compare-with-ci[[:space:]]$var ]]; then :

        elif [[ "$var" =~ ^--workspace-pool-size=(auto|[0-9]+)$ ]]; then :
        elif [[ "$var" =~ ^-D.* ]]; then :
        elif [[ "$var" =~ ^-[[:alpha:]]$ ]]; then :
        elif [[ "$var" == --skip-sources-validation ]]; then :
        elif [[ "$var" == --multiuser ]]; then :
        elif [[ "$var" =~ --exclude=.* ]]; then :

        else
            printHelp
            echo "[TEST] Unrecognized or misused parameter "${var}
            exit 1
        fi
    done
}

applyCustomOptions() {
    for var in "$@"; do
        if [[ "$var" =~ --web-driver-version=.* ]]; then
            if [[ ${MODE} == "local" ]]; then
                WEBDRIVER_VERSION=$(echo "$var" | sed -e "s/--web-driver-version=//g")
            fi

        elif [[ "$var" =~ --web-driver-port=.* ]]; then
            if [[ ${MODE} == "local" ]]; then
                WEBDRIVER_PORT=$(echo "$var" | sed -e "s/--web-driver-port=//g")
            fi

        elif [[ "$var" == --http ]]; then
            PRODUCT_PROTOCOL="http"

        elif [[ "$var" == --https ]]; then
            PRODUCT_PROTOCOL="https"

        elif [[ "$var" =~ --host=.* ]]; then
            PRODUCT_HOST=$(echo "$var" | sed -e "s/--host=//g")

        elif [[ "$var" =~ --port=.* ]]; then
            PRODUCT_PORT=$(echo "$var" | sed -e "s/--port=//g")

        elif [[ "$var" =~ --threads=.* ]]; then
            THREADS=$(echo "$var" | sed -e "s/--threads=//g")

        elif [[ "$var" =~ --workspace-pool-size=.* ]]; then
            WORKSPACE_POOL_SIZE=$(echo "$var" | sed -e "s/--workspace-pool-size=//g")

        elif [[ "$var" =~ --rerun ]]; then
            local rerunAttempts=$(echo $@ | sed 's/.*--rerun\W\+\([0-9]\+\).*/\1/')
            if [[ "$rerunAttempts" =~ ^[0-9]+$ ]]; then
              RERUN_ATTEMPTS=$rerunAttempts
            else
              RERUN_ATTEMPTS=1
            fi

        elif [[ "$var" == --debug ]]; then
            DEBUG_OPTIONS="-Dmaven.failsafe.debug"
            NODE_CHROME_DEBUG_SUFFIX="-debug"

        elif [[ "$var" == --compare-with-ci ]]; then
            COMPARE_WITH_CI=true

        elif [[ "$var" == --multiuser ]]; then
            CHE_MULTIUSER=true

        elif [[ "$var" =~ --exclude=.* ]]; then
            EXCLUDE_PARAM=$(echo "$var" | sed -e "s/--exclude=//g")

        fi
    done
}

extractMavenOptions() {
    for var in "$@"; do
        if [[ "$var" =~ ^-D.* ]]; then
            MAVEN_OPTIONS="${MAVEN_OPTIONS} $var"
        elif [[ "$var" =~ ^-[[:alpha:]]$ ]]; then :
            MAVEN_OPTIONS="${MAVEN_OPTIONS} $var"
        elif [[ "$var" == "--skip-sources-validation" ]]; then :
            MAVEN_OPTIONS="${MAVEN_OPTIONS} -Dskip-enforce -Dskip-validate-sources"
        fi
    done
}


defineTestsScope() {
    for var in "$@"; do
        if [[ "$var" =~ --test=.* ]]; then
            TESTS_SCOPE="-Dit.test="$(echo "$var" | sed -e "s/--test=//g")
            THREADS=1

        elif [[ "$var" =~ --suite=.* ]]; then
            TESTS_SCOPE="-DrunSuite=src/test/resources/suites/"$(echo "$var" | sed -e "s/--suite=//g")

        elif [[ "$var" == --failed-tests ]]; then
            generateTestNgFailedReport $(fetchFailedTests)
            TESTS_SCOPE="-DrunSuite=${TESTNG_FAILED_SUITE}"

        elif [[ "$var" == --regression-tests ]]; then
            generateTestNgFailedReport $(findRegressions)
            TESTS_SCOPE="-DrunSuite=${TESTNG_FAILED_SUITE}"
        fi
    done

    ORIGIN_TESTS_SCOPE=${TESTS_SCOPE}
}

defineOperationSystemSpecificVariables() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        TMP_DIR=$(echo ${TMPDIR})
    else
        TMP_DIR="/tmp"
    fi
}

init() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[0;33m'
  NO_COLOUR='\033[0m'
}

defineRunMode() {
    for var in "$@"; do
        if [[ "$var" =~ -M.* ]]; then
            MODE=$(echo "$var" | sed -e "s/-M//g")
        fi
    done

    if [[ ${MODE} == "grid" ]]; then
        WEBDRIVER_PORT="4444"

        checkDockerRequirements
        checkDockerComposeRequirements

    elif [[ ${MODE} == "local" ]]; then
        GRID_OPTIONS="-Dgrid.mode=false"

    else
        echo "[TEST] Unrecognized mode "${MODE}
        echo "[TEST] Available modes: -M[local|grid]"
        exit 1
    fi
}

stopWebDriver() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        ps -cf | grep chromedriver | awk '{if(NR>0) print $2}' | while read -r pid; do kill "${pid}" > /dev/null; done
    else
        ps -fC chromedriver | awk '{if(NR>1) print $2}' | while read -r pid; do kill "${pid}" > /dev/null; done
    fi
}

startWebDriver() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        curl -s -o ${TMP_DIR}chromedriver_mac64.zip http://chromedriver.storage.googleapis.com/${WEBDRIVER_VERSION}/chromedriver_mac64.zip
        unzip -o ${TMP_DIR}chromedriver_mac64.zip -d ${TMP_DIR} > /dev/null
        chmod +x ${TMP_DIR}chromedriver
        ${TMP_DIR}chromedriver --port=9515 --no-sandbox > /dev/null &
    else
        curl -s -o ${TMP_DIR}/chromedriver_linux64.zip http://chromedriver.storage.googleapis.com/${WEBDRIVER_VERSION}/chromedriver_linux64.zip
        unzip -o ${TMP_DIR}/chromedriver_linux64.zip -d ${TMP_DIR} > /dev/null
        chmod +x ${TMP_DIR}/chromedriver
        ${TMP_DIR}/chromedriver --port=9515 --no-sandbox > /dev/null &
    fi
}

initRunMode() {
    if [[ ${MODE} == "local" ]]; then
        startWebDriver

    elif [[ ${MODE} == "grid" ]]; then
        export NODE_CHROME_DEBUG_SUFFIX
        docker-compose -p=selenium up -d > /dev/null
        docker-compose -p=selenium scale chromenode=${THREADS} > /dev/null

    else
        echo "[TEST] Unrecognized mode "${MODE}
        exit 1
    fi
}

stopSeleniumDockerContainers() {
    local containers=$(docker ps -qa --filter="name=selenium_*" | wc -l)
    if [[ ${containers} != "0" ]]; then
        echo "[TEST] Stopping selenium docker containers..."
        docker stop $(docker ps -qa --filter="name=selenium_*")
        docker rm $(docker ps -qa --filter="name=selenium_*")
    fi
}

checkDockerRequirements() {
    command -v docker >/dev/null 2>&1 || {
        echo >&2 -e "[TEST] Could not find Docker client, please install it.\n    https://docs.docker.com/engine/installation/"
        exit 1;
    }
}

checkDockerComposeRequirements() {
    command -v docker-compose >/dev/null 2>&1 || {
        echo >&2 -e "[TEST] Could not find Docker Compose client, please install it.\n    https://docs.docker.com/compose/install/"
        exit 1;
    }
}

checkIfProductIsRun() {
    local url=${PRODUCT_PROTOCOL}"://"${PRODUCT_HOST}:${PRODUCT_PORT}${API_SUFFIX};

    curl -s -X OPTIONS ${url} > /dev/null
    if [[ $? != 0 ]]; then
        echo "[TEST] "${url}" is down"
        exit 1
    fi
}

prepareTestSuite() {
    local suitePath=${ORIGIN_TESTS_SCOPE:11}
    TMP_SUITE_PATH="/tmp/"$(basename "${suitePath}")
    rm -f ${TMP_SUITE_PATH}
    cp -f ${suitePath} /tmp

    TESTS_SCOPE="-DrunSuite=${TMP_SUITE_PATH}"

    # set number of threads directly in the suite
    sed -i -e "s#thread-count=\"[^\"]*\"#thread-count=\"${THREADS}\"#" "$TMP_SUITE_PATH"
}

printHelp() {
    local usage="
Usage: ${CALLER} [-Mmode] [options] [tests scope]

Options:
    --http                              Use 'http' protocol to connect to product
    --https                             Use 'https' protocol to connect to product
    --host=<PRODUCT_HOST>               Set host where product is deployed
    --port=<PRODUCT_PORT>               Set port of the product, default is 8080
    --multiuser                         Run tests of Multi User Che

Modes (defines environment to run tests):
    -Mlocal                             All tests will be run in a Web browser on the developer machine.
                                        Recommended if test visualization is needed and for debugging purpose.

       Options that go with 'local' mode:
       --web-driver-version=<VERSION>    To use the specific version of the WebDriver, be default the latest will be used: "${WEBDRIVER_VERSION}"
       --web-driver-port=<PORT>          To run WebDriver on the specific port, by default: "${WEBDRIVER_PORT}"
       --threads=<THREADS>               Number of tests that will be run simultaneously. It also means the very same number of
                                        Web browsers will be opened on the developer machine.
                                        Default value is in range [2,5] and depends on available RAM.

    -Mgrid (default)                    All tests will be run in parallel among several docker containers.
                                        One container per thread. Recommended to run test suite.

        Options that go with 'grid' mode:
        --threads=<THREADS>             Number of tests that will be run simultaneously.
                                        Default value is in range [2,5] and depends on available RAM.

Define tests scope:
    --test=<TEST_CLASS>                 Single test/package to run.
                                        For example: '--test=DialogAboutTest', '--test=org.eclipse.che.selenium.git.**'.
    --suite=<SUITE>                     Test suite to run, found:
"$(for x in $(ls -1 src/test/resources/suites); do echo "                                            * "$x; done)"
    --exclude=<TEST_GROUPS_TO_EXCLUDE>  Comma-separated list of test groups to exclude from execution.
                                        For example, use '--exclude=github' to exclude GitHub-related tests.

Handle failing tests:
    --failed-tests                      Rerun failed tests that left after the previous try
    --regression-tests                  Rerun regression tests that left after the previous try
    --rerun [ATTEMPTS]                  Automatically rerun failing tests.
                                        Default attempts number is 1.
    --compare-with-ci [BUILD NUMBER]    Compare failed tests with results on CI server.
                                        Default build is the latest.

Other options:
    --debug                             Run tests in debug mode
    --skip-sources-validation           Fast build. Skips source validation and enforce plugins
    --workspace-pool-size=[<SIZE>|auto] Size of test workspace pool.
                                        Default value is 0, that means that test workspaces are created on demand.

HOW TO of usage:
    Test Eclipse Che single user assembly:
        ${CALLER}

    Test Eclipse Che multi user assembly:
        ${CALLER} --multiuser

    Test Eclipse Che assembly and automatically rerun failing tests:
        ${CALLER} --rerun [ATTEMPTS]

    Run single test or package of tests:
        ${CALLER} <...> --test=<TEST>

    Run suite:
        ${CALLER} <...> --suite=<PATH_TO_SUITE>

    Rerun failed tests:
        ${CALLER} <...> --failed-tests
        ${CALLER} <...> --failed-tests --rerun [ATTEMPTS]

    Debug selenium test:
        ${CALLER} -Mlocal --test=<TEST> --debug

    Analyse tests results:
        ${CALLER} --compare-with-ci [BUILD NUMBER]
"

    printf "%s" "${usage}"
}

printRunOptions() {
    echo "[TEST]"
    echo "[TEST] =========== RUN OPTIONS ==========================="
    echo "[TEST] Mode                : ${MODE}"
    echo "[TEST] Rerun attempts      : ${RERUN_ATTEMPTS}"
    echo "[TEST] ==================================================="
    echo "[TEST] Product Protocol    : ${PRODUCT_PROTOCOL}"
    echo "[TEST] Product Host        : ${PRODUCT_HOST}"
    echo "[TEST] Product Port        : ${PRODUCT_PORT}"
    echo "[TEST] Product Config      : $(getProductConfig)"
    echo "[TEST] Tests               : ${TESTS_SCOPE}"
    echo "[TEST] Tests to exclude    : $(getExcludedGroups)"
    echo "[TEST] Threads             : ${THREADS}"
    echo "[TEST] Workspace pool size : ${WORKSPACE_POOL_SIZE}"
    echo "[TEST] Web browser         : ${BROWSER}"
    echo "[TEST] Web driver ver      : ${WEBDRIVER_VERSION}"
    echo "[TEST] Web driver port     : ${WEBDRIVER_PORT}"
    echo "[TEST] Additional opts     : ${GRID_OPTIONS} ${DEBUG_OPTIONS} ${MAVEN_OPTIONS}"
    echo "[TEST] ==================================================="
}

# convert failed tests methods in the unique list of test classes
# a.b.c.SomeTest.someMethod1
# a.b.c.SomeTest.someMethod2
#          |------> a.b.c.SomeTest
getTestClasses() {
    local tests=$@
    for t in ${tests[*]}
    do
        echo $(echo ${t} | sed  's/\(.*\)[.][^.]*/\1/')
    done
}

fetchRunTestsNumber() {
    local run=0
    for report in target/failsafe-reports/*.txt
    do
        if [[ -f ${report} ]]; then
            run=$((run + $(cat ${report} | grep "Tests run" | sed 's/Tests run:[[:space:]]\([0-9]*\).*/\1/')))
        fi
    done
    echo ${run}
}

# Returns unique records.
fetchFailedTests() {
    local fails=()
    for report in target/failsafe-reports/*.txt
    do
        if [[ -f ${report} ]]; then
            for item in $(cat ${report} | grep "<<< FAILURE!" | grep -e '(.*).*' | tr ' ' '_')
            do
                local method=$(echo ${item} | sed 's/\(.*\)(.*)_.*/\1/')
                local class=$(echo ${item} | sed 's/.*(\(.*\))_.*/\1/')
                fails+=(${class}'.'${method})
            done
        fi
    done

    for f in $(echo ${fails[@]} | tr ' ' '\n' | sort | uniq)
    do
        echo ${f}
    done
}

fetchFailedTestsNumber() {
    echo $(fetchFailedTests) | wc -w
}

detectLatestResultsUrl() {
    local build=$(curl -s ${BASE_ACTUAL_RESULTS_URL} | tr '\n' ' ' | sed 's/.*Last build (#\([0-9]\+\)).*/\1/')
    echo ${BASE_ACTUAL_RESULTS_URL}${build}"/testReport/"
}

# Fetches list of failed tests and failed configurations.
# Combines them into a single unique list.
fetchActualResults() {
    unset ACTUAL_RESULTS
    unset ACTUAL_RESULTS_URL

    # define the URL of CI job to compare local result with result on CI
    local multiuserToken=$([[ "$CHE_MULTIUSER" == true ]] && echo "-multiuser")
    local infrastructureToken=$([[ "$CHE_INFRASTRUCTURE" == "openshift" ]] && echo "-ocp" || echo "-$CHE_INFRASTRUCTURE")
    local nameOfCIJob="che-integration-tests${multiuserToken}-master${infrastructureToken}"

    [[ -z ${BASE_ACTUAL_RESULTS_URL+x} ]] && { BASE_ACTUAL_RESULTS_URL="https://ci.codenvycorp.com/view/qa/job/${nameOfCIJob}/"; }

    local build=$(echo $@ | sed 's/.*--compare-with-ci\W\+\([0-9]\+\).*/\1/')
    if [[ ! ${build} =~ ^[0-9]+$ ]]; then
        ACTUAL_RESULTS_URL=$(detectLatestResultsUrl)
    else
        ACTUAL_RESULTS_URL=${BASE_ACTUAL_RESULTS_URL}${build}"/testReport/"
    fi

    # get list of failed tests from CI server, remove duplicates from it and sort
    ACTUAL_RESULTS=$(echo $( curl -s ${ACTUAL_RESULTS_URL} | \
                           tr '>' '\n' | tr '<' '\n' | tr '"' '\n'  | \
                           grep --extended-regexp "^[a-z_$][a-z0-9_$.]*\.[A-Z_$][a-zA-Z0-9_$]*\.[a-z_$][a-zA-Z0-9_$]*$" | \
                           tr ' ' '\n' | sort | uniq ))
}

findRegressions() {
    local expected=(${ACTUAL_RESULTS[*]})
    local failed=$(fetchFailedTests)

    for f in ${failed[*]}
    do
        local skip=false
        for e in ${expected[*]}
        do
            [[ ${f} == ${e} ]] && { skip=true; break; }
        done
        [[ ${skip} == true ]] || echo ${f}
    done
}

# Analyses tests results by comparing with the actual ones.
analyseTestsResults() {
    echo "[TEST]"
    echo -e "[TEST] "${YELLOW}"RESULTS ANALYSE:"${NO_COLOUR}

    echo "[TEST]"
    echo -e "[TEST] Command line: ${BLUE}${CUR_DIR}/${CALLER} $@${NO_COLOUR}"
    echo "[TEST]"

    if [[ ${COMPARE_WITH_CI} == true ]]; then
        echo -e "[TEST] CI results ${BLUE}${ACTUAL_RESULTS_URL}${NO_COLOUR}"
        echo -e "[TEST] \t- Failed: $(printf "%5s" "$(echo ${ACTUAL_RESULTS[@]} | wc -w)") (unique tests)"
        echo "[TEST]"
    fi

    local run=$(fetchRunTestsNumber)
    local runToDisplay=$(printf "%7s" "${run}")
    local fails=$(fetchFailedTests)
    local totalFails=$(echo ${fails[@]} | wc -w)
    local totalFailsToDisplay=$(printf "%5s" "${totalFails}")

    echo "[TEST] Local results:"
    echo -e "[TEST] \t- Run: \t${runToDisplay}"
    echo -e "[TEST] \t- Failed: ${totalFailsToDisplay}"

    if [[ ${COMPARE_WITH_CI} == true ]]; then
        if [[ ! ${totalFails} -eq 0 ]]; then
            for r in $(echo ${fails[@]} | tr ' ' '\n' | sort)
            do
                echo -e "[TEST] \t"${r}
            done
        fi
        echo "[TEST]"

        echo -e -n "[TEST] Comparing with "${BLUE}${ACTUAL_RESULTS_URL}${NO_COLOUR}
        if [[ ${ACTUAL_RESULTS_URL} != $(detectLatestResultsUrl) ]]; then
            echo -e ${RED}" (not the latest results)"${NO_COLOUR}
        else
            echo
        fi
        echo "[TEST] If a test failed then it is NOT marked as regression."
    fi

    echo "[TEST]"

    if [[ ${run} == "0" ]]; then
        echo -e "[TEST] "${RED}"NO RESULTS"${NO_COLOUR}
    else
        local regressions=$(findRegressions)
        local totalRegressions=$(echo ${regressions[@]} | wc -w)
        if [[ ${totalRegressions} -eq 0 ]]; then
            echo -e -n "[TEST] "${GREEN}"NO REGRESSION! "${NO_COLOUR}
            if [[ ! ${totalFails} -eq 0 ]]; then
                echo -e ${RED}"CHECK THE FAILED TESTS. THEY MIGHT FAIL DUE TO DIFFERENT REASON."${NO_COLOUR}
            else
                echo -e ${GREEN}"NO FAILED TESTS, GREAT JOB!"${NO_COLOUR}
            fi
        else
            echo -e "[TEST] "${RED}"REGRESSION"${NO_COLOUR}" ("${totalRegressions}"):"

            for r in $(echo ${regressions[@]} | tr ' ' '\n' | sort)
            do
                echo -e "[TEST] \t"${r}
            done
        fi
    fi

    echo "[TEST]"
    echo "[TEST]"
}

printProposals() {
    echo -e "[TEST] "${YELLOW}"PROPOSALS:"${NO_COLOUR}
    local cmd=$(echo $@ | sed -e "s/--rerun\W*[0-9]*//g" | \
                          sed -e "s/-M[^ ]*//g" | \
                          sed -e "s/--failed-tests//g" | \
                          sed -e "s/--regression-tests//g" | \
                          sed -e "s/--suite=[^ ]*//g " | \
                          sed -e "s/--test*=[^ ]*//g " | \
                          sed -e "s/--compare-with-ci\W*[0-9]*//g" | \
                          sed -e "s/--threads=[0-9]*//g" | \
                          sed -e "s/--workspace-pool-size=auto|[0-9]*//g")

    local regressions=$(findRegressions)
    local total=$(echo ${regressions[@]} | wc -w)

    if [[ ! ${total} -eq 0 ]]; then
        echo "[TEST]"
        echo "[TEST] Try rerun all tests:"
        echo -e "[TEST] \t${BLUE}${CUR_DIR}/${CALLER} ${cmd} --threads=${THREADS} -Mlocal --failed-tests${NO_COLOUR}"
        echo -e "[TEST] \t${BLUE}${CUR_DIR}/${CALLER} ${cmd} --threads=${THREADS} -Mgrid --failed-tests${NO_COLOUR}"

        echo "[TEST]"
        if [[ ${total} -lt 50 ]]; then
            echo "[TEST] Or run them one by one:"
            for r in $(echo ${regressions[@]} | tr ' ' '\n' | sed  's/\(.*\)[.][^.]*/\1/' | sort | uniq)
            do
                echo -e "[TEST] \t${BLUE}${CUR_DIR}/${CALLER} ${cmd} -Mlocal --test=${r}${NO_COLOUR}"
            done
            echo "[TEST]"
            echo -e "[TEST] You might need add ${BLUE}--debug${NO_COLOUR} option for debugging purpose."
        fi
    fi

    echo "[TEST]"
    echo "[TEST] To compare tests results with the latest results on CI job"
    echo -e "[TEST] \t${BLUE}${CUR_DIR}/${CALLER} ${cmd} --compare-with-ci${NO_COLOUR}"
    echo "[TEST]"
    echo "[TEST] To compare local tests results with certain build on CI job"
    echo -e "[TEST] \t${BLUE}${CUR_DIR}/${CALLER} ${cmd} --compare-with-ci [BUILD NUMBER]${NO_COLOUR}"
    echo "[TEST]"
    echo "[TEST]"
}

printElapsedTime() {
    local totalTime=$(($(date +%s)-${START_TIME}))
    echo "[TEST]"
    echo "[TEST] Elapsed time: "$((${totalTime} / 3600))"hrs "$(( $((${totalTime} / 60)) % 60))"min "$((${totalTime} % 60))"sec"
}

runTests() {
    if [[ ${TESTS_SCOPE} =~ -DrunSuite ]]; then
        prepareTestSuite
    fi

    printRunOptions

    mvn clean verify -Pselenium-test \
                ${TESTS_SCOPE} \
                -Dche.host=${PRODUCT_HOST} \
                -Dche.port=${PRODUCT_PORT} \
                -Dche.protocol=${PRODUCT_PROTOCOL} \
                -Ddocker.interface.ip=$(detectDockerInterfaceIp) \
                -Ddriver.port=${WEBDRIVER_PORT} \
                -Ddriver.version=${WEBDRIVER_VERSION} \
                -Dbrowser=${BROWSER} \
                -Dche.threads=${THREADS} \
                -Dche.workspace_pool_size=${WORKSPACE_POOL_SIZE} \
                -DexcludedGroups="$(getExcludedGroups)" \
                ${DEBUG_OPTIONS} \
                ${GRID_OPTIONS} \
                ${MAVEN_OPTIONS}
}

# Return list of product features
getProductConfig() {
  local testGroups=${CHE_INFRASTRUCTURE}

  if [[ ${CHE_MULTIUSER} == true ]]; then
    testGroups=${testGroups},multiuser
  else
    testGroups=${testGroups},singleuser
  fi

  echo ${testGroups}
}

# Prepare list of test groups to exclude.
# It consists of "--exclude" parameter value + list of groups which don't comply with product config
getExcludedGroups() {
    local excludeParamArray=(${EXCLUDE_PARAM//,/ })

    local productConfig=$(getProductConfig)
    local productConfigArray=(${productConfig//,/ })

    local uncomplyingGroups=(${SUPPORTED_INFRASTRUCTURES[@]} singleuser multiuser)

    for productConfigGroup in ${productConfigArray[*]}; do
        for i in ${!uncomplyingGroups[@]}; do
            if [[ "${productConfigGroup}" == "${uncomplyingGroups[i]}" ]]; then
                unset uncomplyingGroups[i]
            fi
        done
    done

    local excludedGroups=("${uncomplyingGroups[@]}" "${excludeParamArray[@]}")
    echo $(IFS=$','; echo "${excludedGroups[*]}")
}

# Reruns failed tests
rerunTests() {
    local regressions=$(findRegressions)
    local total=$(echo ${regressions[@]} | wc -w)

    if [[ ! ${total} -eq 0 ]]; then
        local rerunCounter=$1 && shift

        analyseTestsResults $@
        generateFailSafeReport
        printProposals $@
        storeTestReport
        printElapsedTime

        echo -e "[TEST]"
        echo -e "[TEST] ${YELLOW}---------------------------------------------------${NO_COLOUR}"
        echo -e "[TEST] ${YELLOW}RERUNNING FAILED TESTS IN ONE THREAD: ATTEMPT #${rerunCounter}${NO_COLOUR}"
        echo -e "[TEST] ${YELLOW}---------------------------------------------------${NO_COLOUR}"

        defineTestsScope "--failed-tests"
        runTests

        if [[ ${rerunCounter} < ${RERUN_ATTEMPTS} ]]; then
            rerunTests $(($rerunCounter+1)) $@
        fi
    fi
}

# Finds regressions and generates testng-failed.xml suite bases on them.
generateTestNgFailedReport() {
    local failsClasses=$(getTestClasses $@)

    if [[ -d ${FAILSAFE_DIR} ]]; then
        echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > ${TESTNG_FAILED_SUITE}
        echo "<suite thread-count=\"1\" verbose=\"0\" parallel=\"classes\" name=\"Failed suite\">" >> ${TESTNG_FAILED_SUITE}
        echo -e "\t<test name=\"Surefire test\">" >> ${TESTNG_FAILED_SUITE}
        echo -e "\t\t<classes>" >> ${TESTNG_FAILED_SUITE}

        for f in $(echo ${failsClasses[@]} | tr ' ' '\n' | sort | uniq)
        do
            echo -e -n "\t\t\t<class name=\"" >> ${TESTNG_FAILED_SUITE}
            echo -e -n ${f} >> ${TESTNG_FAILED_SUITE}
            echo -e "\"/>" >> ${TESTNG_FAILED_SUITE}
        done
        echo -e "\t\t</classes>" >> ${TESTNG_FAILED_SUITE}
        echo -e "\t</test>" >> ${TESTNG_FAILED_SUITE}
        echo -e "</suite>" >> ${TESTNG_FAILED_SUITE}
    fi
}

# generates and updates failsafe report
generateFailSafeReport () {
    mvn -q surefire-report:failsafe-report-only ${MAVEN_OPTIONS}
    mvn -q site -DgenerateReports=false ${MAVEN_OPTIONS}

    echo "[TEST]"
    echo -e "[TEST] ${YELLOW}REPORT:${NO_COLOUR}"

    if [[ ! -f ${FAILSAFE_REPORT} ]]; then
        echo -e "[TEST] Failsafe report: ${BLUE}file://${CUR_DIR}/${FAILSAFE_REPORT}${NO_COLOUR} not found."
        echo "[TEST] Either maven surefire report plugin failed or tests haven't been run at all."
        echo "[TEST]"
        echo "[TEST] To regenerate report manually use the command below:"
        echo -e "[TEST] \t${BLUE}${CUR_DIR}/${CALLER} --compare-with-ci${NO_COLOUR}"
        echo "[TEST]"
        echo "[TEST]"
        exit 1
    fi

    local regressions=$(findRegressions)

    # add REGRESSION mark
    for r in ${regressions[*]}
    do
        local test=$(basename $(echo ${r} | tr '.' '/') | sed 's/\(.*\)_.*/\1/')

        local divTag="<a href=\"#"${r}"\">"${test}"<\/a>"
        local divRegTag="<h2>REGRESSION<\/h2>"${divTag}
        sed -i 's/'"${divTag}"'/'"${divRegTag}"'/' ${FAILSAFE_REPORT}
    done

    # attach screenshots
    for f in target/screenshots/*
    do
        local test=$(basename ${f} | sed 's/\(.*\)_.*/\1/')
        local divTag="<div id=\""${test}"error\" style=\"display:none;\">"
        local imgTag="<img src=\"..\/screenshots\/"$(basename ${f})"\">"
        sed -i "s/${divTag}/${divTag}${imgTag}/" ${FAILSAFE_REPORT}
    done

    echo "[TEST]"
    echo "[TEST] Failsafe report"
    echo -e "[TEST] \t${BLUE}file://${CUR_DIR}/${FAILSAFE_REPORT}${NO_COLOUR}"
    echo "[TEST]"
    echo "[TEST]"
}

storeTestReport() {
    mkdir -p ${TMP_DIR}/webdriver
    local report="${TMP_DIR}/webdriver/report$(date +%s).zip"

    rm -rf ${TMP_DIR}/webdriver/tmp
    mkdir target/suite
    if [[ -f ${TMP_SUITE_PATH} ]]; then
        cp ${TMP_SUITE_PATH} target/suite;
    fi
    zip -qr ${report} target/screenshots target/htmldumps target/workspace-logs target/webdriver-logs target/site target/failsafe-reports target/log target/bin target/suite

    echo -e "[TEST] Tests results and reports are saved to ${BLUE}${report}${NO_COLOUR}"
    echo "[TEST]"
    echo "[TEST] If target directory is accidentally cleaned it is possible to restore it: "
    echo -e "[TEST] \t${BLUE}rm -rf ${CUR_DIR}/target && unzip -q ${report} -d ${CUR_DIR}${NO_COLOUR}"
    echo "[TEST]"
}

checkBuild() {
    mvn package ${MAVEN_OPTIONS}
    [[ $? != 0 ]] && { exit 1; }
}

prepareToFirstRun() {
    checkIfProductIsRun
    cleanUpEnvironment
    initRunMode

    if [[ ${CHE_MULTIUSER} == false ]]; then
      prepareTestUsersForSingleuserChe
    else
      prepareTestUsersForMultiuserChe
    fi
}

prepareTestUsersForSingleuserChe() {
    export CHE_ADMIN_NAME=
    export CHE_ADMIN_EMAIL=
    export CHE_ADMIN_PASSWORD=
    export CHE_ADMIN_OFFLINE__TOKEN=

    export CHE_TESTUSER_NAME=che
    export CHE_TESTUSER_EMAIL=che@eclipse.org
    export CHE_TESTUSER_PASSWORD=secret
    export CHE_TESTUSER_OFFLINE__TOKEN=
}

prepareTestUsersForMultiuserChe() {
    export CHE_ADMIN_NAME=${CHE_ADMIN_NAME:-admin}
    export CHE_ADMIN_EMAIL=${CHE_ADMIN_EMAIL:-admin@admin.com}
    export CHE_ADMIN_PASSWORD=${CHE_ADMIN_PASSWORD:-admin}
    export CHE_ADMIN_OFFLINE__TOKEN=${CHE_ADMIN_OFFLINE__TOKEN}

    export CHE_TESTUSER_OFFLINE__TOKEN=${CHE_TESTUSER_OFFLINE__TOKEN}
    export CHE_SECOND_TESTUSER_OFFLINE__TOKEN=${CHE_SECOND_TESTUSER_OFFLINE__TOKEN}

    if [[ -n ${CHE_TESTUSER_EMAIL+x} ]] && [[ -n ${CHE_TESTUSER_PASSWORD+x} ]]; then
        return
    fi

    # create test user by executing kcadm.sh commands inside keycloak docker container if there are no its credentials among environment variables
    if [[ "${PRODUCT_HOST}" == "$(detectDockerInterfaceIp)" ]] || [[ "${CHE_INFRASTRUCTURE}" == "openshift" ]]; then
        # create default test user
        local time=$(date +%s)
        export CHE_TESTUSER_NAME=${CHE_TESTUSER_NAME:-user${time}}
        export CHE_TESTUSER_EMAIL=${CHE_TESTUSER_EMAIL:-${CHE_TESTUSER_NAME}@1.com}
        export CHE_TESTUSER_PASSWORD=${CHE_TESTUSER_PASSWORD:-${time}}
        echo "[TEST] Creating default test user with name '$CHE_TESTUSER_NAME'..."
        NEW_USER_ID=$(createUser ${CHE_TESTUSER_NAME} ${CHE_TESTUSER_EMAIL} ${CHE_TESTUSER_PASSWORD})
        if [[ -z ${NEW_USER_ID} ]]; then
           # set test user credentials to be equal to admin ones in case of problem with creation of user
           echo -e "${RED}[WARN] There is a problem with creation of default test user in Keycloak server: '${response}'.${NO_COLOUR}"
           echo -e "Admin user will be used as default test user."
           CHE_TESTUSER_NAME=${CHE_ADMIN_NAME}
           CHE_TESTUSER_EMAIL=${CHE_ADMIN_EMAIL}
           CHE_TESTUSER_PASSWORD=${CHE_ADMIN_PASSWORD}
        fi

        # create second test user
        time=$(date +%s)
        export CHE_SECOND_TESTUSER_NAME=${CHE_SECOND_TESTUSER_NAME:-user${time}}
        export CHE_SECOND_TESTUSER_EMAIL=${CHE_SECOND_TESTUSER_EMAIL:-${CHE_SECOND_TESTUSER_NAME}@1.com}
        export CHE_SECOND_TESTUSER_PASSWORD=${CHE_SECOND_TESTUSER_PASSWORD:-${time}}
        echo "[TEST] Creating second test user with name '$CHE_SECOND_TESTUSER_NAME'..."
        NEW_SECOND_USER_ID=$(createUser ${CHE_SECOND_TESTUSER_NAME} ${CHE_SECOND_TESTUSER_EMAIL} ${CHE_SECOND_TESTUSER_PASSWORD})
        if [[ -z ${NEW_SECOND_USER_ID} ]]; then
           # set test user credentials to be equal to admin ones in case of problem with creation of user
           echo -e "${RED}[WARN] There is a problem with creation of second test user in Keycloak server: '${response}'.${NO_COLOUR}"
           echo -e "Admin user will be used as a second test user."
           CHE_SECOND_TESTUSER_NAME=${CHE_ADMIN_NAME}
           CHE_SECOND_TESTUSER_EMAIL=${CHE_ADMIN_EMAIL}
           CHE_SECOND_TESTUSER_PASSWORD=${CHE_ADMIN_PASSWORD}
        fi

        # add role "read-token" of client "broker" to admin user
        docker exec -i $(getKeycloakContainerId) sh -c "keycloak/bin/kcadm.sh add-roles -r che --uusername ${CHE_ADMIN_NAME} --cclientid broker --rolename read-token --no-config --server http://localhost:8080/auth --user ${CHE_ADMIN_NAME} --password ${CHE_ADMIN_PASSWORD} --realm master"
    fi
}

createUser() {
    local username=$1
    local email=$2
    local password=$3

    local kc_container_id=$(getKeycloakContainerId)

    local cli_auth="--no-config --server http://localhost:8080/auth --user ${CHE_ADMIN_NAME} --password ${CHE_ADMIN_PASSWORD} --realm master"
    local response=$(docker exec -i $kc_container_id sh -c "keycloak/bin/kcadm.sh create users -r che -s username=${username} -s enabled=true $cli_auth 2>&1")
    if [[ "$response" =~ "Created new user with id" ]]; then
       local newUserId=$(echo "$response" | grep "Created new user with id" | sed -e "s#Created new user with id ##" | sed -e "s#'##g")
       # set test user's permanent password
       docker exec -i $kc_container_id sh -c "keycloak/bin/kcadm.sh set-password -r che --username ${username} --new-password ${password} $cli_auth"
       # set email of test user to ${cheTestUserEmail}
       docker exec -i $kc_container_id sh -c "keycloak/bin/kcadm.sh update users/${newUserId} -r che --set email=${email} $cli_auth"
       # add realm role "user" test user
       docker exec -i $kc_container_id sh -c "keycloak/bin/kcadm.sh add-roles -r che --uusername ${username} --rolename user $cli_auth"
       # add role "read-token" of client "broker" to test user
       docker exec -i $kc_container_id sh -c "keycloak/bin/kcadm.sh add-roles -r che --uusername ${username} --cclientid broker --rolename read-token $cli_auth"

       echo $newUserId
    fi
}

removeUser() {
    local userId=$1
    local username=$2

    local cli_auth="--no-config --server http://localhost:8080/auth --user ${CHE_ADMIN_NAME} --password ${CHE_ADMIN_PASSWORD} --realm master"
    docker exec -i $(getKeycloakContainerId) sh -c "keycloak/bin/kcadm.sh delete users/${userId} -r che -s username=${username} ${cli_auth} 2>&1"
}

getKeycloakContainerId() {
    if [[ "${CHE_INFRASTRUCTURE}" == "openshift" ]]; then
        echo $(docker ps | grep 'keycloak_keycloak-' | cut -d ' ' -f1)
    else
        echo $(docker ps | grep che_keycloak | cut -d ' ' -f1)
    fi
}

testProduct() {
    runTests

    if [[ ${RERUN_ATTEMPTS} > 0 ]]; then
        MAVEN_OPTIONS="${MAVEN_OPTIONS} -o"
        rerunTests 1 $@
    fi
}

run() {
    if [[ $@ =~ --help ]]; then
        printHelp
        exit
    fi

    START_TIME=$(date +%s)

    trap cleanUpEnvironment EXIT

    initVariables
    init
    extractMavenOptions $@
    checkBuild

    checkParameters $@
    defineOperationSystemSpecificVariables
    defineRunMode $@

    defineTestsScope $@
    applyCustomOptions $@

    if [[ ${COMPARE_WITH_CI} == true ]]; then
        fetchActualResults $@
    else
        prepareToFirstRun
        testProduct $@
    fi

    analyseTestsResults $@

    if [[ ${COMPARE_WITH_CI} == false ]]; then
        generateFailSafeReport
        printProposals $@
        storeTestReport
        printElapsedTime
    fi
}

run "$@"
