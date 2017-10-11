How to run Selenium tests
------------------

#### 1. Register OAuth application 

Go to [OAuth application page](https://github.com/settings/applications/new) and register a new application:
* `Application name` : `Che`
* `Homepage URL` : `http://<YOUR_IP_ADDRESS>:8080`
* `Application description` : `Che`
* `Authorization callback URL` : `http://<YOUR_IP_ADDRESS>:8080/api/oauth/callback`

Substitute `CHE_OAUTH_GITHUB_CLIENTID` and `CHE_OAUTH_GITHUB_CLIENTSECRET` properties in `che.env` with `Client ID` and `Client Secret` taken from 
newly created [OAuth application](https://github.com/settings/developers).

#### 2. Add configuration file

Set `CHE_LOCAL_CONF_DIR` environment variable and point to the folder where selenium tests configuration will be stored.
Create file `selenium.properties` in that folder with the following content:
```
# GitHub account credentials
github.username=<MAIN_GITHUB_USERNAME>
github.password=<MAIN_GITHUB_PASSWORD>
github.auxiliary.username=<AUXILIARY_GITHUB_USERNAME>
github.auxiliary.password=<AUXILIARY_GITHUB_PASSWORD>

# Google account credentials (IMAP has to be enabled)
google.user=<GOOGLE_USER>
google.password=<GOOGLE_PASSWORD>
```

#### 3. Prepare repository 
Fork all repositories from [https://github.com/idexmai?tab=repositories](https://github.com/idexmai?tab=repositories) into the main GitHub account.
Fork the repository [https://github.com/iedexmain1/pull-request-plugin-fork-test](https://github.com/iedexmain1/pull-request-plugin-fork-test) into the auxiliary GitHub account.

#### 4. Start Eclipse Che

Follow the guide: [https://github.com/eclipse/che](https://github.com/eclipse/che)

#### 5. Run tests

Simply launch `./selenium-tests.sh`

Run tests configuration properties
--------------------------------------
```
Usage: ./selenium-tests.sh [-Mmode] [options] [tests scope]

Options:
    --http                              Use 'http' protocol to connect to product
    --https                             Use 'https' protocol to connect to product
    --host=<PRODUCT_HOST>               Set host where product is deployed
    --multiuser                         Run tests of Multi User Che

Modes (defines environment to run tests):
    local                               All tests will be run in a Web browser on the developer machine.
                                        Recommended if test visualization is needed and for debugging purpose.

        Options that go with 'local' mode:
        --web-driver-version=<VERSION>  To use the specific version of the WebDriver, be default the latest will be used: 2.30
        --web-driver-port=<PORT>        To run WebDriver on the specific port, by default: 9515
        --threads=<THREADS>             Number of tests that will be run simultaneously. It also means the very same number of
                                        Web browsers will be opened on the developer machine.
                                        Default value is in range [2,5] and depends on available RAM.


    grid (default)                      All tests will be run in parallel among several docker containers.
                                        One container per thread. Recommended to run test suite.

        Options that go with 'grid' mode:
            --threads=<THREADS>         Number of tests that will be run simultaneously.
                                        Default value is in range [2,5] and depends on available RAM.

Define tests scope:
    --all-tests                         Run all tests within the suite despite of <exclude>/<include> sections in the test suite.
    --test=<TEST_CLASS>                 Single test to run
    --suite=<SUITE>                     Test suite to run, found:
                                            * CheSuite.xml

Handle failing tests:
    --failed-tests                      Rerun failed tests that left after the previous try
    --regression-tests                  Rerun regression tests that left after the previous try
    --rerun                             Automatically rerun failing tests
    --compare-with-ci                   Compare failed tests with results on CI server

Other options:
    --debug                             Run tests in debug mode

HOW TO of usage:
    Test Eclipse Che single user assembly:
        ./selenium-tests.sh -Mgrid
        ./selenium-tests.sh

    Test Eclipse Che multi user assembly:
        ./selenium-tests.sh --multiuser

    Test Eclipse Che assembly and automatically rerun failing tests:
        ./selenium-tests.sh -Mgrid --rerun

    Run single test or package of tests:
        ./selenium-tests.sh <...> --test=<TEST>

    Run suite:
        ./selenium-tests.sh <...> --suite=<PATH_TO_SUITE>

    Rerun failed tests:
        ./selenium-tests.sh <...> --failed-tests
        ./selenium-tests.sh <...> --failed-tests --rerun

    Debug selenium test:
        ./selenium-tests.sh -Mlocal --test=<TEST> --debug

    Analyse tests results:
        ./selenium-tests.sh --compare-with-ci [CI job number]
```


