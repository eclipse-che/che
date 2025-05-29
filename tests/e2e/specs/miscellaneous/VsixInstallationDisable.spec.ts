/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { expect } from 'chai';
import * as fs from 'fs';
import * as path from 'path';

import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';

import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';

import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { CommandPalette } from '../../pageobjects/ide/CommandPalette';
import { ExtensionsView } from '../../pageobjects/ide/ExtensionsView';
import { ExplorerView } from '../../pageobjects/ide/ExplorerView';

import { DriverHelper } from '../../utils/DriverHelper';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { Logger } from '../../utils/Logger';

suite(`Verify VSIX installation can be disabled via configuration ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const commandPalette: CommandPalette = e2eContainer.get(CLASSES.CommandPalette);
	const extensionsView: ExtensionsView = e2eContainer.get(CLASSES.ExtensionsView);
	const explorerView: ExplorerView = e2eContainer.get(CLASSES.ExplorerView);

	const testRepoUrl: string = 'https://github.com/RomanNikitenko/web-nodejs-sample/tree/install-from-vsix-disabled-7-100';
	const vsixFileName: string = 'redhat.vscode-yaml-1.17.0.vsix';
	const configMapNamespace: string = 'openshift-devspaces';
	const configMapName: string = 'vscode-editor-configurations';
	const resourcesPath: string = path.join(__dirname, '../../../resources');

	const INSTALL_FROM_VSIX_TEXT: string = 'Install from VSIX';
	const INSTALL_EXTENSION_VSIX_TEXT: string = 'Install Extension VSIX';

	let workspaceName: string = '';

	function getConfigMapPath(filename: string): string {
		return path.join(resourcesPath, filename);
	}

	function applyConfigMap(configFile: string): void {
		const configPath: string = getConfigMapPath(configFile);
		const configContent: string = fs.readFileSync(configPath, 'utf8');
		shellExecutor.executeCommand(`oc apply -f - <<EOF\n${configContent}\nEOF`);
	}

	async function checkVsixInstallationCapability(vsixInstallationEnabled: boolean): Promise<void> {
		Logger.info(`Checking VSIX installation capability - expected enabled: ${vsixInstallationEnabled}`);

		const commandAvailable: boolean = await commandPalette.searchAndCheckCommand(INSTALL_FROM_VSIX_TEXT);
		expect(commandAvailable).to.equal(
			vsixInstallationEnabled,
			`Command Palette should ${vsixInstallationEnabled ? 'contain' : 'not contain'} Install from VSIX command`
		);

		const extensionMenuAvailable: boolean = await extensionsView.checkForMenuItem(INSTALL_FROM_VSIX_TEXT);
		expect(extensionMenuAvailable).to.equal(
			vsixInstallationEnabled,
			`Extensions view should ${vsixInstallationEnabled ? 'contain' : 'not contain'} Install from VSIX action`
		);

		const contextMenuAvailable: boolean = await explorerView.checkFileContextMenuItem(vsixFileName, INSTALL_EXTENSION_VSIX_TEXT);
		expect(contextMenuAvailable).to.equal(
			vsixInstallationEnabled,
			`Explorer context menu should ${vsixInstallationEnabled ? 'contain' : 'not contain'} Install Extension VSIX action`
		);
	}

	suiteSetup('Apply ConfigMap that disables VSIX installation', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		applyConfigMap('configmap-disable-vsix-installation.yaml');
	});

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Create and open workspace from Git repository', async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(testRepoUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify VSIX installation is disabled', async function (): Promise<void> {
		await checkVsixInstallationCapability(false);
	});

	test('Enable VSIX installation and verify functionality returns', async function (): Promise<void> {
		applyConfigMap('configmap-enable-vsix-installation.yaml');

		await dashboard.openDashboard();
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		registerRunningWorkspace('');

		Logger.info('Waiting for new ConfigMap settings to take effect...');
		await driverHelper.wait(15000);

		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(testRepoUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);

		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustAuthorDialog();

		await checkVsixInstallationCapability(true);
	});

	suiteTeardown('Clean up ConfigMap', function (): void {
		shellExecutor.executeCommand(`oc delete configmap ${configMapName} -n ${configMapNamespace} --ignore-not-found=true`);
		Logger.info('ConfigMap deleted');
	});

	suiteTeardown('Cleanup workspace', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		registerRunningWorkspace('');
	});
});
