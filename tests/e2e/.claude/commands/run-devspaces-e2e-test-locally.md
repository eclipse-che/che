# Run E2E Tests Against Dev Spaces Using Local Browser

Run E2E tests against Red Hat OpenShift Dev Spaces using a local Chrome browser. This is useful for development and debugging as you can see the browser in real-time.

## Execution Mode - ASK USER FIRST

Before proceeding, ask the user which execution mode they prefer using AskUserQuestion:

1. **Just bash command** - Provide only the bash command to run the test (assumes dependencies installed and code already compiled)
2. **Build and run** - Check for changes in the working directory (`git status`), rebuild if there are modifications, then run the test
3. **Check staged changes only** - Only rebuild if there are git staged changes, otherwise just run the test

Based on the user's choice:
- **Just bash command**: Skip build steps, provide `export USERSTORY=<test> && npm run test` command
- **Build and run**: Run `npm ci && npm run tsc` before the test if `git status` shows any changes
- **Check staged changes only**: Run `git diff --cached --name-only` to check for staged changes; rebuild only if staged files exist in the e2e directory

## Required Parameters - ASK USER

Before running tests, you MUST ask the user for these required parameters using AskUserQuestion:

1. **Dev Spaces URL** (`TS_SELENIUM_BASE_URL`) - The Dev Spaces dashboard URL

    - Example: `https://devspaces.apps.ocp.crw-qe.com/`

2. **Test Name** (`USERSTORY`) - Which test to run

    - Common options: `EmptyWorkspace`, `Factory`, `SmokeTest`, `EmptyWorkspaceAPI`

3. **Username** (`TS_SELENIUM_OCP_USERNAME`) - OpenShift username

    - Example: `admin`, `developer`, `user@example.com`

4. **Password** (`TS_SELENIUM_OCP_PASSWORD`) - OpenShift password

5. **Login Provider** (`TS_OCP_LOGIN_PAGE_PROVIDER_TITLE`) - Optional, for SSO
    - Common options: `htpasswd`, `DevSandbox`, or leave empty for default

## Test-Specific Parameters - ASK BASED ON TEST NAME

After getting the test name, read the test spec file to identify additional required parameters.
Use `cat specs/**/${USERSTORY}.spec.ts` to find the test file and look for environment variables used.

### Factory URL Tests (Factory, RefusedOAuthFactory, NoSetupRepoFactory, etc.)
These tests navigate directly to a factory URL to create a workspace.
Ask for these additional parameters:
- **Git Repository URL** (`TS_SELENIUM_FACTORY_GIT_REPO_URL`) - Repository to clone (used to construct the factory URL)
- **Git Provider** (`TS_SELENIUM_FACTORY_GIT_PROVIDER`) - `github`, `gitlab`, `bitbucket`, `azure-devops`
- **OAuth Enabled** (`TS_SELENIUM_GIT_PROVIDER_OAUTH`) - `true` or `false`

### Git Repo Import Form Tests (FactoryWithGitRepoOptions, CreateWorkspaceWithExistingNameFromGitUrl, etc.)
These tests use the Dashboard's "Create Workspace" form to import a git repository.
Ask for these additional parameters:
- **Git Repository URL** (`TS_SELENIUM_FACTORY_GIT_REPO_URL`) - Repository URL to enter in the form
- **Branch Name** (`TS_SELENIUM_FACTORY_GIT_REPO_BRANCH`) - Optional, branch to checkout (default: `main`)
- **Project Name** (`TS_SELENIUM_PROJECT_NAME`) - Optional, expected project folder name

### API Tests (EmptyWorkspaceAPI, ContainerOverridesAPI, etc.)
Ask for:
- **Namespace** (`TS_API_TEST_NAMESPACE`) - Optional, for specific namespace testing
- **UDI Image** (`TS_API_TEST_UDI_IMAGE`) - Optional, custom Universal Developer Image

### Sample/Devfile Tests
Ask for:
- **Sample List** (`TS_SAMPLE_LIST`) - Which samples to test, e.g., `Node.js Express`

### SSH/PAT Tests (SshUrlNoOauthPatFactory, etc.)
Ask for:
- **SSH Private Key Path** or **Personal Access Token** details as needed by the test

## Prerequisites

1. **Node.js 22.x** (use nvm to manage versions)
2. **Chrome browser** version 114.x or later
3. **ChromeDriver** matching your Chrome version (installed via npm)
4. Access to a Dev Spaces instance on OpenShift

## Full Test Run (Recommended)

Complete workflow with clean build and all checks:

```bash
cd tests/e2e

# Use correct Node.js version
nvm use v22.10.0

# Set environment variables
export TS_SELENIUM_LOG_LEVEL="TRACE"
export TS_SELENIUM_BASE_URL=https://devspaces.apps.ocp.crw-qe.com/
export TS_SELENIUM_OCP_USERNAME=admin
export TS_SELENIUM_OCP_PASSWORD=password
export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=htpasswd
export TS_SELENIUM_START_WORKSPACE_TIMEOUT=600000
export VIDEO_RECORDING=true
export TS_DEBUG_MODE=true
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export NODE_TLS_REJECT_UNAUTHORIZED=0
export TESTING_APPLICATION_VERSION=3.23.0
export TS_DELETE_WORKSPACE_ON_FAILED_TEST=true
export USERSTORY=EmptyWorkspace

# Clean build and run tests
rm -rf node_modules dist && npm ci && npm run lint && npm run prettier && npm run tsc && npm test
```

## Quick Setup

```bash
cd tests/e2e

# Use correct Node.js version
nvm use v22.10.0

# Install dependencies
npm ci

# Export required environment variables
export TS_SELENIUM_BASE_URL=https://devspaces.apps.cluster.example.com
export TS_SELENIUM_OCP_USERNAME=<username>
export TS_SELENIUM_OCP_PASSWORD=<password>
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export NODE_TLS_REJECT_UNAUTHORIZED=0
```

## Running Tests

### Run Specific Test File

```bash
# UI test
export USERSTORY=EmptyWorkspace && npm run test

# Factory test
export USERSTORY=Factory && npm run test

# API-only test (no browser)
export USERSTORY=EmptyWorkspaceAPI && npm run driver-less-test
```

## Common Dev Spaces Test Configurations

### Dev Spaces with htpasswd Authentication

```bash
export TS_SELENIUM_BASE_URL=https://devspaces.apps.ocp.crw-qe.com/
export TS_SELENIUM_OCP_USERNAME=admin
export TS_SELENIUM_OCP_PASSWORD=password
export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=htpasswd
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export NODE_TLS_REJECT_UNAUTHORIZED=0
export USERSTORY=EmptyWorkspace && npm run test
```

### Dev Spaces with SSO Provider (e.g., Developer Sandbox)

```bash
export TS_SELENIUM_BASE_URL=https://devspaces.apps.sandbox.x8i5.p1.openshiftapps.com
export TS_SELENIUM_OCP_USERNAME=user@redhat.com
export TS_SELENIUM_OCP_PASSWORD=password
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE="DevSandbox"
export NODE_TLS_REJECT_UNAUTHORIZED=0
export USERSTORY=EmptyWorkspace && npm run test
```

### Dev Spaces Factory Test with GitHub OAuth

```bash
export TS_SELENIUM_BASE_URL=https://devspaces.apps.cluster.example.com
export TS_SELENIUM_OCP_USERNAME=user@example.com
export TS_SELENIUM_OCP_PASSWORD=password
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export TS_SELENIUM_FACTORY_GIT_REPO_URL=https://github.com/user/repo
export TS_SELENIUM_FACTORY_GIT_PROVIDER=github
export TS_SELENIUM_GIT_PROVIDER_OAUTH=true
export USERSTORY=Factory && npm run test
```

### Dev Spaces Factory Test with GitLab OAuth

```bash
export TS_SELENIUM_BASE_URL=https://devspaces.apps.cluster.example.com
export TS_SELENIUM_OCP_USERNAME=user@example.com
export TS_SELENIUM_OCP_PASSWORD=password
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export TS_SELENIUM_FACTORY_GIT_REPO_URL=https://gitlab.com/user/repo
export TS_SELENIUM_FACTORY_GIT_PROVIDER=gitlab
export TS_SELENIUM_GIT_PROVIDER_OAUTH=true
export USERSTORY=Factory && npm run test
```

### Dev Spaces with Bitbucket OAuth

```bash
export TS_SELENIUM_BASE_URL=https://devspaces.apps.cluster.example.com
export TS_SELENIUM_OCP_USERNAME=user@example.com
export TS_SELENIUM_OCP_PASSWORD=password
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export TS_SELENIUM_FACTORY_GIT_REPO_URL=https://bitbucket.org/user/repo
export TS_SELENIUM_FACTORY_GIT_PROVIDER=bitbucket
export TS_SELENIUM_GIT_PROVIDER_OAUTH=true
export USERSTORY=Factory && npm run test
```

### Dev Spaces with Azure DevOps

```bash
export TS_SELENIUM_BASE_URL=https://devspaces.apps.cluster.example.com
export TS_SELENIUM_OCP_USERNAME=user@example.com
export TS_SELENIUM_OCP_PASSWORD=password
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export TS_SELENIUM_FACTORY_GIT_REPO_URL=https://dev.azure.com/org/project/_git/repo
export TS_SELENIUM_FACTORY_GIT_PROVIDER=azure-devops
export TS_SELENIUM_GIT_PROVIDER_OAUTH=true
export USERSTORY=Factory && npm run test
```

## Timeout Configuration

Adjust timeouts for slower environments or large workspaces:

```bash
export TS_SELENIUM_LOAD_PAGE_TIMEOUT=60000
export TS_SELENIUM_START_WORKSPACE_TIMEOUT=600000
export TS_COMMON_DASHBOARD_WAIT_TIMEOUT=30000
export MOCHA_DEFAULT_TIMEOUT=420000
```

## Verbose Logging

Enable detailed logging for troubleshooting:

```bash
export TS_SELENIUM_LOG_LEVEL=TRACE
export TS_SELENIUM_PRINT_TIMEOUT_VARIABLES=true
export USERSTORY=Factory && npm run test
```

## Headless Mode

For CI environments or when you don't need to see the browser:

```bash
export TS_SELENIUM_HEADLESS=true
export USERSTORY=EmptyWorkspace && npm run test
```

## View Test Reports

After test execution, view Allure reports:

```bash
npm run open-allure-dasboard
```

## Pre-flight Checks

Before running tests, verify your setup:

```bash
# Check TypeScript compiles
npm run tsc

# Check linting passes
npm run lint

# Check formatting
npm run prettier -- --check .
```

## Available Test Specifications

Common tests for Dev Spaces:

- `EmptyWorkspace` - Basic workspace creation UI test
- `EmptyWorkspaceAPI` - Workspace creation via API (no browser)
- `Factory` - Factory URL workspace creation with git operations
- `SmokeTest` - Quick validation test
- `UserPreferencesTest` - User preferences functionality
- `CustomOpenVSXRegistry` - Custom plugin registry tests
- `WorkspaceIdleTimeout` - Workspace timeout behavior

Run `ls specs/` to see all available test categories.

## Key Differences from Eclipse Che

When testing Dev Spaces vs Eclipse Che:

- Always set `TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true`
- May need `TS_OCP_LOGIN_PAGE_PROVIDER_TITLE` for SSO provider selection
- Base URL format is typically `https://devspaces.apps.<cluster-domain>`
- Dev Spaces always runs on OpenShift (`TS_PLATFORM=openshift` is default)

## Troubleshooting

### ChromeDriver Version Mismatch

```bash
npm install chromedriver@<version>
```

### Certificate Errors

```bash
export NODE_TLS_REJECT_UNAUTHORIZED=0
```

### Slow Workspace Start

```bash
export TS_SELENIUM_START_WORKSPACE_TIMEOUT=600000
export TS_SELENIUM_DEFAULT_ATTEMPTS=3
```

### OAuth Login Issues

Verify the login provider title matches exactly:

```bash
# Check the login page for the provider button text
export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE="DevSandbox"
```
