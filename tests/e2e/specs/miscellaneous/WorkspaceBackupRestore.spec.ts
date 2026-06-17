/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { ViewSection, Locators } from 'monaco-page-objects';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { expect } from 'chai';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { Key } from 'selenium-webdriver';
import { StringUtil } from '../../utils/StringUtil';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

const factoryUrl: string =
	FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://gh.crw-qe.com/test-automation-only/python-hello-world.git';
const fileToChange: string = 'README.md';
const testFileContent: string = '# This is a test file for backup verification';

suite(`"Restore workspace from backup" test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
	const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;

	let projectSection: ViewSection;
	let workspaceName: string;
	let projectName: string;

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});
	test(`Create and open new workspace from factory:${factoryUrl}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
	});
	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
	});
	test('Register running workspace', function (): void {
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});
	test('Wait workspace readiness and project folder has been created', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});
	test('Check a project folder has been created', async function (): Promise<void> {
		projectName = FACTORY_TEST_CONSTANTS.TS_SELENIUM_PROJECT_NAME || StringUtil.getProjectNameFromGitUrl(factoryUrl);
		projectSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not.undefined;
	});
	test('Accept the project as a trusted one', async function (): Promise<void> {
		await projectAndFileTests.performTrustDialogs();
	});
	test('Make changes to the file', async function (): Promise<void> {
		Logger.debug(`projectSection.openItem: "${fileToChange}"`);
		await projectSection.openItem(projectName, fileToChange);
		await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
		await driverHelper.getDriver().findElement(webCheCodeLocators.Editor.inputArea).click();

		Logger.debug('Clearing the editor with Ctrl+A');
		await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
		await driverHelper.wait(500);
		Logger.debug('Deleting selected text');
		await driverHelper.getDriver().actions().sendKeys(Key.DELETE).perform();
		await driverHelper.wait(500);
		Logger.debug(`Entering text: "${testFileContent}"`);
		await driverHelper.getDriver().actions().sendKeys(testFileContent).perform();
		await driverHelper.wait(3000);
	});
	test('Stop the workspace', async function (): Promise<void> {
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(workspaceName, 'Workspace name not available').not.empty;
		await dashboard.openDashboard();
		await dashboard.waitPage();
		await dashboard.stopWorkspaceByUI(workspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await workspaces.waitWorkspaceWithStoppedStatus(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Never');
	});
	test('Wait for backup completion', async function (): Promise<void> {
		await workspaces.waitWorkspaceListItem(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Success');
	});
	test('Delete the workspace', async function (): Promise<void> {
		await dashboard.deleteStoppedWorkspaceByUI(workspaceName);
	});
	test('Open backups page', async function (): Promise<void> {
		await workspaces.openBackupsPage();
		await workspaces.waitWorkspaceListItem(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Success');
	});
	test('Restore workspace from backup', async function (): Promise<void> {
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await workspaces.createWorkspaceFromBackupButton(workspaceName);
		await workspaces.restoreWorkspaceFromDefaultRegistry();
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	});
	test('Wait workspace readiness after restore', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustDialogs();
		projectSection = await projectAndFileTests.getProjectViewSession();
	});
	test('Verify project folder and files exist after restore', async function (): Promise<void> {
		await projectAndFileTests.expandProjectTreeItem(projectSection, projectName);
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not restored').not.undefined;
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, fileToChange), 'Project files were not restored').not.undefined;
	});
	test('Open test file after restore', async function (): Promise<void> {
		await projectAndFileTests.openFileAndVerify(projectSection, projectName, fileToChange);
	});
	test('Verify test file content is intact after restore', async function (): Promise<void> {
		Logger.debug('Wait for file content is intact after restore');
		await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
		await driverHelper.getDriver().findElement(webCheCodeLocators.Editor.inputArea).click();

		Logger.debug('Select all text in the editor');
		await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
		await driverHelper.wait(500);
		Logger.debug('Copy text to clipboard');
		await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('c').keyUp(Key.CONTROL).perform();
		await driverHelper.wait(500);

		Logger.debug('Create hidden buffer to read clipboard');
		await driverHelper.getDriver().executeScript(`
			let input = document.createElement('textarea');
			input.setAttribute('id', 'clipboard-buffer');
			document.body.appendChild(input);
			input.focus();
		`);
		Logger.debug('Paste clipboard content to buffer');
		await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('v').keyUp(Key.CONTROL).perform();
		await driverHelper.wait(500);

		Logger.debug('Get text from buffer');
		const restoredContent: string = await driverHelper.getDriver().executeScript(`
			let input = document.getElementById('clipboard-buffer');
			let text = input.value;
			input.remove();
			return text;
		`);

		Logger.debug(`Restored content: "${restoredContent}"`);
		expect(restoredContent).to.equal(testFileContent);
	});
	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});
	suiteTeardown('Stop and delete workspace by API', async function (): Promise<void> {
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
	});
	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
