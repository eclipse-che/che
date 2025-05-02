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
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { expect } from 'chai';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';

suite(`Create workspace using Git URL pointing to devfile.yaml ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

	const devfileFactoryUrl: string = 'https://github.com/crw-qe/quarkus-api-example-public/blob/main/devfile.yaml';
	let projectSection: ViewSection;

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test(`Create and open new workspace from devfile URL: ${devfileFactoryUrl}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(devfileFactoryUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Check project folder and files were properly imported', async function (): Promise<void> {
		projectSection = await projectAndFileTests.getProjectViewSession();
		const projectName: string = 'quarkus-api-example-public';
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not.undefined;

		expect(
			await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
			'Project files were not imported'
		).not.undefined;
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
