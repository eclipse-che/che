# Run E2E Tests - Unified Command

Run E2E tests against **Eclipse Che** or **Red Hat OpenShift Dev Spaces** using either a **local Chrome browser** or **Podman container**.

## Arguments: $ARGUMENTS

**Usage:** `/run-e2e-test [URL USERNAME PASSWORD [TESTNAME]]`

**Examples:**

- `/run-e2e-test` - Interactive mode (recommended)
- `/run-e2e-test https://devspaces.apps.cluster.example.com/ admin mypassword SmokeTest npm`
- `/run-e2e-test https://che.apps.cluster.example.com/ admin mypassword EmptyWorkspace podman`

---

## Execution Flow

1. **Parse arguments** - Extract parameters from `$ARGUMENTS`
2. **Prompt if missing** - Collect missing values via AskUserQuestion
3. **Auto-detect platform** - Determine Eclipse Che vs Dev Spaces from URL
4. **Detect local changes** - Check if rebuild is needed based on git status
5. **Detect test-specific parameters** - Read spec file for required env vars
6. **Generate bash commands** - Build appropriate bash commands
7. **Execute** - Run the test after user confirmation

---

## ⚠️ MANDATORY RULES

1. **Never print bash commands with `&& \` or multi-line continuations using `\`** - Commands using line continuation characters (`\`) or chained with `&& \` cannot be copy-pasted properly from the terminal. Instead, present commands as separate lines or use semicolons for simple chains.

    ❌ **Wrong (line continuations):**

    ```bash
    cd /path/to/dir && \
    npm ci && \
    npm run test
    ```

    ❌ **Wrong (backslash continuations):**

    ```bash
    podman run -it \
      --shm-size=2g \
      -p 5920:5920 \
      quay.io/eclipse/che-e2e:next
    ```

    ✅ **Correct (separate lines):**

    ```bash
    cd /path/to/dir
    npm ci
    npm run test
    ```

    ✅ **Correct (single line for podman):**

    ```bash
    podman run -it --shm-size=2g -p 5920:5920 quay.io/eclipse/che-e2e:next
    ```

---

## Step 1: Parse Arguments

Parse `$ARGUMENTS` to extract positional parameters:

```
$ARGUMENTS = "URL USERNAME PASSWORD [TESTNAME]"
```

**Parsing logic:**

1. Split `$ARGUMENTS` by whitespace
2. Assign to variables: `URL`, `USERNAME`, `PASSWORD`, `TESTNAME`, `RUN_METHOD`
3. If `TESTNAME` is not provided, default to `EmptyWorkspace`

**Example:**

```
Input: "https://devspaces.apps.example.com/ admin secret123 SmokeTest npm"
Result:
  - URL = "https://devspaces.apps.example.com/"
  - USERNAME = "admin"
  - PASSWORD = "secret123"
  - TESTNAME = "SmokeTest"
  - RUN_METHOD = "npm"
```

---

## Step 2: Prompt for Missing Values

If `$ARGUMENTS` is empty or incomplete, use **AskUserQuestion** to collect missing parameters.

### Question 1 - Run Method (header: "Method")

- "How do you want to run the test?"
- Options:
    - Option 1: "npm run in local browser (Recommended)" - "Uses local Chrome and Node.js - faster, see browser in real-time"
    - Option 2: "Podman container" - "Isolated environment with VNC support at localhost:5920"

### Question 2 - Execution Mode (header: "Build")

- "How should local changes be handled?"
- Options:
    - Option 1: "Build and run (Recommended)" - "Compare to origin/main, rebuild if any changes exist"
    - Option 2: "Just run" - "Skip build, assume code is already compiled"

### Question 3 - Test Name (header: "Test")

- "Which test do you want to run?"
- Options:
    - Option 1: "EmptyWorkspace (Recommended)" - "Basic workspace creation test"
    - Option 2: "SmokeTest" - "Quick validation of core functionality"
    - Option 3: "Factory" - "Factory URL workspace creation with git operations"
    - Option 4: "EmptyWorkspaceAPI" - "API-only test (no browser needed)"

### For URL - Ask in plain text:

> "What is the dashboard URL? (e.g., https://devspaces.apps.cluster.example.com/ or https://che.apps.cluster.example.com/)"

### For Username and Password - Ask in plain text:

> "Please provide your credentials:
>
> - **Username**: (e.g., admin, user)
> - **Password**: your password"

Wait for the user's response before proceeding.

---

## Step 3: Auto-detect Testing Product from URL

Analyze the URL to determine which product is being tested and set appropriate defaults:

| URL Pattern            | Product                          | Auto-configured Settings                                                                                                                                          |
| ---------------------- | -------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Contains `devspaces`   | **Red Hat OpenShift Dev Spaces** | `TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true`, `TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=htpasswd` `TS_PLATFORM=openshift`                                                     |
| Contains `eclipse-che` | **Eclipse Che**                  | May need OAuth config based on deployment. I host contains `crw-qe.com` - it is `TS_PLATFORM=openshift`. Otherwise ask user for platform: openshift or kubernetes |

**Detection logic:**

```bash
if [[ "$URL" == *"devspaces"* ]]; then
  PRODUCT="devspaces"
  TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
  TS_OCP_LOGIN_PAGE_PROVIDER_TITLE="htpasswd"
elif [[ "$URL" == *"eclipse-che"* ]]; then
  PRODUCT="che"
fi
```

---

## Step 4: Detect Local Changes

Check the git status to determine if code needs to be rebuilt before running tests.

**Important:** Ignore changes to documentation and Claude configuration files:

- `CLAUDE.md`
- `.claude/**/*`
- `*.md` files (documentation)

### For Local Browser Method:

**Smart rebuild based on all changes vs origin/main** - rebuild when there are any test code modifications compared to origin/main.

**Logic:**

1. Compare working tree to `origin/main` to detect all changes (committed, staged, and unstaged)
2. Rebuild if any test code changes exist compared to `origin/main` (excluding docs/config files)

```bash
NEEDS_REBUILD=false

# Get current branch name for informational purposes
CURRENT_BRANCH=$(git branch --show-current)
echo "Current branch: $CURRENT_BRANCH"

# Check for any changes compared to origin/main (committed, staged, and unstaged)
# Excludes docs and Claude config files
CODE_CHANGES=$(git diff --name-only origin/main -- . 2>/dev/null | grep -v -E '(CLAUDE\.md|\.claude/|README\.md|\.md$)')

if [[ -n "$CODE_CHANGES" ]]; then
  echo "Code changes detected compared to origin/main:"
  echo "$CODE_CHANGES"
  NEEDS_REBUILD=true
else
  echo "No test code changes compared to origin/main - no rebuild needed"
fi

echo "Rebuild needed: $NEEDS_REBUILD"
```

### For Podman Container Method:

**Smart mounting based on all changes vs origin/main** - mount full directory when there are any test code modifications compared to origin/main.

**Logic:**

1. Compare working tree to `origin/main` to detect all changes (committed, staged, and unstaged)
2. Mount full directory if any test code changes exist compared to `origin/main` (excluding docs/config files)

```bash
E2E_IMAGE="quay.io/eclipse/che-e2e:next"
MOUNT_FULL_CODE=false

# Get current branch name for informational purposes
CURRENT_BRANCH=$(git branch --show-current)
echo "Current branch: $CURRENT_BRANCH"

# Check for any changes compared to origin/main (committed, staged, and unstaged)
# Excludes docs and Claude config files
CODE_CHANGES=$(git diff --name-only origin/main -- . 2>/dev/null | grep -v -E '(CLAUDE\.md|\.claude/|README\.md|\.md$)')

if [[ -n "$CODE_CHANGES" ]]; then
  echo "Code changes detected compared to origin/main:"
  echo "$CODE_CHANGES"
  MOUNT_FULL_CODE=true
  PODMAN_MOUNT="-v $(pwd):/tmp/e2e:Z"
else
  echo "No test code changes compared to origin/main - using published image code"
  PODMAN_MOUNT="-v $(pwd)/report:/tmp/e2e/report:Z"
fi

echo "Mount strategy: $PODMAN_MOUNT"
```

---

## Step 5: Detect Test-Specific Parameters

After getting the test name, read the test spec file to identify additional required environment variables.

### Find and read the spec file:

```bash
# Find the test file
SPEC_FILE=$(find specs -name "${USERSTORY}.spec.ts" 2>/dev/null | head -1)

if [[ -n "$SPEC_FILE" ]]; then
  echo "Found test file: $SPEC_FILE"
  # Read the file to identify required environment variables
  cat "$SPEC_FILE"
fi
```

### Platform-Specific Configurations

#### OpenShift

```bash
export TS_PLATFORM=openshift
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE="htpasswd"
```

#### Kubernetes

```bash
export TS_PLATFORM=kubernetes
export TS_SELENIUM_K8S_USERNAME=che@eclipse.org
export TS_SELENIUM_K8S_PASSWORD=admin
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=false
```

### Test-specific parameters by category:

#### Factory URL Tests (Factory, RefusedOAuthFactory, NoSetupRepoFactory, etc.)

These tests navigate directly to a factory URL to create a workspace.
**Ask for these additional parameters:**

- **Git Repository URL** (`TS_SELENIUM_FACTORY_GIT_REPO_URL`) - Repository to clone
- **Git Provider** (`TS_SELENIUM_FACTORY_GIT_PROVIDER`) - `github`, `gitlab`, `bitbucket`, `azure-devops`
- **OAuth Enabled** (`TS_SELENIUM_GIT_PROVIDER_OAUTH`) - `true` or `false`

#### Git Repo Import Form Tests (FactoryWithGitRepoOptions, CreateWorkspaceWithExistingNameFromGitUrl, etc.)

These tests use the Dashboard's "Create Workspace" form.
**Ask for these additional parameters:**

- **Git Repository URL** (`TS_SELENIUM_FACTORY_GIT_REPO_URL`) - Repository URL
- **Branch Name** (`TS_SELENIUM_FACTORY_GIT_REPO_BRANCH`) - Optional (default: `main`)
- **Project Name** (`TS_SELENIUM_PROJECT_NAME`) - Optional

#### API Tests (EmptyWorkspaceAPI, ContainerOverridesAPI, etc.)

**Ask for these additional parameters:**

- **Namespace** (`TS_API_TEST_NAMESPACE`) - Optional
- **UDI Image** (`TS_API_TEST_UDI_IMAGE`) - Optional

#### Sample/Devfile Tests

**Ask for these additional parameters:**

- **Sample List** (`TS_SAMPLE_LIST`) - e.g., `Node.js Express`

#### SSH/PAT Tests (SshUrlNoOauthPatFactory, etc.)

**Ask for these additional parameters:**

- **SSH Private Key Path** or **Personal Access Token** details

---

## Step 6: Generate Bash Commands

Based on all collected parameters, generate the appropriate bash commands.

### Option A: Local Browser Commands

#### Browser/ChromeDriver Compatibility Check

Before running tests, verify ChromeDriver compatibility with local Chrome browser:

```bash
# Get local Chrome version
CHROME_VERSION=$(google-chrome --version 2>/dev/null | grep -oP '\d+' | head -1)
echo "Local Chrome major version: $CHROME_VERSION"

# Get ChromeDriver version from package.json
CHROMEDRIVER_VERSION=$(npm list chromedriver --depth=0 2>/dev/null | grep -oP 'chromedriver@\K[\d.]+')
echo "ChromeDriver version in package.json: $CHROMEDRIVER_VERSION"

# If versions don't match, suggest installing compatible driver
if [[ "${CHROMEDRIVER_VERSION%%.*}" != "$CHROME_VERSION" ]]; then
  echo "WARNING: ChromeDriver version mismatch!"
  echo "Install compatible version: npm install chromedriver@$CHROME_VERSION"
fi
```

**If ChromeDriver is incompatible**, install a matching version:

```bash
npm install chromedriver@<chrome-major-version>
```

#### Full Test Run (Build and Run)

```bash
cd tests/e2e

# Use correct Node.js version
nvm use v22.10.0

# Get current branch name for informational purposes
CURRENT_BRANCH=$(git branch --show-current)
echo "Current branch: $CURRENT_BRANCH"

# Check for any changes compared to origin/main (committed, staged, and unstaged)
# Excludes docs and Claude config files
CODE_CHANGES=$(git diff --name-only origin/main -- . 2>/dev/null | grep -v -E '(CLAUDE\.md|\.claude/|README\.md|\.md$)')

if [[ -n "$CODE_CHANGES" ]]; then
  echo "Code changes detected compared to origin/main:"
  echo "$CODE_CHANGES"
  NEEDS_REBUILD=true
else
  echo "No test code changes compared to origin/main - skipping rebuild"
  NEEDS_REBUILD=false
fi

# Check if package.json was modified compared to origin/main
PACKAGE_JSON_CHANGED=$(git diff --name-only origin/main -- package.json 2>/dev/null)

# Rebuild if changes detected compared to origin/main
if [[ "$NEEDS_REBUILD" == "true" ]]; then
  echo "Rebuilding TypeScript..."
  rm -rf node_modules dist

  # Use npm install if package.json changed, otherwise npm ci
  if [[ -n "$PACKAGE_JSON_CHANGED" ]]; then
    echo "package.json modified - using npm install to update package-lock.json"
    npm install
  else
    npm ci
  fi

  npm run lint
  npm run prettier
  npm run tsc
fi

# Set environment variables
export TS_SELENIUM_BASE_URL="<URL>"
export TS_SELENIUM_OCP_USERNAME="<USERNAME>"
export TS_SELENIUM_OCP_PASSWORD="<PASSWORD>"
export USERSTORY="<TESTNAME>"
export TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true
export NODE_TLS_REJECT_UNAUTHORIZED=0

# Run the test
npm run test
```

#### API-Only Tests (No Browser)

```bash
export USERSTORY=EmptyWorkspaceAPI
npm run driver-less-test
```

### Option B: Podman Container Commands

**Important:** Use `--env-host` to pass all exported environment variables from the host to the container.

**Smart mounting logic based on changes vs origin/main:**

- **Changes compared to origin/main**: Mount full directory `-v $(pwd):/tmp/e2e:Z` to test local code
- **No changes compared to origin/main**: Mount only report directory `-v $(pwd)/report:/tmp/e2e/report:Z`

#### With Test Code Changes vs origin/main (mount full directory):

Use this when there are test code changes compared to `origin/main` (works for feature branches or local main with unpushed changes).

```bash
cd tests/e2e

# Set environment variables
export NODE_TLS_REJECT_UNAUTHORIZED=0
export TS_SELENIUM_BASE_URL=<URL>
export TS_SELENIUM_OCP_USERNAME=<USERNAME>
export TS_SELENIUM_OCP_PASSWORD=<PASSWORD>
export USERSTORY=<TESTNAME>

# Run with full local code mounted (testing changes vs origin/main)
podman rm -f selenium-e2e 2>/dev/null
podman run -it --shm-size=2g -p 5920:5920 --env-host -v $(pwd):/tmp/e2e:Z quay.io/eclipse/che-e2e:next
```

#### No Test Code Changes vs origin/main (mount only report directory):

```bash
cd tests/e2e

# Set environment variables
export NODE_TLS_REJECT_UNAUTHORIZED=0
export TS_SELENIUM_BASE_URL=<URL>
export TS_SELENIUM_OCP_USERNAME=<USERNAME>
export TS_SELENIUM_OCP_PASSWORD=<PASSWORD>
export USERSTORY=<TESTNAME>

# Run with only report directory mounted (no changes vs origin/main, uses published image code)
podman rm -f selenium-e2e 2>/dev/null
podman run -it --shm-size=2g -p 5920:5920 --env-host -v $(pwd)/report:/tmp/e2e/report:Z quay.io/eclipse/che-e2e:next
```

---

## Step 7: Execute After User Confirmation

Before executing, present the generated command(s) to the user and ask for confirmation.

### Confirmation prompt:

> "Here is the command I will run:
>
> ```bash
> <generated command>
> ```
>
> **Summary:**
>
> - **Platform:** [OpenShift/Kubernetes]
> - **Product:** [Eclipse Che/Dev Spaces]
> - **Test:** [TESTNAME]
> - **Method:** [Local browser/Podman container]
> - **URL:** [URL]
>
> Shall I proceed?"

### After confirmation:

1. Execute the bash command(s)
2. Monitor the output
3. Report success or failure
4. If using Podman, remind user about VNC access at `localhost:5920`

---

## Debugging Options

### Verbose Logging

```bash
export TS_SELENIUM_LOG_LEVEL=TRACE
export TS_SELENIUM_PRINT_TIMEOUT_VARIABLES=true
export TS_DEBUG_MODE=true
```

### Headless Mode (CI)

```bash
export TS_SELENIUM_HEADLESS=true
```

### Video Recording

```bash
export VIDEO_RECORDING=true
```

### Keep Workspace on Failure

```bash
export TS_DELETE_WORKSPACE_ON_FAILED_TEST=false
```

---

## VNC Access for Visual Debugging (Podman only)

Connect to VNC at `localhost:5920` to watch tests running in the container.

**VNC clients:**

- **TigerVNC** (recommended): `sudo dnf install tigervnc` then `vncviewer localhost:5920`
- **Remmina**: `sudo dnf install remmina remmina-plugins-vnc` (GUI-based)
- **Vinagre**: `sudo dnf install vinagre` then `vinagre localhost:5920`

---

## View Test Reports

After test execution:

```bash
npm run open-allure-dasboard
```

---

## Troubleshooting

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

Verify the login provider title matches exactly what appears on the login page:

```bash
export TS_OCP_LOGIN_PAGE_PROVIDER_TITLE="htpasswd"
```

### Container Cleanup

```bash
podman rm -f selenium-e2e
```

---

## Debugging TypeScript Tests in VS Code

For debugging E2E tests with breakpoints, see: https://code.visualstudio.com/docs/typescript/typescript-debugging
