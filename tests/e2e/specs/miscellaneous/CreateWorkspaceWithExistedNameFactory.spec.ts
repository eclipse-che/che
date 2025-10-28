/** *******************************************************************
 * copyright (c) 2020-2025 Red Hat, Inc.
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
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

const projectName: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_PROJECT_NAME || 'python-hello-world';

suite(`"Start workspace with existed workspace name" test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);

	let projectSection: ViewSection;
	let firstWorkspaceName: string;
	let secondWorkspaceName: string;
	const factoryUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? FACTORY_TEST_CONSTANTS.TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL || 'https://gh.crw-qe.com/test-automation-only/python-hello-world'
		: FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://github.com/crw-qe/python-hello-world';

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
		const originalWindowHandle: string = await browserTabsUtil.getCurrentWindowHandle();
		await browserTabsUtil.waitAndSwitchToAnotherWindow(originalWindowHandle, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());

		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		projectSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not.undefined;
		expect(
			await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
			'Project files were not imported'
		).not.undefined;
	}

	test('Verify "Create New" is off and a new workspace is created', async function (): Promise<void> {
		await waitDashboardPage();

		await createWorkspace.setGitRepositoryUrl(factoryUrl);
		expect(await createWorkspace.isCreateNewWorkspaceCheckboxChecked()).to.be.false;
		expect(await createWorkspace.getGitRepositoryUrl()).to.be.equal(factoryUrl);
		await createWorkspace.clickOnCreateAndOpenButton();
		await createWorkspace.performTrustAuthorPopup();

		await waitWorkspaceReadiness();

		firstWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
	});

	test('Verify existing workspace opens instead of creating a duplicate', async function (): Promise<void> {
		await waitDashboardPage();

		await createWorkspace.setGitRepositoryUrl(factoryUrl);
		expect(await createWorkspace.getGitRepositoryUrl()).to.be.equal(factoryUrl);
		await createWorkspace.clickOnCreateAndOpenButton();
		await createWorkspace.performTrustAuthorPopup();

		await waitWorkspaceReadiness();

		expect(WorkspaceHandlingTests.getWorkspaceName()).to.be.equal(firstWorkspaceName);
	});

	test('Ensure `policies.create=perclick` is appended and a new workspace is created without warnings', async function (): Promise<void> {
		await waitDashboardPage();

		await createWorkspace.setGitRepositoryUrl(factoryUrl);
		await createWorkspace.setCreateNewWorkspaceCheckbox(true);
		expect(await createWorkspace.getGitRepositoryUrl()).to.be.equal(factoryUrl + '?policies.create=perclick');

		await createWorkspace.clickOnCreateAndOpenButton();
		await createWorkspace.performTrustAuthorPopup();

		await waitWorkspaceReadiness();

		secondWorkspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(secondWorkspaceName).to.have.string(firstWorkspaceName);
		expect(secondWorkspaceName.length).to.be.equal(firstWorkspaceName.length + 5);
	});

	test('Verify warning appears with list of existing workspaces and open the first created workspace', async function (): Promise<void> {
		await waitDashboardPage();

		await createWorkspace.setGitRepositoryUrl(factoryUrl);
		await createWorkspace.clickOnCreateAndOpenButton();
		await createWorkspace.performTrustAuthorPopup();
		const originalWindowHandle: string = await browserTabsUtil.getCurrentWindowHandle();
		await browserTabsUtil.waitAndSwitchToAnotherWindow(originalWindowHandle, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);

		await dashboard.waitExistingWorkspaceFoundAlert();
		await dashboard.startExistedWorkspace(firstWorkspaceName);

		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());

		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		projectSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not.undefined;
		expect(
			await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
			'Project files were not imported'
		).not.undefined;
	});

	suiteTeardown(`Stop and delete all created workspaces`, async function (): Promise<void> {
		await workspaceHandlingTests.stopAndRemoveWorkspace(firstWorkspaceName);
		await workspaceHandlingTests.stopAndRemoveWorkspace(secondWorkspaceName);
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
