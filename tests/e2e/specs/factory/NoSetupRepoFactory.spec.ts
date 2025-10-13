/** *******************************************************************
 * copyright (c) 2021-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { e2eContainer } from '../../configs/inversify.config';
import {
	ActivityBar,
	ContextMenu,
	Key,
	Locators,
	ModalDialog,
	NewScmView,
	SingleScmProvider,
	ViewControl,
	ViewSection
} from 'monaco-page-objects';
import { expect } from 'chai';
import { StringUtil } from '../../utils/StringUtil';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { DriverHelper } from '../../utils/DriverHelper';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Logger } from '../../utils/Logger';
import { LoginTests } from '../../tests-library/LoginTests';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { ViewsMoreActionsButton } from '../../pageobjects/ide/ViewsMoreActionsButton';

suite(
	`Create a workspace via launching a factory from the ${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_PROVIDER} repository without PAT/OAuth setup ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`,
	function (): void {
		const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
		const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
		const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
		const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
		const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;
		const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
		const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
		const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
		const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
		const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
		const viewsMoreActionsButton: ViewsMoreActionsButton = e2eContainer.get(CLASSES.ViewsMoreActionsButton);

		let projectSection: ViewSection;
		let scmProvider: SingleScmProvider;
		let scmContextMenu: ContextMenu;
		let viewsActionsButton: boolean;

		// test specific data
		const timeToRefresh: number = 1500;
		const changesToCommit: string = new Date().getTime().toString();
		const fileToChange: string = 'Date.txt';
		const dialogText: string = 'Make sure you configure your "user.name" and "user.email" in git.';
		const refreshButtonLabel: string = 'Refresh';
		const label: string = BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME;
		let testRepoProjectName: string;
		const isPrivateRepo: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO ? 'private' : 'public';

		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		test(`Navigate to the ${isPrivateRepo} repository factory URL`, async function (): Promise<void> {
			await browserTabsUtil.navigateTo(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_URL());
			await createWorkspace.performTrustAuthorPopup();
		});

		if (FACTORY_TEST_CONSTANTS.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO) {
			test('Check creating workspace using default devfile', async function (): Promise<void> {
				await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
				registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
				await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
			});

			test('Check that a project folder has not been cloned', async function (): Promise<void> {
				testRepoProjectName = StringUtil.getProjectNameFromGitUrl(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL);
				await driverHelper.waitVisibility(webCheCodeLocators.TitleBar.itemElement);
				await projectAndFileTests.performTrustAuthorDialog();
				const isProjectFolderUnable: string = await driverHelper.waitAndGetElementAttribute(
					(webCheCodeLocators.TreeItem as any).projectFolderItem,
					'aria-label'
				);
				expect(isProjectFolderUnable).to.contain('No Folder Opened Section');
			});
		} else {
			test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
				await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
			});

			test('Registering the running workspace', function (): void {
				registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
			});

			test('Wait the workspace readiness', async function (): Promise<void> {
				await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
			});

			test('Check if a project folder has been created', async function (): Promise<void> {
				testRepoProjectName = StringUtil.getProjectNameFromGitUrl(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL);
				projectSection = await projectAndFileTests.getProjectViewSession();
				expect(await projectAndFileTests.getProjectTreeItem(projectSection, testRepoProjectName), 'Project folder was not imported')
					.not.undefined;
			});

			test('Accept the project as a trusted one', async function (): Promise<void> {
				await projectAndFileTests.performTrustAuthorDialog();
			});

			test('Check if the project files were imported', async function (): Promise<void> {
				expect(await projectAndFileTests.getProjectTreeItem(projectSection, label), 'Project files were not imported').not
					.undefined;
			});

			test('Make changes to the file', async function (): Promise<void> {
				Logger.debug(`projectSection.openItem: "${fileToChange}"`);
				await projectSection.openItem(testRepoProjectName, fileToChange);
				await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
				await driverHelper.getDriver().findElement(webCheCodeLocators.Editor.inputArea).click();

				Logger.debug('Clearing the editor with Ctrl+A');
				await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
				await driverHelper.wait(500);
				Logger.debug('Deleting selected text');
				await driverHelper.getDriver().actions().sendKeys(Key.DELETE).perform();
				await driverHelper.wait(500);
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
				Logger.debug(`Type commit text: "Commit ${changesToCommit}"`);
				await driverHelper.getDriver().actions().sendKeys(changesToCommit).perform();
				Logger.debug('Press Enter to commit the changes');
				await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys(Key.ENTER).keyUp(Key.CONTROL).perform();
				await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
				await driverHelper.waitVisibility(webCheCodeLocators.Dialog.details);
				const modalDialog: ModalDialog = new ModalDialog();
				const messageDetails: string = await modalDialog.getDetails();
				Logger.debug(`modalDialog.getDetails: "${messageDetails}"`);
				expect(messageDetails).to.contain(dialogText);
			});
		}

		suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
			await dashboard.openDashboard();
			await browserTabsUtil.closeAllTabsExceptCurrent();
		});

		suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
			// to avoid a possible creating workspace which is not appeared on Dashboard yet. TODO: implement a better solution.
			await driverHelper.wait(30000);
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
		});

		suiteTeardown('Unregister running workspace', function (): void {
			registerRunningWorkspace('');
		});
	}
);
