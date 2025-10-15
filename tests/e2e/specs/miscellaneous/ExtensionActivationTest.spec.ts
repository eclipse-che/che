/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
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
import { expect } from 'chai';

import { DriverHelper } from '../../utils/DriverHelper';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

import { By } from 'selenium-webdriver';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ViewSection } from 'monaco-page-objects';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';

suite(`Extension Activation Test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);

	const factoryUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? FACTORY_TEST_CONSTANTS.TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL ||
			'https://gh.crw-qe.com/test-automation-only/python-hello-world/tree/test-vscode-extensions'
		: FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL ||
			'https://github.com/crw-qe/python-hello-world/tree/test-vscode-extensions';

	const PROJECT_NAME: string = 'python-hello-world';
	const PYTHON_FILE_NAME: string = 'hello-world.py';
	const RUN_PYTHON_BUTTON: By = By.xpath('//a[@aria-label="Run Python File"]');
	const TERMINAL_OUTPUT: By = By.className('xterm-screen');
	const OUTPUT_PANEL_TAB: By = By.xpath('//a[contains(@aria-label, "Output")]');
	const PYTHON_OUTPUT_OPTION: By = By.xpath('//option[text()="Python"]');
	const MONACO_SELECT_BOX: By = By.css('select.monaco-select-box');
	const OUTPUT_PANEL_CONTENT: By = By.xpath('//div[contains(@class, "output")]//div[contains(@class, "view-lines")]');
	const WORKSPACE_RESTART_TIMEOUT: number = 300000;

	let workspaceName: string = '';

	suiteSetup('Login to DevSpaces', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Create and open workspace from factory URL', async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(workspaceName, 'Workspace name was not detected').not.empty;
		registerRunningWorkspace(workspaceName);
	});

	test('Wait for workspace readiness and handle trust dialogs', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustDialogs();
	});

	test('Open Python file and run it', async function (): Promise<void> {
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		await projectSection.openItem(PROJECT_NAME, PYTHON_FILE_NAME);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);

		await driverHelper.waitAndClick(RUN_PYTHON_BUTTON);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);
	});

	test('Check terminal output shows "Hello, world!"', async function (): Promise<void> {
		const terminalOutput: string = await driverHelper.waitAndGetText(TERMINAL_OUTPUT, TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);

		expect(terminalOutput).to.include('Hello, world!');
	});

	test('Check Output panel for Python activation errors', async function (): Promise<void> {
		await driverHelper.waitAndClick(OUTPUT_PANEL_TAB);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		await driverHelper.waitAndClick(MONACO_SELECT_BOX);
		await driverHelper.waitAndClick(PYTHON_OUTPUT_OPTION);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		const outputText: string = await driverHelper.waitAndGetText(OUTPUT_PANEL_CONTENT, TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);

		expect(outputText).to.not.include('Failed to activate a workspace');
		expect(outputText).to.not.include('Missing required @injectable annotation');
	});

	test('Refresh page and verify extension still works', async function (): Promise<void> {
		await browserTabsUtil.refreshPage();
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		await projectSection.openItem(PROJECT_NAME, PYTHON_FILE_NAME);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);

		await driverHelper.waitAndClick(RUN_PYTHON_BUTTON);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);

		const terminalOutput: string = await driverHelper.waitAndGetText(TERMINAL_OUTPUT, TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);
		expect(terminalOutput).to.include('Hello, world!');

		await driverHelper.waitAndClick(OUTPUT_PANEL_TAB);
		const outputText: string = await driverHelper.waitAndGetText(OUTPUT_PANEL_CONTENT, TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);
		expect(outputText).to.not.include('Failed to activate a workspace');
	});

	test('Stop workspace', async function (): Promise<void> {
		const currentWorkspaceName: string = WorkspaceHandlingTests.getWorkspaceName();
		expect(currentWorkspaceName, 'Workspace name not available').not.empty;

		await dashboard.openDashboard();
		await dashboard.waitPage();
		await dashboard.stopWorkspaceByUI(currentWorkspaceName);
	});

	test('Start workspace after stop', async function (): Promise<void> {
		this.timeout(WORKSPACE_RESTART_TIMEOUT + 60000);

		const currentWorkspaceName: string = WorkspaceHandlingTests.getWorkspaceName();
		await workspaces.waitWorkspaceWithStoppedStatus(currentWorkspaceName);

		const originalWindowHandle: string = await browserTabsUtil.getCurrentWindowHandle();

		// workaround: Wait before restarting workspace to avoid "Container tools has state Error" issue
		// see: https://issues.redhat.com/browse/CRW-9526
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT);
		await workspaces.clickOpenButton(currentWorkspaceName);

		await browserTabsUtil.waitAndSwitchToAnotherWindow(originalWindowHandle, WORKSPACE_RESTART_TIMEOUT);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Verify Python execution after workspace restart', async function (): Promise<void> {
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		await projectSection.openItem(PROJECT_NAME, PYTHON_FILE_NAME);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);

		await driverHelper.waitAndClick(RUN_PYTHON_BUTTON);
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);

		const terminalOutput: string = await driverHelper.waitAndGetText(TERMINAL_OUTPUT, TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);
		expect(terminalOutput).to.include('Hello, world!');
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
