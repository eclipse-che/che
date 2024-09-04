# Test Scenario for Web Terminal in OpenShift

### Prerequisites:
* Test Environment: Node.js + Webdriver + Mocha framework.
* Automation Requirement: A browser for interacting with the OpenShift Web Console.
* Permissions: Admin access to the test cluster.
* Setup: Ensure the DevWorkspace Operator is preinstalled before executing the tests.
## Scenario for WebTerminal under Admin user
#### Note! This scenario also is used for interop testing
1. Login as Admin:
*  Login as admin user into OpenShift Web Console and refresh the page
2. Clean Up Existing Resources:
* Remove any existing DevWorkspace custom resources (`oc delete dw --all -n  <dedicated_namespace>`)
3. Launch WebTerminal:
*  Click on the "WebTerminal" icon
* Wait for the WebTerminal widget to appear
4. Create a WebTerminal Session:
* Click the "Create" button.
* Wait for the WebTerminal xterm text area to appear.

5. Verify Admin Session:
* Execute the command 'oc whoami' and verify that the session is running under the admin user.
* Since the terminal is canvas-based and difficult to read with WebDriver, read the DevWorkspace object, parse the YAML, and extract the username.
6. Execute 'help' Command:
* Run the 'help' command in the Terminal, redirect the output to a dedicated container, and verify the result.
* Ensure all tools are present, and the 'help' command executes successfully.
 7. Set Inactivity Timeout:
* Configure the Terminal to have a 30-second inactivity timeout.
* Verify that the Terminal closes after 30 seconds of inactivity.

--- 

## Scenario for the Web Terminal under regular user
1. Login as Regular User:
* Log in to the OpenShift Web Console as a regular user and refresh the page.
* If a test project exists, delete it.
2. Clean Up Existing Resources:
* Remove any existing DevWorkspace custom resources (`oc delete dw --all -n  <dedicated_namespace>`)
3. Launch WebTerminal:
* Click on the "WebTerminal" icon.
* Wait for the widget to appear for the regular user.
* In the project creation field, type a new namespace for the user.
4. Create a WebTerminal Session:
* Click the "Create" button.
* Wait for the WebTerminal xterm text area to appear.
5. Verify Regular User Session:
* Execute the command 'oc whoami' and verify that the session is running under the regular user.
6. Execute 'help' Command:
* Run the 'help' command in the Terminal, redirect the output to a dedicated container, and verify the result.
* Ensure all tools are present, and the 'help' command executes successfully.
7. Set Inactivity Timeout:
* Configure the Terminal to have a 30-second inactivity timeout.
* Verify that the Terminal closes after 30 seconds of inactivity.