/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { expect } from 'chai';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { WorkspaceDetails } from '../../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

const stackName: string = BASE_TEST_CONSTANTS.TS_SELENIUM_DASHBOARD_SAMPLE_NAME || 'Empty Workspace';
const RENAMED_WORKSPACE_NAME: string = 'new-ws';

suite(`Rename workspace ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const workspaceDetails: WorkspaceDetails = e2eContainer.get(CLASSES.WorkspaceDetails);

	let firstWorkspaceName: string = '';
	let secondWorkspaceName: string = '';

	async function openDashboardWorkspacesList(): Promise<void> {
		await dashboard.openDashboard();
		await dashboard.clickWorkspacesButton();
		await workspaces.waitPage();
	}

	async function openWorkspaceDetailsOverview(workspaceName: string): Promise<void> {
		await openDashboardWorkspacesList();
		await workspaces.clickWorkspaceListItemLink(workspaceName);
		await workspaceDetails.waitWorkspaceTitle(workspaceName);
		await workspaceDetails.waitLoaderDisappearance();
	}

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test(`Create workspace from sample (${stackName})`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace(stackName);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		firstWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(firstWorkspaceName);

		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Workspace details: rename must not be available while the workspace is running', async function (): Promise<void> {
		await openWorkspaceDetailsOverview(firstWorkspaceName);
		await workspaceDetails.waitRenameWorkspaceNotPossibleWhileWorkspaceRunning();
	});

	test('Stop the first workspace from the dashboard', async function (): Promise<void> {
		await workspaceHandlingTests.stopWorkspace(firstWorkspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	test(`Rename stopped workspace to "${RENAMED_WORKSPACE_NAME}" from workspace details`, async function (): Promise<void> {
		await openWorkspaceDetailsOverview(firstWorkspaceName);
		await workspaceDetails.renameStoppedWorkspaceTo(RENAMED_WORKSPACE_NAME);
		firstWorkspaceName = RENAMED_WORKSPACE_NAME;
	});

	test(`Dashboard lists and details show "${RENAMED_WORKSPACE_NAME}"`, async function (): Promise<void> {
		await openDashboardWorkspacesList();
		await workspaces.waitWorkspaceListItem(RENAMED_WORKSPACE_NAME, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
		await workspaces.clickWorkspaceListItemLink(RENAMED_WORKSPACE_NAME);
		await workspaceDetails.waitWorkspaceTitle(RENAMED_WORKSPACE_NAME);
	});

	test(`Start "${RENAMED_WORKSPACE_NAME}" and wait until it is Running`, async function (): Promise<void> {
		await openDashboardWorkspacesList();

		await workspaceHandlingTests.openWorkspace(RENAMED_WORKSPACE_NAME);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		const startedWorkspaceName: string = WorkspaceHandlingTests.getWorkspaceName();
		expect(WorkspaceHandlingTests.getWorkspaceName()).equal(RENAMED_WORKSPACE_NAME);
		registerRunningWorkspace(startedWorkspaceName);
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test(`Stop "${RENAMED_WORKSPACE_NAME}" again`, async function (): Promise<void> {
		await workspaceHandlingTests.stopWorkspace(RENAMED_WORKSPACE_NAME);
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	test(`Workspace details: setting name to "${RENAMED_WORKSPACE_NAME}" when it is already the current name is rejected`, async function (): Promise<void> {
		await openWorkspaceDetailsOverview(RENAMED_WORKSPACE_NAME);
		await workspaceDetails.attemptRenameWorkspaceName(RENAMED_WORKSPACE_NAME);
	});

	test(`Create a second workspace from sample (${stackName})`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace(stackName);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		secondWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(secondWorkspaceName);
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test(`Second workspace details: rename to "${RENAMED_WORKSPACE_NAME}" shows conflict and does not apply`, async function (): Promise<void> {
		await workspaceHandlingTests.stopWorkspace(secondWorkspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();

		await openWorkspaceDetailsOverview(secondWorkspaceName);
		await workspaceDetails.attemptRenameWorkspaceName(RENAMED_WORKSPACE_NAME);
	});

	suiteTeardown('Stop and delete workspaces created in this suite', async function (): Promise<void> {
		await openDashboardWorkspacesList();

		await dashboard.deleteStoppedWorkspaceByUI(secondWorkspaceName);
		await dashboard.deleteStoppedWorkspaceByUI(RENAMED_WORKSPACE_NAME);
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
