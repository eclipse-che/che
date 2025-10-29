/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
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
import { StringUtil } from '../../utils/StringUtil';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { expect } from 'chai';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Logger } from '../../utils/Logger';

suite(`The SshUrlNoOauthPatFactory userstory ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const userPreferences: UserPreferences = e2eContainer.get(CLASSES.UserPreferences);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const factoryUrl: string =
		FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL ||
		'ssh://git@bitbucket-ssh.apps.ds-airgap2-v15.crw-qe.com/~admin/private-bb-repo.git';
	const privateSshKey: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PRIVATE_KEY;
	const publicSshKey: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PUBLIC_KEY;
	const privateSshKeyPath: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PRIVATE_KEY_PATH;
	const publicSshKeyPath: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_SSH_PUBLIC_KEY_PATH;
	let projectSection: ViewSection;

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
			await deleteSshKeys();
		});
		test('Add SSH keys', async function (): Promise<void> {
			await userPreferences.openUserPreferencesPage();
			await userPreferences.openSshKeyTab();
			// use environment variables if available, otherwise fall back to file paths
			if (privateSshKey && publicSshKey) {
				Logger.info('Using SSH keys from environment variables');
				await userPreferences.addSshKeysFromStrings(privateSshKey, publicSshKey);
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
			await projectAndFileTests.performTrustAuthorDialog();
		});
		test('Check the project files was imported', async function (): Promise<void> {
			expect(
				await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
				'Project files were not imported'
			).not.undefined;
		});
		suiteTeardown('Delete SSH keys', async function (): Promise<void> {
			await dashboard.openDashboard();
			await deleteSshKeys();
		});
		suiteTeardown('Stop and delete the workspace by APII', async function (): Promise<void> {
			await browserTabsUtil.closeAllTabsExceptCurrent();
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
		});
		suiteTeardown('Unregister running workspace', function (): void {
			registerRunningWorkspace('');
		});
	});
});
