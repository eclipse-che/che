# CLAUDE.md

## Project Overview

Eclipse Che is an open-source, Kubernetes-native developer workspace platform. This repository contains:

- **`tests/e2e/`** — TypeScript/Mocha/Selenium end-to-end test framework (the main codebase)
- **`tests/performance/`** — Shell-based performance tests
- **`tests/devworkspace-happy-path/`** — Happy-path smoke tests for DevWorkspaces

The e2e suite uses Inversify for dependency injection, Selenium WebDriver with Chrome, and Mocha in TDD style (`suite`/`test`).

## Local Dev Setup

All npm commands run from `tests/e2e/`, not the repo root:

```bash
cd tests/e2e
npm ci          # install dependencies
npm run tsc     # compile TypeScript
npm run lint    # run eslint with --fix
npm run prettier # format with prettier
```

## Running Tests

```bash
# full test suite (lint + compile + run)
npm run test

# run a single test file by name (without .spec.ts)
export USERSTORY=Factory && npm run test

# driver-less tests (no browser, API-only)
npm run driver-less-test
```

## Single-File Verification

All commands run from `tests/e2e/`:

```bash
# lint one file
npx eslint --fix specs/factory/Factory.spec.ts

# type-check (whole project, no single-file mode)
npm run tsc

# format one file
npx prettier --config .prettierrc.json --write specs/factory/Factory.spec.ts
```

## Pre-Push Checklist

Run before every push — CI checks these on PRs touching `tests/e2e/**`:

```bash
npm run prettier && npm run tsc && npm run lint
```

## Coding Conventions

- **License header**: EPL-2.0 block comment required on every `.ts` file (enforced by `eslint-plugin-header`)
- **Quotes**: single quotes (`'`), enforced by eslint
- **Return types**: explicit on all functions (`@typescript-eslint/explicit-function-return-type`)
- **Formatting**: prettier enforced — tabs, 140 print width, no trailing commas, LF line endings
- **Comments**: must start lowercase (`capitalized-comments: never`)
- **No arrow functions** in Mocha `suite`/`test`/`suiteSetup`/`suiteTeardown` declarations
- **`@injectable()`** decorator on all page objects and utility classes
- **`Logger.debug()`** at the start of every public method in page objects/utils
- **Locators**: static → `private static readonly NAME: By`; dynamic → private method returning `By`
- **Member ordering**: static fields, public fields, instance fields, constructor, then methods (public → private)

## Pattern References

Use these real files as examples when making common changes:

| Change | Example file |
|---|---|
| Add a new e2e test | `tests/e2e/specs/factory/Factory.spec.ts` |
| Add a new page object | `tests/e2e/pageobjects/dashboard/Dashboard.ts` |
| Add a new utility | `tests/e2e/utils/StringUtil.ts` |
| DI container registration | `tests/e2e/configs/inversify.config.ts` |
| DI type identifiers | `tests/e2e/configs/inversify.types.ts` |
| Timeout constants | `tests/e2e/constants/TIMEOUT_CONSTANTS.ts` |

## Key Configuration Files

| File | Purpose |
|---|---|
| `tests/e2e/tsconfig.json` | TypeScript config — target es2021, commonjs, strict, decorators enabled |
| `tests/e2e/.eslintrc.js` | ESLint rules — extends `@typescript-eslint/recommended-type-checked` + prettier |
| `tests/e2e/.prettierrc.json` | Prettier config — tabs, single quotes, 140 width, no trailing commas |
| `tests/e2e/package.json` | npm scripts and dependencies |

## Environment Variables

| Variable | Description |
|---|---|
| `TS_SELENIUM_BASE_URL` | Che dashboard URL |
| `TS_SELENIUM_OCP_USERNAME` | OpenShift username |
| `TS_SELENIUM_OCP_PASSWORD` | OpenShift password |
| `USERSTORY` | Test file to run (without `.spec.ts`) |
| `TS_PLATFORM` | `openshift` or `kubernetes` |
