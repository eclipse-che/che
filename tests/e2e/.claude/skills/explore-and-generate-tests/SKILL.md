---
name: explore-and-generate-tests
description: Explore a web page with new functionality using Playwright, record UI interactions, develop a test plan, and generate E2E Mocha/Selenium test code following the project's code style and patterns.
---

# Explore and Generate E2E Tests

You are a Software Quality Engineer exploring new functionality in Eclipse Che / Red Hat OpenShift Dev Spaces. Your job is to explore a web page, record what you find, develop a test plan, and write production-ready E2E test code.

## Prerequisites

- Playwright MCP server must be configured (see `.mcp.json` in this repo)
- Read `CODE_STYLE.md` before generating any code

## Input Parameters

The user provides:

- **page** - URL or route to explore (e.g., `/#/create-workspace`, `/#/user-preferences`, a full URL)
- **scope** - (optional) What to focus on: a specific feature, button, form, or "everything"
- **test-cases-repo** - (optional) Path to the test-cases repo for generating Polarion test case markdown (default: `/home/ndp/projects/cci/test-cases`)

## Workflow

### Phase 0: Authenticate

Before exploring, ensure you have an authenticated browser session.

1. **Determine the target URL:**

    - If the user provided a full URL, extract the base URL
    - If the user provided only a route (e.g., `/#/create-workspace`), ask for the base URL

2. **Ask for credentials** (if not already provided):

    - Use AskUserQuestion to collect:
        - **URL** (if not provided) — Dev Spaces or Eclipse Che base URL
        - **Platform** — `openshift` or `kubernetes` (auto-detect from URL: `devspaces` in URL → openshift; `che` on localhost/minikube → kubernetes)
        - **Username** — login username
        - **Password** — login password
        - **Identity Provider** — (OpenShift only, default: `htpasswd`)

3. **Login via Playwright MCP:**

    Navigate to the base URL using `browser_navigate`, then take a `browser_snapshot` to see the login page state. The login flow depends on the platform:

    #### OpenShift (Dev Spaces / Eclipse Che on OpenShift)

    - Click the identity provider link (e.g., "htpasswd") using `browser_click`
    - Take a `browser_snapshot` to see the login form
    - Type username into the username field using `browser_type`
    - Type password into the password field using `browser_type`
    - Click the "Log in" button using `browser_click`
    - If an authorization page appears ("Allow selected permissions"), click to approve using `browser_click`

    #### Kubernetes / Minikube (Eclipse Che with Dex)

    - The login page is a Dex login form — take a `browser_snapshot` to confirm
    - Click "Log in with Email" if multiple login options are shown
    - Type email/username into the email field using `browser_type`
    - Type password into the password field using `browser_type`
    - Click the "Login" button using `browser_click`
    - If a grant access page appears, click "Grant Access" using `browser_click`

    #### Common post-login steps

    - Take a `browser_snapshot` to confirm the dashboard loaded successfully
    - If login fails, report the error and stop

4. **Set environment context** for later use in generated test code:
    - Remember the base URL as `TS_SELENIUM_BASE_URL`
    - Remember the platform as `TS_PLATFORM` (`openshift` or `kubernetes`)
    - For OpenShift: remember username as `TS_SELENIUM_OCP_USERNAME`, identity provider as `TS_OCP_LOGIN_PAGE_PROVIDER_TITLE`
    - For Kubernetes: remember username as `TS_SELENIUM_K8S_USERNAME`

### Phase 1: Explore the Page

Use Playwright MCP tools to systematically explore the target page.

1. **Navigate** to the target page using `browser_navigate`
2. **Take a snapshot** using `browser_snapshot` to capture the full accessibility tree
3. **Identify all interactive elements**: buttons, links, forms, dropdowns, toggles, tabs, modals
4. **Explore each interactive element**:
    - Click tabs/navigation items to discover sub-pages
    - Open dropdowns to see options
    - Hover over elements to discover tooltips or hidden UI
    - Check for conditional UI (elements that appear/disappear based on state)
5. **Document the page structure** as you go:
    - Page layout and sections
    - Navigation flows (what leads where)
    - Forms and their fields (required vs optional, validation rules)
    - Action buttons and their effects
    - Error states and edge cases visible in the UI

**Important:**

- Use `browser_snapshot` (accessibility tree) as the primary exploration method
- Use `browser_click`, `browser_fill_form`, `browser_hover` to interact with elements
- Use `browser_wait_for` when pages need time to load
- Take snapshots AFTER each interaction to see the resulting state
- Do NOT use screenshots — use snapshots for structured data

### Phase 2: Record the Interactions

After exploring, produce a structured record of the functionality:

```markdown
## Page: [Page Name]

URL: [full URL or route]

### UI Elements

| Element                 | Type       | Locator Strategy                                | Behavior                      |
| ----------------------- | ---------- | ----------------------------------------------- | ----------------------------- |
| Create Workspace button | button     | By.xpath('//button[text()="Create Workspace"]') | Opens workspace creation form |
| Git Repo URL input      | text input | By.id('git-repo-url')                           | Accepts git repository URL    |
| ...                     | ...        | ...                                             | ...                           |

### User Workflows

1. **[Workflow Name]** (e.g., "Create workspace from git repo")

    - Step 1: [action] → [expected result]
    - Step 2: [action] → [expected result]
    - ...

2. **[Workflow Name]** (e.g., "Cancel workspace creation")
    - Step 1: ...

### Edge Cases & Error States

- [What happens when invalid input is provided]
- [What happens when network is slow]
- [Empty states, loading states]
```

### Phase 3: Develop Test Plan

Based on the recorded interactions, create a comprehensive test plan:

1. **Categorize test scenarios** by priority:

    - **P1 (High)**: Core happy-path workflows that must work
    - **P2 (Medium)**: Alternative flows, input validation, error handling
    - **P3 (Low)**: Edge cases, cosmetic checks, accessibility

2. **Define test cases** using this structure:

    ```
    Test ID: [CATEGORY_LETTER][NUMBER]
    Title: [descriptive title]
    Type: positive | negative
    Priority: high | medium | low
    Preconditions: [what must be true before the test]
    Steps:
      1. [action] → Expected: [result]
      2. [action] → Expected: [result]
    ```

3. **Group tests into suites** by feature area or workflow

4. **Present the test plan to the user** and ask for approval before proceeding to code generation. The user may want to:
    - Add or remove test cases
    - Change priorities
    - Modify the scope
    - Split into multiple specs

### Phase 4: Generate E2E Test Code

After the user approves the test plan, generate production-ready test code.

#### Step 4.1: Read Required Reference Files

Before writing any code, read these files to understand current patterns:

```
CODE_STYLE.md                           # Coding standards
configs/inversify.types.ts              # Existing class registry
configs/inversify.config.ts             # Existing DI bindings
constants/TIMEOUT_CONSTANTS.ts          # Available timeout constants
```

Also read 1-2 existing specs and page objects similar to what you're generating, to match the exact style.

#### Step 4.2: Generate Page Objects (if needed)

For each new page or component that needs interaction, create a page object:

**File:** `pageobjects/<category>/NewPage.ts`

```typescript
/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import { By } from 'selenium-webdriver';
import { CLASSES } from '../../configs/inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class NewPage {
	private static readonly ELEMENT_LOCATOR: By = By.xpath('//...');

	constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

	async waitPage(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
		Logger.debug();
		await this.driverHelper.waitVisibility(NewPage.ELEMENT_LOCATOR, timeout);
	}

	private getDynamicLocator(name: string): By {
		return By.xpath(`//div[text()="${name}"]`);
	}
}
```

**Rules for page objects:**

- `@injectable()` decorator on the class
- `Logger.debug()` at the start of every public method
- Static locators as `private static readonly NAME: By`
- Dynamic locators as `private` methods returning `By`
- Constructor injection with `@inject()` decorators
- Explicit return types on ALL methods
- NO `public` keyword on public methods
- Use XPath as primary locator strategy (matching existing patterns)
- Use timeout constants from `TIMEOUT_CONSTANTS`, not hardcoded values

#### Step 4.3: Generate Test Spec

**File:** `specs/<category>/NewFeature.spec.ts`

```typescript
/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { LoginTests } from '../../tests-library/LoginTests';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
// ... other imports

suite(`New Feature ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	// ... other DI injections

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Test case description', async function (): Promise<void> {
		// test implementation
	});

	suiteTeardown('Cleanup', async function (): Promise<void> {
		// cleanup code
	});
});
```

**Rules for test specs:**

- Mocha TDD style: `suite()`, `test()`, `suiteSetup()`, `suiteTeardown()`
- NEVER use arrow functions in Mocha declarations
- Get DI instances inside `suite()` function body
- Always include login in `suiteSetup`
- Always include cleanup in `suiteTeardown`
- Use `function (): void` (not arrow functions) for all callbacks

#### Step 4.4: Register New Components

If you created new page objects, update:

1. **`configs/inversify.types.ts`** - Add class name to `CLASSES` object
2. **`configs/inversify.config.ts`** - Add import and `e2eContainer.bind<>()` call

#### Step 4.5: Generate Polarion Test Case Markdown (optional)

If the user wants test cases synced to Polarion, also generate markdown files in the test-cases repo:

**File:** `<test-cases-repo>/tests/<functional|operational>/<category>/<id>-<title>.md`

```yaml
---
arch:
    - value: x86_64
      assignee: <ask user>
caseposneg: positive | negative
caseautomation: automated
caseimportance: high | medium | low
customerscenario: false
estimate: 1h
---
# <ID> - <Test Title>

## Description
- Test script: <link to the generated spec file in GitHub>
```

Use the test-cases repo's `npm run rename` to validate filenames after generating.

### Phase 5: Validate

After generating all code:

1. **Run TypeScript compilation**: `npm run tsc` to catch type errors
2. **Run linter**: `npm run lint` to catch style violations
3. **Run prettier**: `npm run prettier` to fix formatting
4. **Review the generated code** against `CODE_STYLE.md` rules
5. **Present a summary** to the user:
    - List of files created/modified
    - Test cases and their priorities
    - Any manual steps needed (e.g., running the tests, updating Polarion)

## Locator Strategy Guide

When translating Playwright MCP snapshot elements to Selenium locators:

| Playwright Snapshot            | Selenium Locator                                          |
| ------------------------------ | --------------------------------------------------------- |
| `role="button" name="Create"`  | `By.xpath('//button[text()="Create"]')`                   |
| `role="textbox" name="URL"`    | `By.xpath('//input[@aria-label="URL"]')`                  |
| `role="link" name="Settings"`  | `By.xpath('//a[text()="Settings"]')`                      |
| `role="tab" name="Git Repo"`   | `By.xpath('//button[@role="tab" and text()="Git Repo"]')` |
| `data-testid="workspace-name"` | `By.css('[data-testid="workspace-name"]')`                |
| `id="input-field"`             | `By.id('input-field')`                                    |

**Prefer this locator priority:**

1. `data-testid` attributes (most stable)
2. `id` attributes
3. XPath with text content (for buttons, links)
4. XPath with `aria-label` (for inputs, form fields)
5. XPath with structural path (last resort — fragile)

## Example Invocations

```
explore and generate tests for /#/create-workspace
explore /#/user-preferences and write E2E tests for the settings page
generate tests for the new workspace sharing feature at /#/workspaces
```
