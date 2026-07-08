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
import { ViewSection } from 'monaco-page-objects';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { expect } from 'chai';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Logger } from '../../utils/Logger';
import { StringUtil } from '../../utils/StringUtil';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellString } from 'shelljs';
import { WorkspaceDetails } from '../../pageobjects/dashboard/workspace-details/WorkspaceDetails';

const factoryUrl: string =
	FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://gh.crw-qe.com/test-automation-only/python-hello-world.git';
const testFileName: string = 'backup-test.txt';
const testFileContent: string = 'This is a test file for backup verification';

suite(`"Restore workspace from backup" test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const workspaceDetails: WorkspaceDetails = e2eContainer.get(CLASSES.WorkspaceDetails);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	const workspaceName2: string = 'test-workspace-2';

	let projectSection: ViewSection;
	let workspaceName: string;
	let backupImageUrl: string;
	let projectName: string;

	async function openWorkspaceDetailsBackup(workspaceName: string): Promise<void> {
		await workspaces.clickWorkspaceListItemLink(workspaceName);
		await workspaceDetails.waitWorkspaceTitle(workspaceName);
		await workspaceDetails.waitLoaderDisappearance();
		await workspaceDetails.selectTab('Backup');
	}

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});
	test(`Create and open new workspace from factory:${factoryUrl}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});

	test('Wait workspace readiness and project folder has been created', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});
	test('Check a project folder has been created', async function (): Promise<void> {
		projectName = FACTORY_TEST_CONSTANTS.TS_SELENIUM_PROJECT_NAME || StringUtil.getProjectNameFromGitUrl(factoryUrl);
		projectSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not imported').not.undefined;
		await projectAndFileTests.performTrustDialogs();
	});
	test('Setup workspace context for API operations', function (): void {
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});
	test('Create test file via API', function (): void {
		Logger.debug(`Creating test file: /projects/${projectName}/${testFileName}`);
		const createFileCommand: string = `echo "${testFileContent}" > /projects/${projectName}/${testFileName}`;
		const output: ShellString = containerTerminal.execInContainerCommand(createFileCommand);
		Logger.debug(`File creation output: ${output.stdout}`);
		expect(output.code).to.equal(0);
	});
	test('Verify test file content via API', function (): void {
		Logger.debug(`Verifying test file content: /projects/${projectName}/${testFileName}`);
		const readFileCommand: string = `cat /projects/${projectName}/${testFileName}`;
		const output: ShellString = containerTerminal.execInContainerCommand(readFileCommand);
		Logger.debug(`File content: ${output.stdout}`);
		expect(output.stdout.trim()).to.equal(testFileContent);
	});
	test('Stop the workspace', async function (): Promise<void> {
		expect(workspaceName, 'Workspace name not available').not.empty;
		await dashboard.openDashboard();
		await dashboard.waitPage();
		await dashboard.stopWorkspaceByUI(workspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await workspaces.waitWorkspaceWithStoppedStatus(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Never');
	});
	test('Wait for backup completion', async function (): Promise<void> {
		await workspaces.waitWorkspaceListItem(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Success');
	});
	test('Get backup image URL', async function (): Promise<void> {
		await openWorkspaceDetailsBackup(workspaceName);
		backupImageUrl = await workspaces.getBackupImageUrlValue();
		Logger.info(`Retrieved backup image URL: ${backupImageUrl}`);
	});
	test('Delete the workspace', async function (): Promise<void> {
		await dashboard.deleteStoppedWorkspaceByUI(workspaceName);
	});
	test('Open backups page', async function (): Promise<void> {
		await workspaces.openBackupsPage();
		await workspaces.waitWorkspaceListItem(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Success');
	});
	test('Restore workspace from default registry', async function (): Promise<void> {
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await workspaces.clickCreateFromBackupButton(workspaceName);
		await workspaces.restoreWorkspaceFromDefaultRegistry();
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	});
	test('Obtain workspace name after restore', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		const obtainedName: string = WorkspaceHandlingTests.getWorkspaceName();
		Logger.info(`Obtained workspace name after first restore: '${obtainedName}'`);
	});
	test('Register restored workspace', function (): void {
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});
	test('Wait workspace readiness after restore', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustDialogs();
		projectSection = await projectAndFileTests.getProjectViewSession();
	});
	test('Setup workspace context for API operations after restore', function (): void {
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		Logger.info(`Workspace name for API setup after first restore: '${workspaceName}'`);
		expect(workspaceName, 'Workspace name should not be empty after restore').not.empty;
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});
	test('Verify project folder exists after restore', async function (): Promise<void> {
		await projectAndFileTests.expandProjectTreeItem(projectSection, projectName);
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not restored').not.undefined;
	});
	test('Verify test file content is intact after restore via API', function (): void {
		Logger.debug(`Verifying restored file content: /projects/${projectName}/${testFileName}`);
		const readFileCommand: string = `cat /projects/${projectName}/${testFileName}`;
		const output: ShellString = containerTerminal.execInContainerCommand(readFileCommand);
		Logger.debug(`Restored file content: ${output.stdout}`);
		expect(output.stdout.trim()).to.equal(testFileContent);
	});
	test('Delete the workspace', async function (): Promise<void> {
		await dashboard.openDashboard();
		await dashboard.waitPage();
		await dashboard.deleteStoppedWorkspaceByUI(workspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});
	test('Open backups page', async function (): Promise<void> {
		await workspaces.openBackupsPage();
		await workspaces.waitWorkspaceListItem(workspaceName);
		await workspaces.waitBackupStatus(workspaceName, 'Success');
	});
	test('Restore workspace from backup image URL ', async function (): Promise<void> {
		const parentGUID: string = await browserTabsUtil.getCurrentWindowHandle();
		await workspaces.clickCreateFromBackupButton(workspaceName);
		await workspaces.restoreWorkspaceFromExternalRegistry(backupImageUrl, workspaceName2);
		await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
	});
	test('Obtain workspace name after second restore', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		const obtainedName: string = WorkspaceHandlingTests.getWorkspaceName();
		Logger.info(`Obtained workspace name after second restore: '${obtainedName}'`);
	});
	test('Register second restored workspace', function (): void {
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
	});
	test('Wait workspace readiness after second restore', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustDialogs();
		projectSection = await projectAndFileTests.getProjectViewSession();
	});
	test('Setup workspace context for API operations after second restore', function (): void {
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		Logger.info(`Workspace name for API setup after second restore: '${workspaceName}'`);
		expect(workspaceName, 'Workspace name should not be empty after second restore').not.empty;
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});
	test('Verify project folder exists after second restore', async function (): Promise<void> {
		await projectAndFileTests.expandProjectTreeItem(projectSection, projectName);
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, projectName), 'Project folder was not restored').not.undefined;
	});
	test('Verify test file content is intact after second restore via API', function (): void {
		Logger.debug(`Verifying restored file content: /projects/${projectName}/${testFileName}`);
		const readFileCommand: string = `cat /projects/${projectName}/${testFileName}`;
		const output: ShellString = containerTerminal.execInContainerCommand(readFileCommand);
		Logger.debug(`Restored file content: ${output.stdout}`);
		expect(output.stdout.trim()).to.equal(testFileContent);
	});
	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});
	suiteTeardown('Stop and delete workspace by API', async function (): Promise<void> {
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
	});
	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
