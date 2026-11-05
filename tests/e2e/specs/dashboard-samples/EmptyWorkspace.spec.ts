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
import { RestartWorkspaceDialog } from '../../pageobjects/ide/RestartWorkspaceDialog';
import { ExplorerView } from '../../pageobjects/ide/ExplorerView';
import { expect } from 'chai';
import { InputBox, QuickPickItem, ViewItem, ViewSection } from 'monaco-page-objects';
import { DriverHelper } from '../../utils/DriverHelper';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { CommandPalette } from '../../pageobjects/ide/CommandPalette';

const stackName: string = 'Empty Workspace';

suite(`Create and Start ${stackName} ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const restartWorkspaceDialog: RestartWorkspaceDialog = e2eContainer.get(CLASSES.RestartWorkspaceDialog);
	const explorerView: ExplorerView = e2eContainer.get(CLASSES.ExplorerView);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const commandPalette: CommandPalette = e2eContainer.get(CLASSES.CommandPalette);
	const gitCloneUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? 'https://gh.crw-qe.com/test-automation-only/ubi8/tree/ubi8-latest'
		: 'https://github.com/crw-qe/public-repo-check';
	const projectName: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED() ? 'ubi8' : 'public-repo-check';
	const devfileTaskName: string = 'devfile: File list';

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
		await explorerView.waitEmptyExplorer(TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	});

	test('Clone and open repository with devfile.yaml using the Explorer', async function (): Promise<void> {
		await explorerView.cloneAndOpenRepository(gitCloneUrl);
	});

	test('Accept the cloned project as trusted', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify the cloned repository is visible in Explorer', async function (): Promise<void> {
		await explorerView.openExplorerView();
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		const devfileItem: ViewItem | undefined = await projectSection.findItem('devfile.yaml');
		expect(devfileItem, 'The cloned devfile.yaml is not visible in Explorer').not.undefined;
	});

	test('Restart workspace from local devfile', async function (): Promise<void> {
		await restartWorkspaceDialog.restartFromLocalDevfile(projectName);
	});

	test('Wait for workspace to restart and open IDE', async function (): Promise<void> {
		await explorerView.waitWorkbenchSideBarDisappearance(120);
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify the cloned repository is visible in Explorer after restart', async function (): Promise<void> {
		await explorerView.openExplorerView();
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		const devfileItem: ViewItem | undefined = await projectSection.findItem('devfile.yaml');
		expect(devfileItem, 'The cloned devfile.yaml is not visible in Explorer after restart').not.undefined;
	});

	test('Check devfile commands are visible in the run task list', async function (): Promise<void> {
		await commandPalette.openCommandPalette();
		const input: InputBox = await InputBox.create(TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
		await input.setText('>Tasks: Run Task');
		const runTaskItem: QuickPickItem | undefined = await input.findQuickPick('Tasks: Run Task');
		expect(runTaskItem, 'The Tasks: Run Task command was not found').not.undefined;
		await runTaskItem?.click();
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);

		const devfileTask: QuickPickItem | undefined = await input.findQuickPick('devfile');
		expect(devfileTask, 'The devfile task provider was not found').not.undefined;
		await devfileTask?.click();
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);

		const commandItem: QuickPickItem | undefined = await input.findQuickPick(devfileTaskName);
		expect(commandItem, `The devfile command "${devfileTaskName}" was not found in the run task list`).not.undefined;
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
