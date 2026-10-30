/** *******************************************************************
 * copyright (c) 2020-2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { RestartWorkspaceDialog } from '../../pageobjects/ide/RestartWorkspaceDialog';
import { ExplorerView } from '../../pageobjects/ide/ExplorerView';
import { ShellString } from 'shelljs';
import { expect } from 'chai';

const stackName: string = 'Empty Workspace';

suite(`Create and Start ${stackName} ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const restartWorkspaceDialog: RestartWorkspaceDialog = e2eContainer.get(CLASSES.RestartWorkspaceDialog);
	const explorerView: ExplorerView = e2eContainer.get(CLASSES.ExplorerView);
	const gitCloneUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? 'https://gh.crw-qe.com/test-automation-only/ubi8/tree/ubi8-latest'
		: 'https://github.com/crw-qe/public-repo-check';
	const projectName: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED() ? 'ubi8' : 'public-repo-check';
	let clonedFilePaths: string[] = [];

	function getProjectFilePaths(): string[] {
		const findOutput: ShellString = containerTerminal.execInContainerCommand(
			`find /projects/${projectName} -path /projects/${projectName}/.git -prune -o -type f -print | sort`
		);
		expect(findOutput.code).to.equal(0, `Failed to list files in project "${projectName}"`);

		return findOutput.stdout
			.split('\n')
			.map((filePath: string): string => filePath.trim())
			.filter((filePath: string): boolean => filePath.length > 0);
	}

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});
	test(`Create and open new workspace, stack:${stackName}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace(stackName);
	});

	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
	});

	test('Register running workspace', function (): void {
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Accept the workspace as trusted', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify the project and Explorer are empty', async function (): Promise<void> {
		await explorerView.waitEmptyExplorer();

		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();

		const lsOutput: ShellString = containerTerminal.ls('/projects');
		expect(lsOutput.stdout.trim()).to.equal('');
	});

	test('Clone and open repository with devfile.yaml using the Explorer', async function (): Promise<void> {
		await explorerView.cloneAndOpenRepository(gitCloneUrl);
	});

	test('Accept the cloned project as trusted', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Capture all cloned files before restart', function (): void {
		clonedFilePaths = getProjectFilePaths();
		expect(clonedFilePaths).not.empty;
		expect(clonedFilePaths).includes(`/projects/${projectName}/devfile.yaml`);
	});

	test('Restart workspace from local devfile', async function (): Promise<void> {
		await restartWorkspaceDialog.restartFromLocalDevfile(projectName);
	});

	test('Wait for workspace to restart and open IDE', async function (): Promise<void> {
		await explorerView.waitWorkbenchSideBarDisappearance(120);
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Re-initialize workspace context after restart', function (): void {
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});

	test('Verify all cloned files exist after restart', function (): void {
		const restartedFilePaths: string[] = getProjectFilePaths();
		expect(restartedFilePaths).to.include.members(clonedFilePaths);
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
});
