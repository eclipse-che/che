# Coding Standards and Conventions

### Introducing

#### Why are coding standards important?

Coding standards offer several advantages, including:

1. Increase Code Quality: By adhering to coding standards, developers can create code that is more secure, efficient,
   maintainable, and uniform. This, in turn, can result in fewer errors and improved overall performance.

2. Improved Readability and Maintainability: Coding standards contribute to code that is more comprehensible and easier
   to maintain. Consistently formatted code aids other developers in comprehending and modifying it, saving time and
   reducing the likelihood of introducing errors.

3. Accelerated Development: The adherence to coding standards can expedite the development process. When developers
   adhere to a predefined set of guidelines, they can produce code more swiftly and with fewer mistakes. Additionally,
   uniform code formatting facilitates the identification and resolution of issues.

4. Better Scalability: Coding standards facilitate the creation of scalable code, simplifying the incorporation of new
   features or updates. Consistent coding practices also streamline code maintenance as the codebase expands.

5. Elevated Collaboration and Communication: Uniform guidelines encourage better understanding and manipulation of code
   written by fellow developers. This fosters smoother teamwork and facilitates the sharing of code.

6. Consistency Across Projects: The adoption of coding standards guarantees a consistent coding approach across various
   projects. This simplifies the task of upholding code quality, transitioning between tasks, and fostering
   collaborative work.

### Automated tools

Automated lint checking and code format performs with ESLint and Prettier tools before every commit using Husky
pre-commit hook.
Full set of rules can be found:

-   [.eslintrc](.eslintrc.js)
-   [.prettierrc](.prettierrc.json)

### Preferable code style

1. Page-object and util classes

    1. ✔ Class declaration using dependency injection (inversify library)

        ```
          @injectable()
          export class BrowserTabsUtil {
              constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }
        ```

    2. Public methods

        - ✔ Declare public methods without "public "keyword
        - ✔ Add Logger.debug() inside method to log its name (with optional message)

        ```
         async switchToWindow(windowHandle: string): Promise<void> {
             Logger.debug(); // logs BrowserUtils.sswitchToWindow

             await this.driverHelper.getDriver().switchTo().window(windowHandle);
         }
        ```

    3. Locators

        - ✔ For static locators - private static readonly fields type of By

        ```
        private static readonly FACTORY_URL_LOCATOR: By = By.xpath('//input[@id="git-repo-url"]');
        ```

        - ✔ For dynamic locators - private methods which returns By

        ```
        private getExpandedActionsLocator(workspaceName: string): By {
            return By.xpath(`${this.getWorkspaceListItemLocator(workspaceName)}//button[@aria-label='Actions' and @aria-expanded='true']`);
        }
        ```

        - ✗ Avoid to declare locators as constant in methods

        ```
        async waitTitleContains(expectedText: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
             Logger.debug();

             const pageTitleLocator: By = By.xpath(`//h1[contains(text(), '${expectedText}')]`);
             await this.driverHelper.waitVisibility(pageTitleLocator, timeout);
         }
        ```

        #### Page object sample:

        ```
        import { e2eContainer } from '../../configs/inversify.config';

        @injectable()
        export class OcpMainPage {

            private static readonly MAIN_PAGE_HEADER_LOCATOR: By = By.id('page-main-header');
            private static readonly SELECT_ROLE_BUTTON_LOCATOR: By = By.xpath('//*[@data-test-id="perspective-switcher-toggle"]');

            constructor(
                @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

            async waitOpenMainPage(): Promise<void> {
                Logger.debug();

                await this.driverHelper.waitVisibility(OcpMainPage.MAIN_PAGE_HEADER_LOCATOR, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
            }

            private getProjectDropdownItemLocator(projectName: string): By {
                return By.xpath(`//button//*[text()="${projectName}"]`);
            }
        }

        ```

2. Mocha framework

    - ✔ TDD framework (`suit()`, `test()`)
    - ✔ Inject class instances, declare all test data inside test `suit()` function to avoid unnecessary code execution if test suit will not be run

    ```
    suite('name', function(): void {
        const webCheCodeLocators: Locators = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
        // test specific data
        const gitImportReference: string = 'pipeline';
    ```

    - ✗ Don`t use arrow functions in test declaration https://mochajs.org/#arrow-functions
    - ✔ Specs which don`t use browser should have API in name ending (like EmptyWorkspaceAPI.spec)
    - ✗ Don\`t create scripts in package.json for each test. Instead of it use [dynamic config](configs/mocharc.ts), `mocha --spec` or `mocha --grep` flags to run specific test.
    - ✔ Use test [./constants](constants) to make test flexible

3. Packages

    1. Add packages as dev dependencies
    2. If any changes re-create package-lock.json before push

4. Comments
    1. If some code commented or added as workaround mark it as `//todo` with number of issue to get possibility to find it quickly
    ```
    // todo commented due to issue crw-1010
    ```
