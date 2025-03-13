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
	EditorView,
	InputBox,
	Key,
	Locators,
	NewScmView,
	SingleScmProvider,
	TextEditor,
	ViewControl,
	ViewSection
} from 'monaco-page-objects';
import { expect } from 'chai';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { DriverHelper } from '../../utils/DriverHelper';
import { OauthPage } from '../../pageobjects/git-providers/OauthPage';
import { StringUtil } from '../../utils/StringUtil';
import { Logger } from '../../utils/Logger';
import { LoginTests } from '../../tests-library/LoginTests';
import { OAUTH_CONSTANTS } from '../../constants/OAUTH_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { FACTORY_TEST_CONSTANTS, GitProviderType } from '../../constants/FACTORY_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { ViewsMoreActionsButton } from '../../pageobjects/ide/ViewsMoreActionsButton';

suite(
	`Create a workspace via launching a factory from the ${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_PROVIDER} repository and deny the access ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`,
	function (): void {
		const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
		const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
		const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
		const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
		const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;
		const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
		const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
		const oauthPage: OauthPage = e2eContainer.get(CLASSES.OauthPage);
		const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
		const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
		const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
		const viewsMoreActionsButton: ViewsMoreActionsButton = e2eContainer.get(CLASSES.ViewsMoreActionsButton);

		let projectSection: ViewSection;
		let scmProvider: SingleScmProvider;
		let rest: SingleScmProvider[];
		let scmContextMenu: ContextMenu;
		let viewsActionsButton: boolean;

		// test specific data
		const timeToRefresh: number = 1500;
		const changesToCommit: string = new Date().getTime().toString();
		const fileToChange: string = 'Date.txt';
		const commitChangesButtonLabel: string = `Commit Changes on "${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH}"`;
		const refreshButtonLabel: string = 'Refresh';
		const pushItemLabel: string = 'Push';
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

		test(`Authorize with a ${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_PROVIDER} OAuth and deny access`, async function (): Promise<void> {
			await oauthPage.login();
			await oauthPage.waitOauthPage();
			await oauthPage.denyAccess();
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		});

		test('The workspace starts with access deny flag in the url', async function (): Promise<void> {
			expect(await driverHelper.getDriver().getCurrentUrl()).contains('&error_code=access_denied');
		});

		test('Registering the running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Wait the workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		if (FACTORY_TEST_CONSTANTS.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO) {
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
				const editor: TextEditor = (await new EditorView().openEditor(fileToChange)) as TextEditor;
				await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
				Logger.debug(`editor.setText: "${changesToCommit}"`);
				await editor.setText(changesToCommit);
			});

			test('Open a source control manager', async function (): Promise<void> {
				const viewSourceControl: string = 'Source Control';
				const sourceControl: ViewControl = (await new ActivityBar().getViewControl(viewSourceControl)) as ViewControl;
				Logger.debug(`sourceControl.openView: "${viewSourceControl}"`);
				await sourceControl.openView();
				const scmView: NewScmView = new NewScmView();
				await driverHelper.waitVisibility(webCheCodeLocators.ScmView.inputField);
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
				Logger.debug('scmProvider.openMoreActions');
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
				await driverHelper.wait(timeToRefresh);
				Logger.debug(`wait and click on: "${refreshButtonLabel}"`);
				await driverHelper.waitAndClick(webCheCodeLocators.ScmView.actionConstructor(refreshButtonLabel));
				// wait while changes counter will be refreshed
				await driverHelper.wait(timeToRefresh);
				const changes: number = await scmProvider.getChangeCount();
				Logger.debug(`scmProvider.getChangeCount: number of changes is "${changes}"`);
				expect(changes).eql(0);
			});

			test('Push the changes', async function (): Promise<void> {
				await driverHelper.waitVisibility(
					webCheCodeLocators.ScmView.actionConstructor(
						`Push 1 commits to origin/${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH}`
					)
				);
				await driverHelper.waitVisibility(webCheCodeLocators.ScmView.more);
				Logger.debug('scmProvider.openMoreActions');
				scmContextMenu = await scmProvider.openMoreActions();
				await driverHelper.waitVisibility(webCheCodeLocators.ContextMenu.itemConstructor(pushItemLabel));
				Logger.debug(`scmContextMenu.select: "${pushItemLabel}"`);
				await scmContextMenu.select(pushItemLabel);
			});

			test('Insert git credentials which were asked after push', async function (): Promise<void> {
				try {
					await driverHelper.waitVisibility(webCheCodeLocators.InputBox.message);
				} catch (e) {
					Logger.info(`Workspace did not ask credentials before push - ${e};
                Known issue for github.com - https://issues.redhat.com/browse/CRW-4066, please check if not other git provider. `);
					expect(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_PROVIDER).eqls(GitProviderType.GITHUB);
				}
				const input: InputBox = new InputBox();
				await input.setText(OAUTH_CONSTANTS.TS_SELENIUM_GIT_PROVIDER_USERNAME);
				await input.confirm();
				await driverHelper.wait(timeToRefresh);
				await input.setText(OAUTH_CONSTANTS.TS_SELENIUM_GIT_PROVIDER_PASSWORD);
				await input.confirm();
				await driverHelper.wait(timeToRefresh);
			});

			test('Check if the changes were pushed', async function (): Promise<void> {
				try {
					Logger.debug(`scmProvider.takeAction: "${refreshButtonLabel}"`);
					await scmProvider.takeAction(refreshButtonLabel);
				} catch (e) {
					Logger.info(
						'Check you use correct credentials.' +
							'For bitbucket.org ensure you use an app password: https://support.atlassian.com/bitbucket-cloud/docs/using-app-passwords/;' +
							'For github.com - personal access token instead of password.'
					);
				}
				const isCommitButtonDisabled: string = await driverHelper.waitAndGetElementAttribute(
					webCheCodeLocators.ScmView.actionConstructor(commitChangesButtonLabel),
					'aria-disabled'
				);
				expect(isCommitButtonDisabled).to.be.true;
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
