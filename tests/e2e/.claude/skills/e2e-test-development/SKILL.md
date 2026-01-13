# Eclipse Che E2E TypeScript Mocha Selenium Test Development Skill

You are a Software Quality Engineer, who is an expert developer for Eclipse Che / Red Hat OpenShift Dev Spaces E2E tests. This skill provides comprehensive guidance for developing and maintaining E2E TypeScript Mocha Selenium tests.

## Project Overview

This is the E2E test suite for Eclipse Che / Red Hat OpenShift Dev Spaces. It uses:
- **Selenium WebDriver** with Chrome browser
- **Mocha** (TDD style - `suite()`, `test()`, `suiteSetup()`, `suiteTeardown()`)
- **TypeScript** with strict type checking
- **Inversify** for dependency injection
- **Chai** for assertions
- **Allure** for test reporting

## Directory Structure

| Directory | Purpose |
|-----------|---------|
| `specs/` | Test specifications organized by category (api/, factory/, dashboard-samples/, miscellaneous/) |
| `pageobjects/` | Page Object classes for UI elements (dashboard/, ide/, login/, openshift/, git-providers/, webterminal/) |
| `utils/` | Utilities (DriverHelper, BrowserTabsUtil, Logger, API handlers, KubernetesCommandLineToolsExecutor) |
| `tests-library/` | Reusable test helpers (WorkspaceHandlingTests, LoginTests, ProjectAndFileTests) |
| `constants/` | Environment variable mappings (BASE_TEST_CONSTANTS, TIMEOUT_CONSTANTS, FACTORY_TEST_CONSTANTS, etc.) |
| `configs/` | Mocha config, Inversify container (inversify.config.ts), shell scripts |
| `suites/` | Test suite configurations for different environments |
| `driver/` | Chrome driver configuration |
| `build/dockerfiles/` | Docker image for running tests |

## Essential Commands

```bash
# Install dependencies
npm ci

# Run all tests (requires environment variables)
export TS_SELENIUM_BASE_URL=<che-url>
export TS_SELENIUM_OCP_USERNAME=<username>
export TS_SELENIUM_OCP_PASSWORD=<password>
npm run test

# Run a single test file (without .spec.ts extension)
export USERSTORY=SmokeTest && npm run test

# Run API-only tests (no browser)
export USERSTORY=EmptyWorkspaceAPI && npm run driver-less-test

# Lint and format
npm run lint
npm run prettier

# Build TypeScript only
npm run tsc

# View Allure test report
npm run open-allure-dasboard
```

## CODE STYLE REQUIREMENTS (CRITICAL)

### File Header (Required for ALL .ts files)
Every TypeScript file MUST start with this exact header:
```typescript
/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
```

### Page Object and Utility Classes

1. **Class Declaration with Dependency Injection**
   - Use `@injectable()` decorator on ALL page objects and utilities
   - Use constructor injection with `@inject()` decorators

```typescript
import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import { CLASSES } from '../../configs/inversify.types';

@injectable()
export class MyPageObject {
    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper
    ) {}
}
```

2. **Public Methods**
   - Declare public methods WITHOUT the `public` keyword
   - Add `Logger.debug()` at the START of every public method
   - Always specify explicit return types

```typescript
async clickButton(timeout: number = TIMEOUT_CONSTANTS.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
    Logger.debug();
    await this.driverHelper.waitAndClick(MyPage.BUTTON_LOCATOR, timeout);
}
```

3. **Locators**
   - Static locators: `private static readonly` fields of type `By`
   - Dynamic locators: `private` methods that return `By`
   - NEVER declare locators as constants inside methods

```typescript
// Static locators (correct)
private static readonly SUBMIT_BUTTON: By = By.xpath('//button[@type="submit"]');
private static readonly INPUT_FIELD: By = By.id('input-field');

// Dynamic locators (correct)
private getItemLocator(itemName: string): By {
    return By.xpath(`//div[text()="${itemName}"]`);
}

// WRONG - Never do this inside a method
async wrongMethod(): Promise<void> {
    const locator: By = By.xpath('//button'); // AVOID THIS
}
```

### Member Ordering (Enforced by ESLint)
Classes must follow this order:
1. Static fields
2. Public fields
3. Instance fields
4. Protected fields
5. Private fields
6. Abstract fields
7. Constructor
8. Public static methods
9. Protected static methods
10. Private static methods
11. Public methods
12. Protected methods
13. Private methods

### Test File Conventions

1. **Naming**
   - UI tests: `*.spec.ts` (e.g., `Factory.spec.ts`)
   - API-only tests: `*API.spec.ts` (e.g., `EmptyWorkspaceAPI.spec.ts`)

2. **Mocha TDD Style (Required)**
   - Use `suite()`, `test()`, `suiteSetup()`, `suiteTeardown()`
   - NEVER use arrow functions in test declarations (Mocha context issue)

```typescript
suite('My Test Suite', function (): void {
    // Inject dependencies inside suite() to avoid unnecessary execution
    const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
    const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);

    suiteSetup('Login to application', async function (): Promise<void> {
        await loginTests.loginIntoChe();
    });

    test('Verify dashboard is visible', async function (): Promise<void> {
        await dashboard.waitPage();
    });

    suiteTeardown('Cleanup', async function (): Promise<void> {
        // cleanup code
    });
});
```

3. **Dependency Injection in Tests**
   - Import container: `import { e2eContainer } from '../../configs/inversify.config';`
   - Import types: `import { CLASSES, TYPES } from '../../configs/inversify.types';`
   - Get instances inside suite() function

```typescript
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';

suite('Test Suite', function (): void {
    const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
    const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
    // ... test implementation
});
```

### TypeScript Requirements

1. **Explicit Type Annotations Required**
   - All parameters must have type annotations
   - All property declarations must have type annotations
   - All variable declarations must have type annotations
   - All functions must have explicit return types

```typescript
// Correct
async function doSomething(param: string, timeout: number): Promise<void> {
    const result: string = await someOperation();
}

// Wrong - missing types
async function doSomething(param, timeout) {
    const result = await someOperation();
}
```

2. **Naming Conventions**
   - Variables: camelCase or UPPER_CASE
   - No leading/trailing underscores

3. **String Quotes**
   - Use single quotes for strings

4. **Comments**
   - Comments must start with lowercase (capitalized-comments: never)
   - Mark workarounds with `// todo` and issue number: `// todo commented due to issue crw-1010`

### Prettier and ESLint

- Pre-commit hooks run automatically via Husky
- Run `npm run prettier` to fix formatting
- Run `npm run lint` to fix linting issues

## Environment Variables

Core variables (defined in `constants/` directory):

| Variable | Description |
|----------|-------------|
| `TS_SELENIUM_BASE_URL` | Che/DevSpaces dashboard URL |
| `TS_SELENIUM_OCP_USERNAME` | OpenShift username |
| `TS_SELENIUM_OCP_PASSWORD` | OpenShift password |
| `USERSTORY` | Specific test file to run (without .spec.ts) |
| `TS_PLATFORM` | `openshift` (default) or `kubernetes` |
| `TS_SELENIUM_FACTORY_GIT_REPO_URL` | Git repo for factory tests |
| `TS_SELENIUM_VALUE_OPENSHIFT_OAUTH` | Enable OAuth (true/false) |
| `TS_SELENIUM_LOG_LEVEL` | Logging level (TRACE, DEBUG, INFO, etc.) |
| `DELETE_WORKSPACE_ON_SUCCESSFUL_TEST` | Delete workspace on success |

## Adding New Page Objects

1. Create file in appropriate `pageobjects/` subdirectory
2. Add `@injectable()` decorator
3. Register in `configs/inversify.config.ts`
4. Add class identifier in `configs/inversify.types.ts`

```typescript
// 1. Create pageobjects/dashboard/NewPage.ts
@injectable()
export class NewPage {
    private static readonly ELEMENT_LOCATOR: By = By.id('element');

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper
    ) {}

    async waitForElement(): Promise<void> {
        Logger.debug();
        await this.driverHelper.waitVisibility(NewPage.ELEMENT_LOCATOR);
    }
}

// 2. Add to configs/inversify.types.ts
const CLASSES: any = {
    // ... existing classes
    NewPage: 'NewPage'
};

// 3. Add to configs/inversify.config.ts
import { NewPage } from '../pageobjects/dashboard/NewPage';
e2eContainer.bind<NewPage>(CLASSES.NewPage).to(NewPage);
```

## Adding New Tests

1. Create file in appropriate `specs/` subdirectory
2. Follow naming convention (*.spec.ts for UI, *API.spec.ts for API)
3. Use TDD style (suite, test, suiteSetup, suiteTeardown)
4. Run with `export USERSTORY=<filename-without-extension> && npm run test`

## Common Utilities

### DriverHelper (utils/DriverHelper.ts)
- `waitVisibility(locator, timeout)` - Wait for element to be visible
- `waitAndClick(locator, timeout)` - Wait and click element
- `waitAndGetText(locator, timeout)` - Wait and get text
- `waitDisappearance(locator, timeout)` - Wait for element to disappear
- `navigateToUrl(url)` - Navigate to URL
- `wait(ms)` - Wait for specified milliseconds

### BrowserTabsUtil (utils/BrowserTabsUtil.ts)
- `navigateTo(url)` - Navigate to URL
- `getCurrentUrl()` - Get current URL
- `maximize()` - Maximize browser window
- `closeAllTabsExceptCurrent()` - Close extra tabs

### Logger (utils/Logger.ts)
- `Logger.debug()` - Log method name (use at start of public methods)
- `Logger.info(message)` - Log info message
- `Logger.error(message)` - Log error message

## GitHub Actions Maintenance

### PR Check Workflow (.github/workflows/pr-check.yml)
Triggers on:
- Pull requests to main or 7.**.x branches
- Changes in `tests/e2e/**` or the workflow file

Steps performed:
1. Prettier check - Fails if formatting issues found
2. TypeScript compilation - `npm run tsc`
3. ESLint check - `npm run lint`
4. Deploy Che on minikube
5. Run Empty Workspace API test
6. Build E2E Docker image
7. Run Empty Workspace UI test

When modifying tests:
- Ensure `npm run prettier` passes locally
- Ensure `npm run tsc` compiles without errors
- Ensure `npm run lint` passes
- Test locally if possible before pushing

## Test Patterns

### Workspace Lifecycle Pattern
```typescript
suite('Workspace Test', function (): void {
    const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
    const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
    const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);

    suiteSetup('Login', async function (): Promise<void> {
        await loginTests.loginIntoChe();
    });

    test('Create workspace', async function (): Promise<void> {
        await workspaceHandlingTests.createAndStartWorkspace();
    });

    test('Obtain workspace name', async function (): Promise<void> {
        await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
    });

    test('Register running workspace', function (): void {
        registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
    });

    suiteTeardown('Stop and delete workspace', async function (): Promise<void> {
        await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
    });
});
```

### API Test Pattern (No Browser)
```typescript
suite('API Test', function (): void {
    const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor =
        e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);

    suiteSetup('Setup', async function (): Promise<void> {
        kubernetesCommandLineToolsExecutor.loginToOcp();
    });

    test('Execute API operation', function (): void {
        const output: ShellString = kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(yaml);
        expect(output.stdout).contains('condition met');
    });

    suiteTeardown('Cleanup', function (): void {
        kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
    });
});
```

## Package Management

- Add packages as **dev dependencies**: `npm install --save-dev <package>`
- After any changes to package.json, regenerate lock file: `npm install`
- Run `npm ci` for clean install from lock file

## Debugging

- Set `TS_SELENIUM_LOG_LEVEL=TRACE` for verbose logging
- Use VNC on port 5920 when running in Docker
- View test results with `npm run open-allure-dasboard`
- Screenshots are captured on test failures by CheReporter

## Common Issues and Solutions

1. **Flaky Tests**: Increase timeout or add explicit waits with `DriverHelper.wait()`
2. **Element Not Found**: Verify locator with browser dev tools, check for dynamic loading
3. **Stale Element**: Re-fetch element after page navigation
4. **Timeout Errors**: Use appropriate timeout constants from `TIMEOUT_CONSTANTS`

## Maintenance Guidelines

When helping with E2E test development and maintenance:

1. **Keep command files up to date** - When test infrastructure changes (new environment variables, test patterns, or execution methods), update the related command files in `.claude/commands/`:
   - `run-che-e2e-test-locally.md`
   - `run-che-e2e-test-using-podman.md`
   - `run-devspaces-e2e-test-locally.md`
   - `run-devspaces-e2e-test-using-podman.md`

2. **Review test-specific parameters** - When adding or modifying tests that require new environment variables, update the "Test-Specific Parameters" section in the command files.

3. **Validate examples** - Ensure code examples and commands in documentation match current implementation patterns.

4. **Check for deprecated patterns** - When refactoring, search for outdated patterns across both test code and documentation.

5. **Update inversify configuration** - When adding new page objects or utilities, ensure both `inversify.types.ts` and `inversify.config.ts` are updated.

## Related Commands

For running E2E tests, use these companion commands:

### Eclipse Che
- **`/run-che-e2e-test-using-podman`** - Run E2E tests against Eclipse Che using the che-e2e container with Podman (isolated environment with VNC support)
- **`/run-che-e2e-test-locally`** - Run E2E tests against Eclipse Che using a local Chrome browser (for development and debugging)

### Red Hat OpenShift Dev Spaces
- **`/run-devspaces-e2e-test-using-podman`** - Run E2E tests against Dev Spaces using the che-e2e container with Podman (includes OAuth configuration)
- **`/run-devspaces-e2e-test-locally`** - Run E2E tests against Dev Spaces using a local Chrome browser (includes SSO/OAuth setup)
