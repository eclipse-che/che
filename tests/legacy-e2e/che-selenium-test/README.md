How to run Selenium tests
------------------

#### 1. Register OAuth application 

Go to [OAuth application page](https://github.com/settings/applications/new) and register a new application:
* `Application name` : `Che`
* `Homepage URL` : `http://<YOUR_IP_ADDRESS><:YOUR_CHE_PORT>`
* `Application description` : `Che`
* `Authorization callback URL` : `http://<YOUR_IP_ADDRESS><:YOUR_CHE_PORT>/api/oauth/callback`

Substitute `CHE_OAUTH_GITHUB_CLIENTID` and `CHE_OAUTH_GITHUB_CLIENTSECRET` properties in `che.env` with `Client ID` and `Client Secret` taken from 
newly created [OAuth application](https://github.com/settings/developers).

#### 2. Configure selenium tests
In case of running GitHub-related tests (which are run by default) you need to define GitHub test users credentials. Set `CHE_LOCAL_CONF_DIR` environment variable 
and point to the folder where selenium tests configuration will be stored. Then create file with `.properties` extension in that folder 
with the following content:
```
# GitHub account credentials
github.username=<MAIN_GITHUB_USERNAME>
github.password=<MAIN_GITHUB_PASSWORD>
github.auxiliary.username=<AUXILIARY_GITHUB_USERNAME>
github.auxiliary.password=<AUXILIARY_GITHUB_PASSWORD>
```

In case of running the tests for Eclipse Che in Multi User mode you can set your own credentials of test user or admin instead of default ones
```
export CHE_ADMIN_NAME=<che_admin_name>
export CHE_ADMIN_EMAIL=<che_admin_email>
export CHE_ADMIN_PASSWORD=<che_admin_password>

export CHE_TESTUSER_NAME=<che_test_user_name>
export CHE_TESTUSER_EMAIL=<che_test_user_email>
export CHE_TESTUSER_PASSWORD=<che_test_user_password>
```

Default values:
- CHE_ADMIN_NAME: "admin"
- CHE_ADMIN_EMAIL: "admin@admin.com"
- CHE_ADMIN_PASSWORD: "admin"

#### 3. Start Eclipse Che

Follow the guide: [https://github.com/eclipse/che](https://github.com/eclipse/che)

#### 4. Run tests

Simply launch `./selenium-tests.sh`

### How to run tests on OpenShift
#### 1. Set workspace runtime infrastructure implementation
export CHE_INFRASTRUCTURE=openshift
#### 2. Run tests and specify host and port of Che deployed to OpenShift
Launch `./selenium-tests.sh --host=<Che host on openshift> --port=80`

Example: `./selenium-tests.sh --host=che-spi.192.168.99.100.nip.io --port=80`

In case of running the tests for Eclipse Che on OCP, which is run remotely with default Eclipse Che admin and test user credentials: 
```
export OPENSHIFT_USERNAME=<openshift_web_console_username>
export OPENSHIFT_PASSWORD=<openshift_web_console_password>
export OPENSHIFT_TOKEN=<openshift_web_console_bearer_auth_token>
export OPENSHIFT_CHE_NAMESPACE=<namespace_of_eclipse_che_deployed_on_openshift>
export OPENSHIFT_URL=<url_of_openshift_web_console>
```
where `OPENSHIFT_TOKEN` is optional and is aimed to replace username/password when OpenShift is configured with OAuth. 


Default values:
- OPENSHIFT_USERNAME: "developer"
- OPENSHIFT_PASSWORD: "any"
- OPENSHIFT_CHE_NAMESPACE: "che"
- OPENSHIFT_URL: https://<che_host_ip>:8443


Run tests configuration properties
--------------------------------------
```
Usage: ./selenium-tests.sh [-Mmode] [options] [tests scope]

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
        --web-driver-version=<VERSION>  To use the specific version of the WebDriver, be default the latest will be used: 2.30
        --web-driver-port=<PORT>        To run WebDriver on the specific port, by default: 9515
        --threads=<THREADS>             Number of tests that will be run simultaneously. It also means the very same number of
                                        Web browsers will be opened on the developer machine.
                                        Default value is in range [2,5] and depends on available RAM.

    -Mgrid (default)                    All tests will be run in parallel among several docker containers.
                                        One container per thread. Recommended to run test suite.

        Options that go with 'grid' mode:
            --threads=<THREADS>         Number of tests that will be run simultaneously.
                                        Default value is in range [2,5] and depends on available RAM.

Define tests scope:
    --test=<TEST_CLASS>                 Single test/package to run.
                                        For example: '--test=DialogAboutTest', '--test=org.eclipse.che.selenium.git.**'. 
    --suite=<SUITE>                     Test suite to run. Default suite is CheSuite.xml.
    --exclude=<TEST_GROUPS_TO_EXCLUDE>  Comma-separated list of test groups to exclude from execution.
                                        For example, use '--exclude=github' to exclude GitHub-related tests.
                                        
Handle failing tests:
    --failed-tests                      Rerun failed tests that left after the previous try
    --regression-tests                  Rerun regression tests that left after the previous try
    --rerun [ATTEMPTS]                  Automatically rerun failing tests.
                                        Default attempts number is 1.
    --compare-with-ci [BUILD NUMBER]    Compare failed tests with results on CI server.
                                        Default build is the latest.
    --fail-script-on-failed-tests       Fail webdriver.sh if tests failed.

Other options:
    --debug                             Run tests in debug mode
    --skip-sources-validation           Fast build. Skips source validation and enforce plugins
    --workspace-pool-size=[<SIZE>|auto] Size of test workspace pool.
                                        Default value is 0, that means that test workspaces are created on demand.
    --include-tests-under-repair        Include tests which permanently fail and so belong to group 'UNDER REPAIR'
    --include-flaky-tests               Include tests which randomly fail and so belong to group 'FLAKY'        

HOW TO of usage:
    Test Eclipse Che single user assembly:
        ./selenium-tests.sh

    Test Eclipse Che multi user assembly:
        ./selenium-tests.sh --multiuser

    Test Eclipse Che assembly and automatically rerun failing tests:
        ./selenium-tests.sh --rerun [ATTEMPTS]

    Run single test or package of tests:
        ./selenium-tests.sh <...> --test=<TEST>

    Run suite:
        ./selenium-tests.sh <...> --suite=<PATH_TO_SUITE>

    Include tests which belong to groups 'UNDER REPAIR' and 'FLAKY'
        ./selenium-tests.sh --include-tests-under-repair --include-flaky-tests
         
    Rerun failed tests:
        ./selenium-tests.sh <...> --failed-tests
        ./selenium-tests.sh <...> --failed-tests --rerun [ATTEMPTS]

    Debug selenium test:
        ./selenium-tests.sh -Mlocal --test=<TEST> --debug

    Analyse tests results:
        ./selenium-tests.sh --compare-with-ci [BUILD NUMBER]
```

Test development tips
--------------------------------------
It's impossible to use `@Test(dependsOnMethods)` to express dependency between the tests because it breaks parallel execution of test classes (an issue https://github.com/cbeust/testng/issues/1773).

`@Test(priority)` can be used instead, and dependent test will be skipped if preceding test with higher priority from the same test class has failed.
