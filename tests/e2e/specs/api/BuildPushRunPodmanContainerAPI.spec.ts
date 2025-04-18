/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { expect } from 'chai';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellString } from 'shelljs';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { ViewSection } from 'monaco-page-objects';

suite(`Test podman build container functionality ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);

	let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
	let workspaceName: string = '';
	let originalBuildCapabilitiesSetting: string = '';
	let devSpacesNamespace: string = '';
	let cheClusterName: string = '';

	/**
	 * test requires the following files to be present in the workspace root:
	 * - A Dockerfile named Dockerfile.${ARCH} with content:
	 * FROM scratch
	 * COPY hello /hello
	 * CMD ["/hello"]
	 * - A compiled 'hello' binary, that prints "Hello from Kubedock!" to output.
	 *
	 * Used to build and run a container in a pod for verifying Podman functionality.
	 */
	const buildPushScript: string = `
export ARCH=$(uname -m)
export DATE=$(date +"%m%d%y")
export USER=$(oc whoami)
export TKN=$(oc whoami -t)
export REG="image-registry.openshift-image-registry.svc:5000"
export PROJECT=$(oc project -q)
export IMG="\${REG}/\${PROJECT}/hello:\${DATE}"
cd $PROJECT_SOURCE

podman login --tls-verify=false --username "\${USER}" --password "\${TKN}" "\${REG}"
podman build -t "\${IMG}" -f Dockerfile.\${ARCH} .
podman push --tls-verify=false "\${IMG}"
`;

	const runTestScript: string = `
export DATE=$(date +"%m%d%y")
export REG="image-registry.openshift-image-registry.svc:5000"
export PROJECT=$(oc project -q)
export IMG="\${REG}/\${PROJECT}/hello:\${DATE}"

oc delete pod test-hello-pod --ignore-not-found
oc run test-hello-pod --restart=Never --image="\${IMG}"

if ! oc wait --for=jsonpath='{.status.phase}'=Succeeded pod/test-hello-pod --timeout=60s; then
  PHASE=$(oc get pod test-hello-pod -o jsonpath='{.status.phase}')
  if [[ "$PHASE" == "Failed" ]]; then
    oc describe pod test-hello-pod
    exit 1
  fi
fi

oc logs test-hello-pod
`;

	const factoryUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? FACTORY_TEST_CONSTANTS.TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL ||
			'https://gh.crw-qe.com/test-automation-only/dockerfile-hello-world'
		: FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://github.com/crw-qe/dockerfile-hello-world';

	suiteSetup('Setup DevSpaces with container build capabilities enabled', function (): void {
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.loginToOcp();

		// get the namespace where DevSpaces is installed
		const getDevSpacesNamespaceCommand: string = 'oc get checluster --all-namespaces -o jsonpath="{.items[0].metadata.namespace}"';
		devSpacesNamespace = shellExecutor.executeCommand(getDevSpacesNamespaceCommand).stdout.trim();

		// get the name of the CheCluster
		const getCheClusterNameCommand: string = `oc get checluster -n ${devSpacesNamespace} -o jsonpath='{.items[0].metadata.name}'`;
		cheClusterName = shellExecutor.executeCommand(getCheClusterNameCommand).stdout.trim();

		// get the original value of disableContainerBuildCapabilities
		const getOriginalSettingCommand: string = `oc get checluster/${cheClusterName} -n ${devSpacesNamespace} -o jsonpath='{.spec.devEnvironments.disableContainerBuildCapabilities}'`;
		originalBuildCapabilitiesSetting = shellExecutor.executeCommand(getOriginalSettingCommand).stdout.trim();

		// patch the CheCluster to enable container build capabilities
		const patchCommand: string = `oc patch checluster/${cheClusterName} -n ${devSpacesNamespace} --type=merge -p '{"spec":{"devEnvironments":{"disableContainerBuildCapabilities":false}}}'`;
		const patchResult: ShellString = shellExecutor.executeCommand(patchCommand);

		expect(patchResult.code).to.equal(0, 'Failed to patch CheCluster to enable container build capabilities');
	});

	suiteSetup('Login into DevSpaces', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test(`Create and open new workspace from: ${factoryUrl}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(workspaceName, 'Workspace name was not detected').not.empty;
		registerRunningWorkspace(workspaceName);
	});

	test('Wait for workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Check the project files were imported', async function (): Promise<void> {
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, 'Dockerfile.x86_64'), 'Dockerfile not found in the project tree').not.undefined;
	});

	test('Build and push container image from workspace', function (): void {
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
		kubernetesCommandLineToolsExecutor.execInContainerCommand(buildPushScript);
	});

	test('Verify container image can be used in a pod', function (): void {
		const runTestScriptOutput: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(runTestScript);
		expect(runTestScriptOutput.stdout).to.include('Hello from Kubedock!', 'Expected "Hello from Kubedock!" message not found in logs');
	});

	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});

	suiteTeardown('Restore DevSpaces container build capabilities setting', function (): void {
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.loginToOcp();

		// default to false if value wasn't found
		const validSetting: string = ['true', 'false'].includes(originalBuildCapabilitiesSetting)
			? originalBuildCapabilitiesSetting
			: 'false';

		const restorePatchCommand: string = `oc patch checluster/${cheClusterName} -n ${devSpacesNamespace} --type=merge -p '{"spec":{"devEnvironments":{"disableContainerBuildCapabilities":${validSetting}}}}'`;
		const restorePatchResult: ShellString = shellExecutor.executeCommand(restorePatchCommand);

		expect(restorePatchResult.code).to.equal(0, 'Failed to restore CheCluster container build capabilities setting');
	});
});
