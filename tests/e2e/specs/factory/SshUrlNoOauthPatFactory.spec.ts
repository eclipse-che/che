/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { ActivityBar, ContextMenu, Key, Locators, NewScmView, SingleScmProvider, ViewControl, ViewSection } from 'monaco-page-objects';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { StringUtil } from '../../utils/StringUtil';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { expect } from 'chai';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Logger } from '../../utils/Logger';
import { DriverHelper } from '../../utils/DriverHelper';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { ViewsMoreActionsButton } from '../../pageobjects/ide/ViewsMoreActionsButton';
import { SourceControlView } from '../../pageobjects/ide/SourceControlView';

suite(`The SshUrlNoOauthPatFactory userstory ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const userPreferences: UserPreferences = e2eContainer.get(CLASSES.UserPreferences);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
	const viewsMoreActionsButton: ViewsMoreActionsButton = e2eContainer.get(CLASSES.ViewsMoreActionsButton);
	const sourceControlView: SourceControlView = e2eContainer.get(CLASSES.SourceControlView);
	const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;

	const factoryUrl: string =
		FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL ||
		'ssh://git@bitbucket-ssh.apps.ds-airgap-v20.crw-qe.com/~admin/private-bb-repo.git';
	const privateSshKey: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PRIVATE_KEY;
	const publicSshKey: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PUBLIC_KEY;
	const sshPassphrase: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_KEY_PASSPHRASE;
	const privateSshKeyPath: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PRIVATE_KEY_PATH;
	const publicSshKeyPath: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PUBLIC_KEY_PATH;

	let projectSection: ViewSection;
	let scmProvider: SingleScmProvider;
	let scmContextMenu: ContextMenu;
	let viewsActionsButton: boolean;
	// test specific data
	const timeToRefresh: number = 1500;
	const changesToCommit: string = 'Commit ' + new Date().getTime().toString();
	const fileToChange: string = 'Date.txt';
	const refreshButtonLabel: string = 'Refresh';
	const pushItemLabel: string = 'Push';
	let testRepoProjectName: string;

	async function deleteSshKeys(): Promise<void> {
		Logger.debug('Deleting SSH keys if they are present');
		await userPreferences.openUserPreferencesPage();
		await userPreferences.openSshKeyTab();
		if (await userPreferences.isSshKeyPresent()) {
			await userPreferences.deleteSshKeys();
		}
	}

	suite(`Create workspace from factory:${factoryUrl}`, function (): void {
		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
			await userPreferences.ensureGitConfig(
				FACTORY_TEST_CONSTANTS.TS_GIT_COMMIT_AUTHOR_NAME,
				FACTORY_TEST_CONSTANTS.TS_GIT_COMMIT_AUTHOR_EMAIL
			);
			await deleteSshKeys();
		});
		test('Add SSH keys', async function (): Promise<void> {
			await userPreferences.openUserPreferencesPage();
			await userPreferences.openSshKeyTab();
			// use environment variables if available, otherwise fall back to file paths
			if (privateSshKey && publicSshKey) {
				Logger.info('Using SSH keys from environment variables');
				await userPreferences.addSshKeysFromStrings(privateSshKey, publicSshKey, sshPassphrase);
			} else {
				Logger.info('Using SSH keys from file paths');
				await userPreferences.addSshKeysFromFiles(privateSshKeyPath, publicSshKeyPath);
			}
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
		test('Wait workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});
		test('Check a project folder has been created', async function (): Promise<void> {
			const projectName: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_PROJECT_NAME || StringUtil.getProjectNameFromGitUrl(factoryUrl);
			projectSection = await projectAndFileTests.getProjectViewSession();
			expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not
				.undefined;
		});
		test('Accept the project as a trusted one', async function (): Promise<void> {
			await projectAndFileTests.performTrustDialogs();
			// it needs to wait here for the Trust dialogs to be closed and the Editor to be ready
			await driverHelper.wait(5000);
		});
		test('Check the project files was imported', async function (): Promise<void> {
			expect(
				await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
				'Project files were not imported'
			).not.undefined;
			testRepoProjectName = StringUtil.getProjectNameFromGitUrl(factoryUrl);
		});

		test('Make changes to the file', async function (): Promise<void> {
			Logger.debug(`projectSection.openItem: "${fileToChange}"`);
			await projectSection.openItem(testRepoProjectName, fileToChange);
			await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
			await driverHelper.getDriver().findElement(webCheCodeLocators.Editor.inputArea).click();
			await driverHelper.wait(1000);

			Logger.debug('Clearing the editor with Ctrl+A');
			await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
			await driverHelper.wait(1000);
			Logger.debug('Deleting selected text');
			await driverHelper.getDriver().actions().sendKeys(Key.DELETE).perform();
			await driverHelper.wait(1000);
			Logger.debug(`Entering text: "${changesToCommit}"`);
			await driverHelper.getDriver().actions().sendKeys(changesToCommit).perform();
		});

		test('Open a source control manager', async function (): Promise<void> {
			const viewSourceControl: string = 'Source Control';
			const sourceControl: ViewControl = (await new ActivityBar().getViewControl(viewSourceControl)) as ViewControl;
			Logger.debug(`sourceControl.openView: "${viewSourceControl}"`);
			await sourceControl.openView();
			const scmView: NewScmView = new NewScmView();
			await driverHelper.waitVisibility(webCheCodeLocators.ScmView.inputField);
			let rest: SingleScmProvider[];
			[scmProvider, ...rest] = await scmView.getProviders();
			Logger.debug(`scmView.getProviders: "${JSON.stringify(scmProvider)}, ${rest}"`);
		});

		test('Check if the changes are displayed in the source control manager', async function (): Promise<void> {
			await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
			await driverHelper.wait(timeToRefresh);
			Logger.debug(`wait and click on: "${refreshButtonLabel}"`);
			await driverHelper.waitAndClick(webCheCodeLocators.ScmView.actionConstructor(refreshButtonLabel));
			// wait while changes counter will be refreshed
			await driverHelper.wait(timeToRefresh);
			const changes: number = await scmProvider.getChangeCount();
			Logger.debug(`scmProvider.getChangeCount: number of changes is "${changes}"`);
			expect(changes).eql(1);
		});

		test('Stage the changes', async function (): Promise<void> {
			await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
			viewsActionsButton = await viewsMoreActionsButton.viewsAndMoreActionsButtonIsVisible();
			if (viewsActionsButton) {
				await viewsMoreActionsButton.closeSourceControlGraph();
			}
			scmContextMenu = await scmProvider.openMoreActions();
			await driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.contextView);
			Logger.debug('scmContextMenu.select: "Changes" -> "Stage All Changes"');
			await scmContextMenu.select('Changes', 'Stage All Changes');
		});

		test('Commit the changes', async function (): Promise<void> {
			Logger.info(`ScmView inputField locator: "${(webCheCodeLocators.ScmView as any).scmEditor}"`);
			Logger.debug('Click on the Scm Editor');
			await driverHelper
				.getDriver()
				.findElement((webCheCodeLocators.ScmView as any).scmEditor)
				.click();
			await sourceControlView.typeCommitMessage(changesToCommit);
			await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
			await driverHelper.wait(timeToRefresh);
			Logger.debug(`wait and click on: "${refreshButtonLabel}"`);
			await driverHelper.waitAndClick(webCheCodeLocators.ScmView.actionConstructor(refreshButtonLabel));
			// wait while changes counter will be refreshed
			await driverHelper.wait(timeToRefresh);
			const changes: number = await scmProvider.getChangeCount();
			Logger.debug(`scmProvider.getChangeCount: number of changes is "${changes}"`);
			expect(changes).eql(0);
		});

		test('Push the changes and check if the changes were pushed', async function (): Promise<void> {
			await driverHelper.waitVisibility(webCheCodeLocators.Notification.action);
			await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
			Logger.debug('scmProvider.openMoreActions');
			scmContextMenu = await scmProvider.openMoreActions();
			await driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.itemConstructor(pushItemLabel));
			Logger.debug(`scmContextMenu.select: "${pushItemLabel}"`);
			await scmContextMenu.select(pushItemLabel);

			await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
			await driverHelper.wait(timeToRefresh);
			Logger.debug(`wait and click on: "${refreshButtonLabel}"`);
			await driverHelper.waitAndClick(webCheCodeLocators.ScmView.actionConstructor(refreshButtonLabel));
			const isCommitButtonDisabled: string = await driverHelper.waitAndGetElementAttribute(
				webCheCodeLocators.Notification.action,
				'aria-disabled'
			);
			expect(isCommitButtonDisabled).to.equal('true');
		});

		suiteTeardown('Delete SSH keys', async function (): Promise<void> {
			await dashboard.openDashboard();
			await deleteSshKeys();
		});
		suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
			await browserTabsUtil.closeAllTabsExceptCurrent();
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
		});
		suiteTeardown('Unregister running workspace', function (): void {
			registerRunningWorkspace('');
		});
	});
});
