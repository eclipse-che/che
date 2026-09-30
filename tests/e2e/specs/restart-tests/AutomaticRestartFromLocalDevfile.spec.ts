/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { expect } from 'chai';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { LoginTests } from '../../tests-library/LoginTests';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { WorkspaceDetails } from '../../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { StringUtil } from '../../utils/StringUtil';
import { ViewSection } from 'monaco-page-objects';
import { By } from 'selenium-webdriver';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { registerRunningWorkspace } from '../MochaHooks';

suite(`Create workspace from private SSH repo with default devfile ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const workspaceDetails: WorkspaceDetails = e2eContainer.get(CLASSES.WorkspaceDetails);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const userPreferences: UserPreferences = e2eContainer.get(CLASSES.UserPreferences);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const gitSshUrl: string = process.env.TS_SELENIUM_GIT_SSH_REPO_URL || '';
	const privateSshKey: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PRIVATE_KEY;
	const publicSshKey: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PUBLIC_KEY;
	const editorXpath: string = '//*[@id="editor-selector-card-che-incubator/che-code/latest"]';
	const xPathWorkspacePart: string = '//*[@id="workbench.parts.sidebar"]';
	let currentTabHandle: string = 'undefined';

	suiteSetup('Login into Che', async function (): Promise<void> {
		if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
			Logger.info('Test cluster is disconnected. Skipping private SSH repo suite.');
			this.skip();
		}

		await loginTests.loginIntoChe();
	});

	function clearCurrentTabHandle(): void {
		currentTabHandle = 'undefined';
	}

	async function deleteSshKeysIfExists(): Promise<void> {
		await dashboard.openDashboard();
		await userPreferences.openUserPreferencesPage();
		await userPreferences.openSshKeyTab();
		if (await userPreferences.isSshKeyPresent()) {
			await userPreferences.deleteSshKeys();
		}
	}

	test('Add SSH key', async function (): Promise<void> {
		await deleteSshKeysIfExists();
		await userPreferences.addSshKeysFromStrings(privateSshKey, publicSshKey);
	});

	test('Create workspace using SSH URL of private repo', async function (): Promise<void> {
		await dashboard.openDashboard();
		currentTabHandle = await browserTabsUtil.getCurrentWindowHandle();
		await dashboard.clickCreateWorkspaceButton();
		await createWorkspace.waitPage();
		await dashboard.openChooseEditorMenu();
		await dashboard.chooseEditor(editorXpath);
		await createWorkspace.importFromGitUsingUI(gitSshUrl);
		await browserTabsUtil.waitAndSwitchToAnotherWindow(currentTabHandle, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Verify "Failed to fetch devfile" warning appears', async function (): Promise<void> {
		Logger.info('Verifying warning about failed devfile fetch');
		const alertText: string = await dashboard.getLoaderAlert();
		Logger.info(`Loader alert text: ${alertText}`);
		expect(alertText).to.include('Failed to fetch devfile');
	});

	test('Wait for workspace to start successfully without restart dialog', async function (): Promise<void> {
		Logger.info('Waiting for IDE to load — workspace should start with default devfile, no restart dialog expected');

		const isRestartDialogVisible: boolean = await driverHelper.isVisible(
			By.xpath('//span[text()="Restart with default devfile"]')
		);
		expect(isRestartDialogVisible, 'Restart dialog should not appear for workspace started with default devfile').to.be.false;
	});

	test('Verify no errors or alerts after workspace start', async function (): Promise<void> {
		Logger.info('Verifying no error dialogs or alerts are present after workspace start');
		await driverHelper.waitVisibility(By.xpath(xPathWorkspacePart), TIMEOUT_CONSTANTS.TS_IDE_START_TIMEOUT);

		const isErrorDialogVisible: boolean = await driverHelper.isVisible(
			By.xpath('//*[@class="dialog-message-text"]')
		);
		expect(isErrorDialogVisible, 'Error dialog should not be present').to.be.false;

		const isAlertVisible: boolean = await driverHelper.isVisible(By.css('h4[class*="alert__title"]'));
		expect(isAlertVisible, 'Workspace alert should not be present').to.be.false;
	});

	test('Verify correct project is present in Explorer', async function (): Promise<void> {
		const projectName: string = StringUtil.getProjectNameFromGitUrl(gitSshUrl);
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not
			.undefined;
	});

	test('Navigate to workspace Devfile tab and verify devfile content is not available', async function (): Promise<void> {
		const workspaceName: string = WorkspaceHandlingTests.getWorkspaceName();

		Logger.info('Opening Dashboard and navigating to Workspaces page');
		await dashboard.openDashboard();
		await dashboard.clickWorkspacesButton();
		await workspaces.waitPage();

		Logger.info(`Opening workspace details for: ${workspaceName}`);
		await workspaces.clickWorkspaceListItemLink(workspaceName);
		await workspaceDetails.waitWorkspaceTitle(workspaceName);

		Logger.info('Selecting Devfile tab');
		await workspaceDetails.selectTab('Devfile');

		Logger.info('Verifying "Devfile content is not available" message is shown');
		const devfileUnavailableLocator: By = By.xpath('//*[contains(text(), "Devfile content is not available")]');
		await driverHelper.waitVisibility(devfileUnavailableLocator, TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT);
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
		await deleteSshKeysIfExists();
	});
});
