/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { SideBarView, ViewItem, ViewSection } from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { expect } from 'chai';
import { OcpMainPage } from '../../pageobjects/openshift/OcpMainPage';
import { OcpImportFromGitPage } from '../../pageobjects/openshift/OcpImportFromGitPage';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { StringUtil } from '../../utils/StringUtil';
import { OcpApplicationPage } from '../../pageobjects/openshift/OcpApplicationPage';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

suite('DevConsole Integration', function (): void {
	let ocpImportPage: OcpImportFromGitPage;
	let ocpApplicationPage: OcpApplicationPage;

	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	// test specific data
	const gitImportRepo: string = 'https://github.com/crw-qe/summit-lab-spring-music.git';
	const gitImportReference: string = 'pipeline';
	const projectLabel: string = 'app.openshift.io/runtime=spring';
	const projectName: string = 'devconsole-integration-test';

	suiteSetup('Create new empty project using ocp', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.createProject(projectName);
	});

	loginTests.loginIntoOcpConsole();

	test('Select test project and Developer role on DevConsole', async function (): Promise<void> {
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

	loginTests.loginIntoChe();

	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
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
		const projectSection: ViewSection = await new SideBarView().getContent().getSection(applicationSourceProjectName);
		const isFileImported: ViewItem | undefined = await projectSection.findItem(BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
		expect(isFileImported).not.eqls(undefined);
	});

	test('Stop and delete the workspace by API', async function (): Promise<void> {
		await browserTabsUtil.closeAllTabsExceptCurrent();
		testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
	});

	loginTests.logoutFromChe();

	suiteTeardown('Delete project using ocp', function (): void {
		kubernetesCommandLineToolsExecutor.deleteProject(projectName);
	});
});
