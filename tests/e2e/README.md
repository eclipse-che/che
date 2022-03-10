
# Module for launch E2E tests related to Che 7

## Requirements

- node 8.x
- "Chrome" browser 69.x or later
- deployed Che 7 with accessible URL

## Before launch

**Perform commands:**

- ```export TS_SELENIUM_BASE_URL=<Che7 URL>```
- ```npm ci```

Note: If there is any modifications in package.json, manually execute the `npm install` to update the package-lock.json. So that errors can be avoided while executing npm ci

## Default launch

- Provide connection credentials to Che:
  - ```export TS_SELENIUM_USERNAME=<username>```
  - ```export TS_SELENIUM_PASSWORD=<password>```
- ```npm test```

## Custom launch

- Use environment variables which described in the **```'TestConstants.ts'```** file
- Use environment variables for setting timeouts if needed. You can see the list in **```'TimeoutConstants.ts'```**. You can see the list of those variables and their value if you set the ```'TS_SELENIUM_PRINT_TIMEOUT_VARIABLES = true'```
- Check for available scripts in package.json which are prefixed by `test-`. Then, perform command ```npm run test-<suffixNameOfScript>```

## Docker launch

- open terminal and go to the "e2e" directory
- export the ```"TS_SELENIUM_BASE_URL"``` variable with "Che" url
- run command ```"npm run test-docker"```

## Docker launch with changed tests

**For launching tests with local changes perform next steps:**

- open terminal and go to the "e2e" directory
- export the ```"TS_SELENIUM_BASE_URL"``` variable with "Che" url
- run command ```"npm run test-docker-mount-e2e"```

## Debug docker launch

The ```'eclipse/che-e2e'``` docker image has VNC server installed inside. For connecting use ```'0.0.0.0:5920'``` address.

## The "Happy Path" scenario launching

**The easiest way to do that is to perform steps which are described in the "Docker launch" paragraph.
For running tests without docker, please perform next steps:**

- Deploy Che on Kubernetes infrastructure by using 'Minikube' and 'Chectl' <https://github.com/eclipse-che/che-server/blob/HEAD/deploy/kubernetes/README.md>
- Create workspace by using 'Chectl' and devfile
  - link to 'Chectl' manual <https://github.com/che-incubator/chectl#chectl-workspacestart>
  - link to devfile ( **```For successfull test passing, exactly provided devfile should be used```** )
    <https://gist.githubusercontent.com/Ohrimenko1988/93f5426f4ebc1705c55feb8ff0396a49/raw/cbea89ad145ba33ed34a151a12c50f045f9f3b78/yaml-ls-bug.yaml>
- Provide the **```'TS_SELENIUM_BASE_URL'```** environment variable as described above
- perform command **```'npm run test-happy-path'```**

## Plugins tests launching for the Che without oauth authentication

**Setup next environment variables:**

- export TS_SELENIUM_BASE_URL=\<Che-URL\>
- export USERSTORY=\<Test you want to run \>
- (skip if login is "admin") export TS_SELENIUM_USERNAME=\<Login\>
- (skip if password is "admin") export TS_SELENIUM_PASSWORD=\<Password\>

**Run command:**

- npm run test-plugin

## Plugins tests launching for the Che with oauth authentication

**Setup next environment variables:**

- export TS_SELENIUM_BASE_URL=\<Che-URL\>
- export USERSTORY=\<test-you-want-to-run\>
- export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH="true"
- export TS_SELENIUM_OCP_USERNAME=\<cluster-username\>
- export TS_SELENIUM_OCP_PASSWORD=\<cluster-password\>

**Run command:**

- npm run test-plugin


## Launching the DevWorkspaceHappyPath spec file using Che with oauth authentication

**Setup next environment variables:**

- export TS_SELENIUM_BASE_URL=\<Che-URL\>
- export TS_SELENIUM_OCP_USERNAME=\<cluster-username\>
- export TS_SELENIUM_OCP_PASSWORD=\<cluster-password\>
- export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH="true"
- export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=\<login-provide-title\>
- export TS_SELENIUM_DEVWORKSPACE_URL=\<devworkspace-url\>

**Execute the npm command:**
- npm run test-devworkspace-happy-path
