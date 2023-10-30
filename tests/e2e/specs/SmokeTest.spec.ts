/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { ViewSection } from 'monaco-page-objects';
import { ProjectAndFileTests } from '../tests-library/ProjectAndFileTests';
import { CLASSES } from '../configs/inversify.types';
import { e2eContainer } from '../configs/inversify.config';
import { WorkspaceHandlingTests } from '../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from './MochaHooks';
import { LoginTests } from '../tests-library/LoginTests';
import { StringUtil } from '../utils/StringUtil';
import { FACTORY_TEST_CONSTANTS } from '../constants/FACTORY_TEST_CONSTANTS';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { expect } from 'chai';
import { BASE_TEST_CONSTANTS } from '../constants/BASE_TEST_CONSTANTS';

suite(`The SmokeTest userstory ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const factoryUrl: string =
		FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://github.com/che-incubator/quarkus-api-example.git';
	let projectSection: ViewSection;
	suite(`Create workspace from factory:${factoryUrl}`, function (): void {
		loginTests.loginIntoChe();
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
		test('Check the project files was imported', async function (): Promise<void> {
			expect(
				await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
				'Project files were not imported'
			).not.undefined;
		});
		test('Stop the workspace', async function (): Promise<void> {
			await workspaceHandlingTests.stopWorkspace(WorkspaceHandlingTests.getWorkspaceName());
			await browserTabsUtil.closeAllTabsExceptCurrent();
		});
		test('Delete the workspace', async function (): Promise<void> {
			await workspaceHandlingTests.removeWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});
		loginTests.logoutFromChe();
	});
});
