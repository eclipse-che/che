# Run E2E Tests Using Podman/Container Image

Run E2E tests against Eclipse Che using the che-e2e container image with Podman. This provides a consistent, isolated test environment with Chrome browser and VNC support.

## Execution Mode - ASK USER FIRST

Before proceeding, ask the user which execution mode they prefer using AskUserQuestion:

1. **Just bash command** - Provide only the podman run command (uses published image, no rebuild)
2. **Build and run** - Check for changes in the working directory (`git status`), rebuild container image if there are modifications, then run the test
3. **Check staged changes only** - Only rebuild container if there are git staged changes, otherwise use published image

Based on the user's choice:
- **Just bash command**: Skip build steps, provide podman run command with `eclipse/che-e2e:nightly` image
- **Build and run**: Run `podman build` before the test if `git status` shows any changes in e2e directory
- **Check staged changes only**: Run `git diff --cached --name-only` to check for staged changes; rebuild container only if staged files exist in the e2e directory

## Required Parameters - ASK USER

Before running tests, you MUST ask the user for these required parameters using AskUserQuestion:

1. **Eclipse Che URL** (`TS_SELENIUM_BASE_URL`) - The Che dashboard URL

    - Example: `https://che.apps.ocp.crw-qe.com/`

2. **Test Name** (`USERSTORY`) - Which test to run

    - Common options: `EmptyWorkspace`, `Factory`, `SmokeTest`, `EmptyWorkspaceAPI`

3. **Username** (`TS_SELENIUM_OCP_USERNAME`) - OpenShift/Kubernetes username

    - Example: `admin`, `developer`, `che@eclipse.org`

4. **Password** (`TS_SELENIUM_OCP_PASSWORD`) - OpenShift/Kubernetes password

5. **Platform** (`TS_PLATFORM`) - Optional, deployment platform
    - Options: `openshift` (default), `kubernetes`

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

## Auto-Rebuild on Local Changes

Before running tests, check for local modifications and rebuild the container if needed:

```bash
cd tests/e2e

# Check for uncommitted changes and rebuild if found
if [[ -n $(git status --porcelain .) ]]; then
  echo "Local changes detected, rebuilding container image..."
  podman build -t quay.io/eclipse/che-e2e:local -f build/dockerfiles/Dockerfile .
  E2E_IMAGE="quay.io/eclipse/che-e2e:local"
else
  echo "No local changes, using published image..."
  E2E_IMAGE="eclipse/che-e2e:nightly"
fi

# Run tests with the appropriate image
podman rm -f selenium-e2e 2>/dev/null; \
podman run -it --shm-size=2g -p 5920:5920 \
  --name selenium-e2e \
  -e TS_SELENIUM_BASE_URL=$TS_SELENIUM_BASE_URL \
  -e TS_SELENIUM_OCP_USERNAME=$TS_SELENIUM_OCP_USERNAME \
  -e TS_SELENIUM_OCP_PASSWORD=$TS_SELENIUM_OCP_PASSWORD \
  $E2E_IMAGE
```

## Podman Run Commands

### Basic Podman Run (using published image)

```bash
cd tests/e2e
podman rm -f selenium-e2e 2>/dev/null; \
podman run -it --shm-size=2g -p 5920:5920 \
  --name selenium-e2e \
  -e TS_SELENIUM_BASE_URL=$TS_SELENIUM_BASE_URL \
  -e TS_SELENIUM_OCP_USERNAME=$TS_SELENIUM_OCP_USERNAME \
  -e TS_SELENIUM_OCP_PASSWORD=$TS_SELENIUM_OCP_PASSWORD \
  eclipse/che-e2e:nightly
```

### Podman Run with Local E2E Code Mounted

Use this when testing local modifications to E2E tests:

```bash
cd tests/e2e
podman rm -f selenium-e2e 2>/dev/null; \
podman run -it --shm-size=2g -p 5920:5920 \
  --name selenium-e2e \
  -e TS_SELENIUM_BASE_URL=$TS_SELENIUM_BASE_URL \
  -e TS_SELENIUM_OCP_USERNAME=$TS_SELENIUM_OCP_USERNAME \
  -e TS_SELENIUM_OCP_PASSWORD=$TS_SELENIUM_OCP_PASSWORD \
  -v $(pwd):/tmp/e2e:Z \
  eclipse/che-e2e:nightly
```

### Running Specific Test in Podman

```bash
podman run -it --shm-size=2g -p 5920:5920 \
  --name selenium-e2e \
  -e TS_SELENIUM_BASE_URL=$TS_SELENIUM_BASE_URL \
  -e TS_SELENIUM_OCP_USERNAME=$TS_SELENIUM_OCP_USERNAME \
  -e TS_SELENIUM_OCP_PASSWORD=$TS_SELENIUM_OCP_PASSWORD \
  -e USERSTORY=SmokeTest \
  eclipse/che-e2e:nightly
```

### Full Example with All Common Options (Recommended)

```bash
podman rm -f selenium-e2e 2>/dev/null; \
podman run -it --shm-size=2g -p 5920:5920 \
  --network="host" \
  --name selenium-e2e \
  -e TS_PLATFORM=openshift \
  -e TS_SELENIUM_BASE_URL=https://che.apps.ocp.crw-qe.com/ \
  -e TS_SELENIUM_OCP_USERNAME=admin \
  -e TS_SELENIUM_OCP_PASSWORD=password \
  -e TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=true \
  -e TS_OCP_LOGIN_PAGE_PROVIDER_TITLE=htpasswd \
  -e TS_SELENIUM_LOG_LEVEL=TRACE \
  -e TS_SELENIUM_START_WORKSPACE_TIMEOUT=600000 \
  -e TS_DEBUG_MODE=true \
  -e NODE_TLS_REJECT_UNAUTHORIZED=0 \
  -e TS_DELETE_WORKSPACE_ON_FAILED_TEST=true \
  -e VIDEO_RECORDING=true \
  -e USERSTORY=EmptyWorkspace \
  -v /tmp/e2e/report:/tmp/e2e/report:Z \
  -v /tmp/e2e/video:/tmp/ffmpeg_report:Z \
  eclipse/che-e2e:nightly
```

### Kubernetes Platform Example

```bash
podman run -it --shm-size=2g -p 5920:5920 \
  --network="host" \
  --name selenium-e2e \
  -e TS_PLATFORM=kubernetes \
  -e TS_SELENIUM_BASE_URL=$TS_SELENIUM_BASE_URL \
  -e TS_SELENIUM_K8S_USERNAME=$TS_SELENIUM_K8S_USERNAME \
  -e TS_SELENIUM_K8S_PASSWORD=$TS_SELENIUM_K8S_PASSWORD \
  -e TS_SELENIUM_VALUE_OPENSHIFT_OAUTH=false \
  -e NODE_TLS_REJECT_UNAUTHORIZED=0 \
  -e USERSTORY=EmptyWorkspace \
  eclipse/che-e2e:nightly
```

## VNC Access for Debugging

Connect to VNC at `0.0.0.0:5920` to watch tests running in the container.

## Build Local Container Image

To build and use a local container image:

```bash
cd tests/e2e
podman build -t quay.io/eclipse/che-e2e:local -f build/dockerfiles/Dockerfile .

# Then run with local image
podman run -it --shm-size=2g -p 5920:5920 \
  --name selenium-e2e \
  -e TS_SELENIUM_BASE_URL=$TS_SELENIUM_BASE_URL \
  quay.io/eclipse/che-e2e:local
```

## Cleanup

Remove existing container before running again:

```bash
podman rm -f selenium-e2e
```

## Report Collection

Mount volumes to collect test reports:

```bash
-v /local/path/report:/tmp/e2e/report:Z \
-v /local/path/video:/tmp/ffmpeg_report:Z
```
