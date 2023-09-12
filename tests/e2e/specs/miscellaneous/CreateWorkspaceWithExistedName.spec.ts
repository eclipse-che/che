/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { ViewSection, SideBarView, ViewItem } from 'monaco-page-objects';
import { CLASSES } from '../../configs/inversify.types';
import { expect } from 'chai';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';

const stackName: string = BASE_TEST_CONSTANTS.TS_SELENIUM_DASHBOARD_SAMPLE_NAME || 'Python';
const projectName: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_PROJECT_NAME || 'python-hello-world';

suite(`"Start workspace with existed workspace name" test`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	let projectSection: ViewSection;
	let existedWorkspaceName: string;

	loginTests.loginIntoChe();

	test(`Create and open new workspace, stack:${stackName}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace(stackName);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Wait workspace readiness and project folder has been created', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		projectSection = await new SideBarView().getContent().getSection(projectName);
		const isFileImported: ViewItem | undefined = await projectSection.findItem(BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
		expect(isFileImported).not.eqls(undefined);
	});

	test(`Stop created workspace`, async function (): Promise<void> {
		existedWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		await workspaceHandlingTests.stopWorkspace(existedWorkspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});	

	test(`Create new workspace from the same ${stackName} stack`, async function (): Promise<void> {
		existedWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();

		await browserTabsUtil.navigateTo(BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL);
		await dashboard.waitPage();
		await workspaceHandlingTests.createAndOpenWorkspaceWithExistedWorkspaceName(stackName);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Wait the second workspace readiness and project folder has been created', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		projectSection = await new SideBarView().getContent().getSection(projectName);
		const isFileImported: ViewItem | undefined = await projectSection.findItem(BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
		expect(isFileImported).not.eqls(undefined);
	});

	test(`Stop all created ${stackName} workspaces`, async function (): Promise<void> {
		await workspaceHandlingTests.stopWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	test(`Delete all created ${stackName} workspaces`, async function (): Promise<void> {
		await workspaceHandlingTests.removeWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await workspaceHandlingTests.removeWorkspace(existedWorkspaceName);
	});

	loginTests.logoutFromChe();
});
