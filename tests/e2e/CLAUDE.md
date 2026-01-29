# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Available commands:**
- `/e2e-test-development` - Comprehensive E2E test development guidance (code style, patterns, architecture)

**Eclipse Che:**
- `/run-che-e2e-test-using-podman` - Run E2E tests using che-e2e container with Podman
- `/run-che-e2e-test-locally` - Run E2E tests using local Chrome browser

**Red Hat OpenShift Dev Spaces:**
- `/run-devspaces-e2e-test-using-podman` - Run E2E tests against Dev Spaces using Podman container
- `/run-devspaces-e2e-test-locally` - Run E2E tests against Dev Spaces using local browser

## Overview

This is the E2E test suite for Eclipse Che / Red Hat OpenShift Dev Spaces. It uses Selenium WebDriver with Chrome, Mocha (TDD style), and TypeScript to test workspace creation, IDE functionality, and platform integrations.

## Quick Reference

### Essential Commands

```bash
npm ci                                    # Install dependencies
npm run test                              # Run all tests
npm run lint                              # Fix linting issues
npm run prettier                          # Fix formatting
npm run tsc                               # Compile TypeScript
export USERSTORY=TestName && npm run test # Run single test
```

### Key Files to Read

- `CODE_STYLE.md` - **CRITICAL**: Coding standards (read before writing any code)
- `README.md` - Setup and launch instructions
- `configs/inversify.config.ts` - Dependency injection container
- `configs/inversify.types.ts` - Class and type identifiers
- `constants/TIMEOUT_CONSTANTS.ts` - Timeout values for waits

## Code Style Requirements (CRITICAL)

**Always read `CODE_STYLE.md` before modifying code.**

### Required File Header
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

### Page Object Pattern
```typescript
@injectable()
export class MyPage {
    private static readonly BUTTON: By = By.xpath('//button');

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

    async clickButton(): Promise<void> {
        Logger.debug();
        await this.driverHelper.waitAndClick(MyPage.BUTTON);
    }

    private getDynamicLocator(name: string): By {
        return By.xpath(`//div[text()="${name}"]`);
    }
}
```

### Test Structure (TDD Style - No Arrow Functions)
```typescript
suite('Suite Name', function (): void {
    const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

    suiteSetup('Setup', async function (): Promise<void> {
        // setup code
    });

    test('Test case', async function (): Promise<void> {
        // test code
    });

    suiteTeardown('Cleanup', async function (): Promise<void> {
        // cleanup code
    });
});
```

### Key Rules
- `@injectable()` on all page objects and utilities
- `Logger.debug()` at start of every public method
- Static locators: `private static readonly NAME: By`
- Dynamic locators: private methods returning `By`
- NO arrow functions in Mocha declarations
- Explicit return types on ALL functions
- Single quotes for strings
- Comments start lowercase: `// todo issue crw-1010`

## Directory Structure

| Directory | Purpose |
|-----------|---------|
| `specs/` | Test files (api/, factory/, miscellaneous/) |
| `pageobjects/` | Page Object classes |
| `utils/` | DriverHelper, Logger, API handlers |
| `tests-library/` | Reusable helpers (LoginTests, WorkspaceHandlingTests) |
| `constants/` | Environment variables and timeouts |
| `configs/` | Inversify DI, Mocha config |

## Adding New Components

### New Page Object
1. Create in `pageobjects/<category>/NewPage.ts`
2. Add to `inversify.types.ts`: `NewPage: 'NewPage'`
3. Add to `inversify.config.ts`: `e2eContainer.bind<NewPage>(CLASSES.NewPage).to(NewPage)`

### New Test
1. Create `specs/<category>/TestName.spec.ts`
2. Run: `export USERSTORY=TestName && npm run test`

## GitHub Actions

PR checks run on changes to `tests/e2e/**`:
1. Prettier formatting check
2. TypeScript compilation
3. ESLint linting
4. API and UI tests on minikube

**Before pushing**: Run `npm run prettier && npm run tsc && npm run lint`

## Environment Variables

| Variable | Description |
|----------|-------------|
| `TS_SELENIUM_BASE_URL` | Che dashboard URL |
| `TS_SELENIUM_OCP_USERNAME` | OpenShift username |
| `TS_SELENIUM_OCP_PASSWORD` | OpenShift password |
| `USERSTORY` | Test file to run (without .spec.ts) |
| `TS_PLATFORM` | `openshift` or `kubernetes` |
