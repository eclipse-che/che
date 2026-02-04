/** *******************************************************************
 * copyright (c) 2020-2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { Workbench, ActivityBar, ViewControl } from 'monaco-page-objects';
import { CLASSES } from '../../configs/inversify.types';
import { expect } from 'chai';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { Logger } from '../../utils/Logger';

suite(`"Create Empty Workspace sample" test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);

	const stackName: string = 'Empty Workspace';

	let firstWorkspaceName: string;
	let secondWorkspaceName: string;

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	async function waitDashboardPage(): Promise<void> {
		await dashboard.openDashboard();
		await dashboard.waitPage();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		await dashboard.clickCreateWorkspaceButton();
		await createWorkspace.waitPage();
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked()).to.be.true;
	}

	async function waitWorkspaceReadiness(): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		const workbench: Workbench = new Workbench();
		const activityBar: ActivityBar = workbench.getActivityBar();
		const activityBarControls: ViewControl[] = await activityBar.getViewControls();

		Logger.debug('Editor sections:');
		for (const control of activityBarControls) {
			Logger.debug(`${await control.getTitle()}`);
		}
	}

	test('Verify "Create New" is on and a new workspace is created without warnings', async function (): Promise<void> {
		await waitDashboardPage();

		// verify "Create New" checkbox is checked by default
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked()).to.be.true;
		await workspaceHandlingTests.createAndOpenWorkspace(stackName, true);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await waitWorkspaceReadiness();

		firstWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
	});

	test('Verify "Create New" is off and a new workspace is created', async function (): Promise<void> {
		await waitDashboardPage();

		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked()).to.be.true;
		await workspaceHandlingTests.createAndOpenWorkspace(stackName, false);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await waitWorkspaceReadiness();

		secondWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();

		// empty Workspace sample generates unique names, so workspaces should have different names
		expect(WorkspaceHandlingTests.getWorkspaceName()).not.to.be.equal(firstWorkspaceName);
	});

	suiteTeardown('Stop and delete all created workspaces', async function (): Promise<void> {
		await workspaceHandlingTests.stopAndRemoveWorkspace(firstWorkspaceName);
		await workspaceHandlingTests.stopAndRemoveWorkspace(secondWorkspaceName);
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
