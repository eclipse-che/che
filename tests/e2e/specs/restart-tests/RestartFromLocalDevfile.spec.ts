/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { Logger } from '../../utils/Logger';
import { LoginTests } from '../../tests-library/LoginTests';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { WorkspaceDetails } from '../../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { DriverHelper } from '../../utils/DriverHelper';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { RestartWorkspaceDialog } from '../../pageobjects/ide/RestartWorkspaceDialog';
import { By } from 'selenium-webdriver';
import { registerRunningWorkspace } from '../MochaHooks';

suite(`Test case with empty workspace (per-user storage) ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const restartWorkspaceDialog: RestartWorkspaceDialog = e2eContainer.get(CLASSES.RestartWorkspaceDialog);
	const testFileName: string = 'test-new-file.txt';
	const testFileContent: string = 'test file content for restart verification';
	const gitRepository: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? 'https://gh.crw-qe.com/test-automation-only/devfile-with-typo'
		: 'https://github.com/dmytro-ndp/devfile-with-typo';
	const projectName: string = 'devfile-with-typo';
	let currentTabHandle: string = 'undefined';
	const editorXpath: string = '//*[@id="editor-selector-card-che-incubator/che-code/latest"]';
	const xPathToWaitFor: string = '//*[@id="workbench.parts.sidebar"]';
	const cheClusterName: string = 'devspaces';
	let originalStorageStrategy: string = '';

	suiteSetup('Login into Che and set per-user storage strategy', async function (): Promise<void> {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		shellExecutor.executeCommand('oc project openshift-devspaces');

		originalStorageStrategy = shellExecutor
			.executeCommand(`oc get checluster/${cheClusterName} -o "jsonpath={.spec.devEnvironments.storage.pvcStrategy}"`)
			.stdout.trim();

		if (originalStorageStrategy !== 'per-user') {
			const patchResult: ShellString = shellExecutor.executeCommand(
				`oc patch checluster ${cheClusterName} --type=merge ` +
					'-p \'{"spec":{"devEnvironments":{"storage":{"pvcStrategy":"per-user"}}}}\''
			);
			expect(patchResult.code).to.equal(0, 'Failed to patch CheCluster to set per-user storage strategy');
		}

		await loginTests.loginIntoChe();
	});

	function clearCurrentTabHandle(): void {
		currentTabHandle = 'undefined';
	}

	test('Create Empty workspace', async function (): Promise<void> {
		await dashboard.openDashboard();
		currentTabHandle = await browserTabsUtil.getCurrentWindowHandle();
		await dashboard.clickCreateWorkspaceButton();
		await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndSample(editorXpath, 'Empty Workspace', xPathToWaitFor);
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Setup workspace context for API operations', function (): void {
		kubernetesCommandLineToolsExecutor.workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});

	test('Clone devfile-with-typo repository', function (): void {
		const cloneOutput: ShellString = containerTerminal.gitClone(gitRepository);
		expect(cloneOutput.stdout + cloneOutput.stderr).includes('Cloning');
	});

	test('Accept the project as a trusted one', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify cloned project exists', function (): void {
		expect(containerTerminal.ls().stdout).includes(projectName);
	});

	test('Create new file in project root', function (): void {
		const output: ShellString = containerTerminal.execInContainerCommand(
			`echo "${testFileContent}" > /projects/${projectName}/${testFileName}`
		);
		expect(output.code).to.equal(0);
	});

	test('Restart workspace from local devfile with bad image', async function (): Promise<void> {
		await restartWorkspaceDialog.restartFromLocalDevfile(projectName);
		Logger.info('Waiting for "Restart Workspace" confirmation popup');
		await restartWorkspaceDialog.confirmRestartWorkspace();
	});

	test('Wait for workspace start failure and restart', async function (): Promise<void> {
		Logger.info('Waiting for workspace to enter Failed phase and restart with default devfile');
		await restartWorkspaceDialog.clickRestartWithDefaultDevfile();
		await driverHelper.waitVisibility(By.xpath(xPathToWaitFor), TIMEOUT_CONSTANTS.TS_IDE_START_TIMEOUT);
	});

	test('Re-initialize workspace context after restart', function (): void {
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});

	test('Verify project exists with cloned files after restart', function (): void {
		expect(containerTerminal.ls().stdout).includes(projectName);
		expect(containerTerminal.ls(`/projects/${projectName}`).stdout).includes('devfile.yaml');
	});

	test('Verify new file is present in project root after restart', function (): void {
		const output: ShellString = containerTerminal.execInContainerCommand(`cat /projects/${projectName}/${testFileName}`);
		expect(output.stdout.trim()).to.equal(testFileContent);
	});

	test('Verify devfile schema validation error on invalid env declaration', async function (): Promise<void> {
		Logger.info('Appending invalid env declaration to devfile.yaml');
		const appendOutput: ShellString = containerTerminal.execInContainerCommand(
			`echo "    env:" >> /projects/${projectName}/devfile.yaml && ` +
				`echo "      - name: test-env" >> /projects/${projectName}/devfile.yaml && ` +
				`echo "        value: true" >> /projects/${projectName}/devfile.yaml`
		);
		expect(appendOutput.code).to.equal(0, 'Failed to append invalid env content to devfile.yaml');

		Logger.info('Restart from local devfile');
		await restartWorkspaceDialog.restartFromLocalDevfile(projectName);

		Logger.info('Check error popup');
		const dialogText: string = await restartWorkspaceDialog.getErrorDialogText();
		Logger.info(`Error dialog title text: ${dialogText}`);

		const detailText: string = await restartWorkspaceDialog.getErrorDetailText();
		Logger.info(`Error dialog detail text: ${detailText}`);

		expect(detailText).to.equal(
			'Devfile schema validation failed. Error: 0: instance.components[0] is not allowed to have the additional property "env"'
		);
	});

	suiteTeardown('Delete DevWorkspace', async function (): Promise<void> {
		Logger.debug('Delete DevWorkspace. After each test.');
		if (currentTabHandle !== 'undefined') {
			await browserTabsUtil.switchToWindow(currentTabHandle);
		}

		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		if (WorkspaceHandlingTests.getWorkspaceName() !== 'undefined') {
			Logger.debug('Workspace name is defined. Deleting workspace...');
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}

		WorkspaceHandlingTests.clearWorkspaceName();
		clearCurrentTabHandle();
		registerRunningWorkspace('');

		if (originalStorageStrategy !== '' && originalStorageStrategy !== 'per-user') {
			const restoreResult: ShellString = shellExecutor.executeCommand(
				`oc patch checluster ${cheClusterName} --type=merge ` +
					`-p '{"spec":{"devEnvironments":{"storage":{"pvcStrategy":"${originalStorageStrategy}"}}}}'`
			);
			expect(restoreResult.code).to.equal(0, 'Failed to restore CheCluster storage strategy');
		}
	});
});

suite(`Test case with pvc-fail workspace (bad image restart) ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const workspaceDetails: WorkspaceDetails = e2eContainer.get(CLASSES.WorkspaceDetails);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const restartWorkspaceDialog: RestartWorkspaceDialog = e2eContainer.get(CLASSES.RestartWorkspaceDialog);
	const testFileName: string = 'test-new-file.txt';
	const testFileContent: string = 'test file content for pvc-fail restart verification';
	const gitRepository: string = 'https://github.com/cgruver/test-workspace/tree/pvc-fail';
	const projectName: string = 'test-workspace';
	let currentTabHandle: string = 'undefined';
	const xPathToWaitFor: string = '//*[@id="workbench.parts.sidebar"]';
	const editorXpath: string = '//*[@id="editor-selector-card-che-incubator/che-code/latest"]';
	const cheClusterName: string = 'devspaces';
	let originalStorageStrategy: string = '';

	suiteSetup('Skip suite on disconnected clusters', function (): void {
		if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
			Logger.info('Test cluster is disconnected. Skipping pvc-fail workspace suite.');
			this.skip();
		}
	});

	suiteSetup('Login into Che and set per-workspace storage strategy', async function (): Promise<void> {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		shellExecutor.executeCommand('oc project openshift-devspaces');

		originalStorageStrategy = shellExecutor
			.executeCommand(`oc get checluster/${cheClusterName} -o "jsonpath={.spec.devEnvironments.storage.pvcStrategy}"`)
			.stdout.trim();

		if (originalStorageStrategy !== 'per-workspace') {
			const patchResult: ShellString = shellExecutor.executeCommand(
				`oc patch checluster ${cheClusterName} --type=merge ` +
					'-p \'{"spec":{"devEnvironments":{"storage":{"pvcStrategy":"per-workspace"}}}}\''
			);
			expect(patchResult.code).to.equal(0, 'Failed to patch CheCluster to set per-workspace storage strategy');
		}

		await loginTests.loginIntoChe();
	});

	function clearCurrentTabHandle(): void {
		currentTabHandle = 'undefined';
	}

	test('Create workspace from test-workspace pvc-fail branch', async function (): Promise<void> {
		await dashboard.openDashboard();
		currentTabHandle = await browserTabsUtil.getCurrentWindowHandle();
		await dashboard.clickCreateWorkspaceButton();
		await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndGitUrl(editorXpath, gitRepository, xPathToWaitFor);
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Accept the project as a trusted one', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Setup workspace context for API operations', function (): void {
		kubernetesCommandLineToolsExecutor.workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});

	test('Create new file in project folder', function (): void {
		const output: ShellString = containerTerminal.execInContainerCommand(
			`echo "${testFileContent}" > /projects/${projectName}/${testFileName}`
		);
		expect(output.code).to.equal(0);
	});

	test('Modify devfile to point to bad image location', function (): void {
		Logger.info('Replacing ":latest" with ":badimage" in devfile.yaml using sed');
		const output: ShellString = containerTerminal.execInContainerCommand(
			`sed -i 's/:latest/:badimage/g' /projects/${projectName}/devfile.yaml`
		);
		expect(output.code).to.equal(0, 'Failed to replace :latest with :badimage in devfile.yaml');

		const verifyOutput: ShellString = containerTerminal.execInContainerCommand(`cat /projects/${projectName}/devfile.yaml`);
		expect(verifyOutput.stdout).to.include(':badimage');
		expect(verifyOutput.stdout).to.not.include(':latest');
	});

	test('Restart workspace from local devfile with bad image', async function (): Promise<void> {
		await restartWorkspaceDialog.restartFromLocalDevfile(projectName);
		Logger.info('Waiting for "Restart Workspace" confirmation popup');
		await restartWorkspaceDialog.confirmRestartWorkspace();
	});

	test('Wait for workspace start failure and restart with default devfile', async function (): Promise<void> {
		Logger.info('Waiting for workspace to enter Failed phase and restart with default devfile');
		await restartWorkspaceDialog.clickRestartWithDefaultDevfile();
		await driverHelper.waitVisibility(By.xpath(xPathToWaitFor), TIMEOUT_CONSTANTS.TS_IDE_START_TIMEOUT);
	});

	test('Re-initialize workspace context after restart', function (): void {
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});

	test('Verify project exists after restart with default devfile', function (): void {
		expect(containerTerminal.ls().stdout).includes(projectName);
		expect(containerTerminal.ls(`/projects/${projectName}`).stdout).includes('devfile.yaml');
	});

	test('Verify new file is present after restart with default devfile', function (): void {
		const output: ShellString = containerTerminal.execInContainerCommand(`cat /projects/${projectName}/${testFileName}`);
		expect(output.stdout.trim()).to.equal(testFileContent);
	});

	test('Verify storage type is per-workspace in workspace details', async function (): Promise<void> {
		const workspaceName: string = WorkspaceHandlingTests.getWorkspaceName();

		Logger.info('Opening Workspaces page');
		await dashboard.openDashboard();
		await dashboard.clickWorkspacesButton();
		await workspaces.waitPage();

		Logger.info(`Opening workspace details for: ${workspaceName}`);
		await workspaces.clickWorkspaceListItemLink(workspaceName);
		await workspaceDetails.waitWorkspaceTitle(workspaceName);

		Logger.info('Verifying storage type is per-workspace on Overview tab');
		const storageTypeValue: string = await workspaceDetails.getStorageTypeValue();
		expect(storageTypeValue).to.equal('per-workspace');
	});

	suiteTeardown('Delete DevWorkspace', async function (): Promise<void> {
		Logger.debug('Delete DevWorkspace. After each test.');
		if (currentTabHandle !== 'undefined') {
			await browserTabsUtil.switchToWindow(currentTabHandle);
		}

		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		if (WorkspaceHandlingTests.getWorkspaceName() !== 'undefined') {
			Logger.debug('Workspace name is defined. Deleting workspace...');
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}

		WorkspaceHandlingTests.clearWorkspaceName();
		clearCurrentTabHandle();
		registerRunningWorkspace('');

		if (originalStorageStrategy !== '' && originalStorageStrategy !== 'per-workspace') {
			const restoreResult: ShellString = shellExecutor.executeCommand(
				`oc patch checluster ${cheClusterName} --type=merge ` +
					`-p '{"spec":{"devEnvironments":{"storage":{"pvcStrategy":"${originalStorageStrategy}"}}}}'`
			);
			expect(restoreResult.code).to.equal(0, 'Failed to restore CheCluster storage strategy');
		}
	});
});
