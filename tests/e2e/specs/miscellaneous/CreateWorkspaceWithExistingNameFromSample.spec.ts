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
import { ViewSection } from 'monaco-page-objects';
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
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

const sampleName: string = 'Java Lombok';

suite(`"Create workspace from sample with existing name" test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);

	let projectSection: ViewSection;
	let firstWorkspaceName: string;
	let secondWorkspaceName: string;

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
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());

		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		projectSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, 'lombok-project-sample'), 'Project folder was not imported').not
			.undefined;
		expect(
			await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
			'Project files were not imported'
		).not.undefined;
	}

	test('Verify "Create New" is on by default and create first workspace from sample', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// verify "Create New" checkbox is checked by default
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be checked by default').to.be
			.true;

		// create workspace from sample
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await createWorkspace.clickOnSampleNoEditorSelection(sampleName);
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		await waitWorkspaceReadiness();
		firstWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(firstWorkspaceName, 'First workspace name should not be empty').not.empty;
	});

	test('Turn off "Create New" and verify existing workspace opens', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// turn off "Create New" checkbox
		await createWorkspace.setCreateNewWorkspaceCheckbox(false);
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be unchecked').to.be.false;

		// click on the same sample
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await createWorkspace.clickOnSampleNoEditorSelection(sampleName);
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		await waitWorkspaceReadiness();

		// verify the same workspace was opened (not a new one)
		expect(WorkspaceHandlingTests.getWorkspaceName(), 'The existing workspace should be opened').to.be.equal(firstWorkspaceName);
	});

	test('Turn on "Create New" and verify new workspace is created without warnings', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// turn on "Create New" checkbox
		await createWorkspace.setCreateNewWorkspaceCheckbox(true);
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be checked').to.be.true;

		// click on the same sample
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await createWorkspace.clickOnSampleNoEditorSelection(sampleName);
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		await waitWorkspaceReadiness();

		secondWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(secondWorkspaceName, 'Second workspace name should not be empty').not.empty;
		expect(secondWorkspaceName, 'A new workspace with different name should be created').not.to.be.equal(firstWorkspaceName);
	});

	test('Turn off "Create New", verify warning with workspace list, and select second workspace', async function (): Promise<void> {
		await openCreateWorkspacePage();

		// turn off "Create New" checkbox
		await createWorkspace.setCreateNewWorkspaceCheckbox(false);
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked(), '"Create New" checkbox should be unchecked').to.be.false;

		// click on the same sample
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await createWorkspace.clickOnSampleNoEditorSelection(sampleName);
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		// wait for existing workspace found alert with list of workspaces
		await dashboard.waitExistingWorkspaceFoundAlert();

		// select the second workspace (created at step 4)
		await dashboard.startExistedWorkspace(secondWorkspaceName);

		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());

		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();

		// verify the second workspace was opened
		expect(WorkspaceHandlingTests.getWorkspaceName(), 'The second workspace should be opened').to.be.equal(secondWorkspaceName);
	});

	suiteTeardown('Stop and delete all created workspaces', async function (): Promise<void> {
		await workspaceHandlingTests.stopAndRemoveWorkspace(firstWorkspaceName);
		await workspaceHandlingTests.stopAndRemoveWorkspace(secondWorkspaceName);
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
