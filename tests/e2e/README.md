# Module for launch E2E tests related to Che 7

## Requirements

- node 16.x
- "Chrome" browser 114.x or later
- deployed Che 7 with accessible URL

## Before launch

**Perform commands:**

- `export TS_SELENIUM_BASE_URL=<Che7 URL>`
- `npm ci`

Note: If there is any modifications in package.json, manually execute the `npm install` to update the package-lock.json. So that errors can be avoided while executing npm ci

## Default launch

- Provide connection credentials:
    - `export TS_SELENIUM_OCP_USERNAME=<username>`
    - `export TS_SELENIUM_OCP_PASSWORD=<password>`
    - `npm run test`

## Custom launch

- Use environment variables which described in the "constants" folder
- Use environment variables for setting timeouts if needed. You can see the list in **`'TimeoutConstants.ts'`**. You can see the list of those variables and their value if you set the `'TS_SELENIUM_PRINT_TIMEOUT_VARIABLES = true'`
- To test one specification export file name as `export USERSTORY=<spec-file-name-without-extension> && npm run test` (example: `-e USERSTORY=Quarkus`)
- To run test without Selenium WebDriver (API tests etc.) use `export USERSTORY=<spec-file-name-without-extension> && npm run driver-less-test` (example: `-e USERSTORY=CloneGitRepoAPI`)
- This project support application testing deployed on Kubernetes or Openshift platform. Openshift is default value. To switch into Kubernetes, please, use `TS_PLATFORM=kubernetes` environmental variable and `TS_SELENIUM_K8S_PASSWORD`, `TS_SELENIUM_K8S_USERNAME` to provide credentials. The sample of test command in this case:
    ```
    export TS_PLATFORM=kubernetes && \
    export TS_SELENIUM_K8S_USERNAME=<username> && \
    export TS_SELENIUM_K8S_PASSWORD=<password> && \
    export TS_SELENIUM_BASE_URL=<ingress-url> && \
    npm run test
    ```
    Also, environmental variables can be set in files in "constants" folder.
- Local test results can be represented with Allure reporter `npm run open-allure-dasboard`

## Docker launch

- open terminal and go to the "e2e" directory
- export the `"TS_SELENIUM_BASE_URL"` variable with "Che" url
- run command `"npm run test-docker"`

## Docker launch with changed tests

**For launching tests with local changes perform next steps:**

- open terminal and go to the "e2e" directory
- export the `"TS_SELENIUM_BASE_URL"` variable with "Che" url
- run command `"npm run test-docker-mount-e2e"`

## Debug docker launch

The `'quay.io/eclipse/che-e2e'` docker image has VNC server installed inside. For connecting use `'0.0.0.0:5920'` address.

## The "Happy Path" scenario launching

**The easiest way to do that is to perform steps which are described in the "Docker launch" paragraph. For running tests without docker, please perform next steps:**

- Deploy Che on Kubernetes infrastructure by using 'Minikube' and 'Chectl' <https://github.com/eclipse-che/che-server/blob/HEAD/deploy/kubernetes/README.md>
- Create workspace by using 'Chectl' and devfile
    - link to 'Chectl' manual <https://github.com/che-incubator/chectl#chectl-workspacestart>
    - link to devfile ( **`For successfull test passing, exactly provided devfile should be used`** ) <https://gist.githubusercontent.com/Ohrimenko1988/93f5426f4ebc1705c55feb8ff0396a49/raw/cbea89ad145ba33ed34a151a12c50f045f9f3b78/yaml-ls-bug.yaml>
- Provide the **`'TS_SELENIUM_BASE_URL'`** environment variable as described above
- export TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME=EmptyWorkspace (default value, see BASE_TEST_CONSTANTS.ts)
- perform command **`export USERSTORY=$TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME && npm run test-all-devfiles`**

## Launching the DevWorkspaceHappyPath spec file using Che with oauth authentication

**Setup next environment variables:**

- export TS_SELENIUM_BASE_URL=\<Che-URL\>
- export TS_SELENIUM_OCP_USERNAME=\<cluster-username\>
- export TS_SELENIUM_OCP_PASSWORD=\<cluster-password\>
- export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH="true"
- export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=\<login-provide-title\>
- export TS_SELENIUM_DEVWORKSPACE_URL=\<devworkspace-url\>
- export TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME=EmptyWorkspace (default value, see BASE_TEST_CONSTANTS.ts)

**Execute the npm command:**

- perform command `export USERSTORY=$TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME && npm run test-all-devfiles`
