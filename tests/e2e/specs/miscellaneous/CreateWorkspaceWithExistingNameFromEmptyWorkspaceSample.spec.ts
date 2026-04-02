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
	let thirdWorkspaceName: string;

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	async function openCreateWorkspacePage(): Promise<void> {
		await dashboard.openDashboard();
		await dashboard.waitPage();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		await dashboard.clickCreateWorkspaceButton();
		await createWorkspace.waitPage();
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

	test('Step 1-2: Verify "Create New" is ON by default and create first Empty Workspace', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// verify "Create New" checkbox is checked by default
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be checked by default').to.be
			.true;

		await workspaceHandlingTests.createAndOpenWorkspace(stackName, true);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		firstWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(firstWorkspaceName, 'First workspace name should not be empty').not.empty;
		registerRunningWorkspace(firstWorkspaceName);

		await waitWorkspaceReadiness();
	});

	test('Step 3: Create second Empty Workspace with "Create New" ON - should succeed without warnings', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// verify "Create New" checkbox is still checked
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be checked').to.be.true;

		await workspaceHandlingTests.createAndOpenWorkspace(stackName, true);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		secondWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(secondWorkspaceName, 'Second workspace name should not be empty').not.empty;
		registerRunningWorkspace(secondWorkspaceName);

		await waitWorkspaceReadiness();

		// empty Workspace sample generates unique names, so workspaces should have different names
		expect(secondWorkspaceName, 'Second workspace should have a different name than the first one').not.to.be.equal(firstWorkspaceName);
	});

	test('Step 4-5: Turn OFF "Create New" and create third Empty Workspace - should succeed without warnings', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// turn off "Create New" checkbox
		await createWorkspace.setCreateNewWorkspaceCheckbox(false);
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be unchecked').to.be.false;

		await workspaceHandlingTests.createAndOpenWorkspace(stackName, false);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		thirdWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(thirdWorkspaceName, 'Third workspace name should not be empty').not.empty;
		registerRunningWorkspace(thirdWorkspaceName);

		await waitWorkspaceReadiness();

		// empty Workspace sample generates unique names, so all workspaces should have different names
		expect(thirdWorkspaceName, 'Third workspace should have a different name than the first one').not.to.be.equal(firstWorkspaceName);
		expect(thirdWorkspaceName, 'Third workspace should have a different name than the second one').not.to.be.equal(secondWorkspaceName);
	});

	suiteTeardown('Stop and delete all created workspaces', async function (): Promise<void> {
		if (firstWorkspaceName) {
			await workspaceHandlingTests.stopAndRemoveWorkspace(firstWorkspaceName);
		}
		if (secondWorkspaceName) {
			await workspaceHandlingTests.stopAndRemoveWorkspace(secondWorkspaceName);
		}
		if (thirdWorkspaceName) {
			await workspaceHandlingTests.stopAndRemoveWorkspace(thirdWorkspaceName);
		}
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
