/** *******************************************************************
 * copyright (c) 2019-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { ViewSection } from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { expect } from 'chai';
import { OcpMainPage } from '../../pageobjects/openshift/OcpMainPage';
import { OcpImportFromGitPage } from '../../pageobjects/openshift/OcpImportFromGitPage';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { StringUtil } from '../../utils/StringUtil';
import { OcpApplicationPage } from '../../pageobjects/openshift/OcpApplicationPage';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { Logger } from '../../utils/Logger';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { ShellString } from 'shelljs';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { satisfies } from 'compare-versions';

suite(`DevConsole Integration ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	let ocpImportPage: OcpImportFromGitPage;
	let ocpApplicationPage: OcpApplicationPage;
	let parentGUID: string = '';
	let developerPerspectiveWasEnabled: boolean = false;
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);

	// test specific data
	const gitImportRepo: string = 'https://github.com/crw-qe/summit-lab-spring-music.git';
	const gitImportReference: string = 'pipeline';
	const projectLabel: string = 'app.openshift.io/runtime=spring';
	const projectName: string = 'devconsole-integration-test';

	suiteSetup('Create new empty project using ocp', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		// delete the test project on a cluster if it has not been deleted properly in the previous run
		const expectedProject: ShellString = shellExecutor.executeCommand(`oc get project ${projectName}`);
		if (expectedProject.stderr.length === 0) {
			kubernetesCommandLineToolsExecutor.deleteProject(projectName);
		}
		kubernetesCommandLineToolsExecutor.createProject(projectName);
	});

	// configure Developer perspective for OCP 4.19+
	suiteSetup('Configure Developer perspective for OCP 4.19+', function (): void {
		if (BASE_TEST_CONSTANTS.OCP_VERSION && satisfies(BASE_TEST_CONSTANTS.OCP_VERSION, '>=4.19')) {
			// check current state
			const checkState: ShellString = shellExecutor.executeCommand(
				'oc get console.operator.openshift.io/cluster -o jsonpath="{.spec.customization.perspectives[?(@.id==\'dev\')].visibility.state}" 2>/dev/null || echo "notfound"'
			);

			if (checkState.stdout.trim() === 'Enabled') {
				Logger.info('Developer perspective already enabled');
				return;
			}

			const enableResult: ShellString = shellExecutor.executeCommand(
				'oc patch consoles.operator.openshift.io cluster --type=merge -p \'{"spec":{"customization":{"perspectives":[{"id":"dev","visibility":{"state":"Enabled"}}]}}}\''
			);

			if (enableResult.code === 0) {
				developerPerspectiveWasEnabled = true;
				Logger.info('Developer perspective enabled - waiting 30s to take effect');
				shellExecutor.executeCommand('sleep 30');
			} else {
				Logger.error(`Failed to enable developer perspective: ${enableResult.stderr}`);
				throw new Error(`Failed to enable developer perspective: ${enableResult.stderr}`);
			}
		}
	});

	loginTests.loginIntoOcpConsole();

	test('Select test project and Developer role on DevConsole', async function (): Promise<void> {
		parentGUID = await browserTabsUtil.getCurrentWindowHandle();
		await ocpMainPage.selectDeveloperRole();
		await ocpMainPage.selectProject(projectName);
	});

	test('Open import from git project page', async function (): Promise<void> {
		ocpImportPage = await ocpMainPage.openImportFromGitPage();
	});

	test('Fill and submit import data', async function (): Promise<void> {
		ocpApplicationPage = await ocpImportPage.fitAndSubmitConfiguration(gitImportRepo, gitImportReference, projectLabel);
	});

	test('Wait until application creates', async function (): Promise<void> {
		await ocpApplicationPage.waitApplicationIcon();
	});

	test('Check if application has worked link "Open Source Code"', async function (): Promise<void> {
		await ocpApplicationPage.waitAndOpenEditSourceCodeIcon();
	});

	test('Login', async function (): Promise<void> {
		try {
			await dashboard.waitLoader(TIMEOUT_CONSTANTS.TS_WAIT_LOADER_PRESENCE_TIMEOUT);
		} catch (e) {
			await loginTests.loginIntoChe();
		}
	});

	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
		await createWorkspace.performTrustAuthorPopup();
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
	});

	test('Registering the running workspace', function (): void {
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Check if application source code opens in workspace', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Check if project and files imported', async function (): Promise<void> {
		const applicationSourceProjectName: string = StringUtil.getProjectNameFromGitUrl(gitImportRepo);
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		expect(
			await projectAndFileTests.getProjectTreeItem(projectSection, applicationSourceProjectName),
			'Project folder was not imported'
		).not.undefined;
		expect(
			await projectAndFileTests.getProjectTreeItem(projectSection, BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME),
			'Project files were not imported'
		).not.undefined;
	});

	test('Check redirection to DevSpaces from App launcher', async function (): Promise<void> {
		await browserTabsUtil.switchToWindow(parentGUID);
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await ocpMainPage.clickOnAppLauncherAndDevSpaceItem();
		await loginTests.loginIntoChe();
		await dashboard.waitPage();
	});

	suiteTeardown('Delete project using ocp', function (): void {
		kubernetesCommandLineToolsExecutor.workspaceName =
			WorkspaceHandlingTests.getWorkspaceName() !== '' ? WorkspaceHandlingTests.getWorkspaceName() : 'spring-music';
		try {
			kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
		} catch (err) {
			Logger.error(`Error while deleting workspace: ${err}`);
		}
		try {
			kubernetesCommandLineToolsExecutor.deleteProject(projectName);
		} catch (err) {
			Logger.error(`Cannot delete the project: ${err}`);
		}
	});

	// restore original console configuration
	suiteTeardown('Restore Developer perspective configuration', function (): void {
		if (BASE_TEST_CONSTANTS.OCP_VERSION && satisfies(BASE_TEST_CONSTANTS.OCP_VERSION, '>=4.19') && developerPerspectiveWasEnabled) {
			Logger.info('Restoring Developer perspective to disabled state');

			const restoreResult: ShellString = shellExecutor.executeCommand(
				'oc patch consoles.operator.openshift.io cluster --type=merge -p \'{"spec":{"customization":{"perspectives":[{"id":"dev","visibility":{"state":"Disabled"}}]}}}\''
			);

			if (restoreResult.code === 0) {
				Logger.info('Developer perspective restored to disabled');
			} else {
				Logger.warn(`Failed to restore developer perspective: ${restoreResult.stderr}`);
			}
		}
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
