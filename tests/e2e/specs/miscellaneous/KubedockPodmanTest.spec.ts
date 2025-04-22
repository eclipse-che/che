/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { ViewSection } from 'monaco-page-objects';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { expect } from 'chai';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellString } from 'shelljs';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';

suite(
	`Check possibility to manage containers within a workspace with kubedock and podman ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`,
	function (): void {
		const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
		const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
		const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
		const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
		const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
		const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

		let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
		let workspaceName: string = '';

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
		const testScript: string =
			'# Enable Kubedock\n' +
			'export KUBEDOCK_ENABLED=true\n' +
			'echo KUBEDOCK_ENABLED\n' +
			'/entrypoint.sh\n' +
			'cd $PROJECT_SOURCE\n' +
			'export ARCH=$(uname -m)\n' +
			'export DATE=$(date +"%m%d%y")\n' +
			'export USER=$(oc whoami)\n' +
			'export TKN=$(oc whoami -t)\n' +
			'export REG="image-registry.openshift-image-registry.svc:5000"\n' +
			'export PROJECT=$(oc project -q)\n' +
			'export IMG="${REG}/${PROJECT}/hello:${DATE}"\n' +
			'podman login --tls-verify=false --username ${USER} --password ${TKN} ${REG}\n' +
			'podman build -t ${IMG} -f Dockerfile.${ARCH}\n' +
			'podman push --tls-verify=false ${IMG}\n';

		const runTestScript: string =
			'# Enable Kubedock\n' +
			'export KUBEDOCK_ENABLED=true\n' +
			'echo KUBEDOCK_ENABLED\n' +
			'/entrypoint.sh\n' +
			'export DATE=$(date +"%m%d%y")\n' +
			'export USER=$(oc whoami)\n' +
			'export TKN=$(oc whoami -t)\n' +
			'export REG="image-registry.openshift-image-registry.svc:5000"\n' +
			'export PROJECT=$(oc project -q)\n' +
			'export IMG="${REG}/${PROJECT}/hello:${DATE}"\n' +
			'podman login --tls-verify=false --username ${USER} --password ${TKN} ${REG}\n' +
			'podman run --rm ${IMG}';

		const factoryUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
			? FACTORY_TEST_CONSTANTS.TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL ||
				'https://gh.crw-qe.com/test-automation-only/dockerfile-hello-world'
			: FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://github.com/crw-qe/dockerfile-hello-world';

		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		test(`Create and open new workspace from factory:${factoryUrl}`, async function (): Promise<void> {
			await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
			workspaceName = WorkspaceHandlingTests.getWorkspaceName();
			expect(workspaceName, 'Workspace name was not fetched from the loading page').not.undefined;
		});

		test('Register running workspace', function (): void {
			registerRunningWorkspace(workspaceName);
		});

		test('Wait workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Check the project files were imported', async function (): Promise<void> {
			const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
			expect(await projectAndFileTests.getProjectTreeItem(projectSection, 'Dockerfile.ppc64le'), 'Files not imported').not.undefined;
		});

		test('Create and check container runs using kubedock and podman', function (): void {
			kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
			kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
			kubernetesCommandLineToolsExecutor.loginToOcp();
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(testScript);
			expect(output, 'Podman test script failed').contains('Successfully tagged');
			const runOutput: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(runTestScript);
			expect(runOutput, 'Podman test script failed').contains('Hello from Kubedock!');
		});

		suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
			await dashboard.openDashboard();
			await browserTabsUtil.closeAllTabsExceptCurrent();
		});

		suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
		});

		suiteTeardown('Unregister running workspace', function (): void {
			registerRunningWorkspace('');
		});
	}
);
