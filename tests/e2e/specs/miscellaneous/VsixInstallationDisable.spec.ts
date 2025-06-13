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
import { NotificationHandler } from '../../pageobjects/ide/NotificationHandler';
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
	const notificationHandler: NotificationHandler = e2eContainer.get(CLASSES.NotificationHandler);
	const commandPalette: CommandPalette = e2eContainer.get(CLASSES.CommandPalette);
	const extensionsView: ExtensionsView = e2eContainer.get(CLASSES.ExtensionsView);
	const explorerView: ExplorerView = e2eContainer.get(CLASSES.ExplorerView);

	// test configuration
	const testRepoUrl: string = 'https://github.com/RomanNikitenko/web-nodejs-sample/tree/install-from-vsix-disabled-7-100';
	const resourcesPath: string = path.join(__dirname, '../../../resources');
	const defaultExtensions: string[] = ['YAML'];

	/**
	 * verify VSIX installation capability in UI locations
	 */
	async function verifyVsixInstallationCapability(expectedEnabled: boolean): Promise<void> {
		Logger.info(`Verifying VSIX installation capability - expected enabled: ${expectedEnabled}`);

		// check Command Palette
		await commandPalette.openCommandPalette();
		await commandPalette.searchCommand('Install from VSIX');
		const commandAvailable: boolean = await commandPalette.isCommandVisible('Extensions: Install from VSIX...');
		await commandPalette.closeCommandPalette();

		expect(commandAvailable).to.equal(
			expectedEnabled,
			`Command Palette should ${expectedEnabled ? 'contain' : 'not contain'} Install from VSIX command`
		);

		// check Extensions View More Actions Menu
		await extensionsView.openExtensionsView();
		await extensionsView.openMoreActionsMenu();
		const extensionMenuAvailable: boolean = await extensionsView.isMoreActionsMenuItemVisible('Install from VSIX');
		await extensionsView.closeMoreActionsMenu();

		expect(extensionMenuAvailable).to.equal(
			expectedEnabled,
			`Extensions view should ${expectedEnabled ? 'contain' : 'not contain'} Install from VSIX action`
		);

		// check Explorer Context Menu
		const vsixFileName: string = 'redhat.vscode-yaml-1.17.0.vsix';
		await explorerView.openFileContextMenu(vsixFileName);
		const contextMenuAvailable: boolean = await explorerView.isContextMenuItemVisible('Install Extension VSIX');
		await explorerView.closeContextMenu();

		expect(contextMenuAvailable).to.equal(
			expectedEnabled,
			`Explorer context menu should ${expectedEnabled ? 'contain' : 'not contain'} Install Extension VSIX action`
		);
	}

	let workspaceName: string = '';

	/**
	 * verify default extensions installation status
	 */
	async function verifyDefaultExtensionsInstallation(shouldBeInstalled: boolean): Promise<void> {
		Logger.info(`Verifying default VSIX extensions auto-installation - expected installed: ${shouldBeInstalled}`);

		await extensionsView.openExtensionsView();
		const installedExtensions: string[] = await extensionsView.getInstalledExtensionNames();

		Logger.debug(`Found installed extensions: ${installedExtensions.join(', ')}`);

		for (const extensionName of defaultExtensions) {
			const isInstalled: boolean = installedExtensions.some((installed: string): boolean =>
				installed.toLowerCase().includes(extensionName.toLowerCase())
			);

			expect(isInstalled).to.equal(
				shouldBeInstalled,
				`Default VSIX extension "${extensionName}" should ${shouldBeInstalled ? 'be auto-installed' : 'not be auto-installed'}`
			);
		}
	}

	/**
	 * apply a ConfigMap from resources folder
	 */
	function applyConfigMap(configFileName: string): void {
		const configPath: string = path.join(resourcesPath, configFileName);
		const configContent: string = fs.readFileSync(configPath, 'utf8');
		shellExecutor.executeCommand(`oc apply -f - <<EOF\n${configContent}\nEOF`);
	}

	/**
	 * delete a ConfigMap
	 */
	function deleteConfigMap(name: string, namespace: string): void {
		Logger.debug(`Deleting ConfigMap ${name} from namespace ${namespace}`);
		shellExecutor.executeCommand(`oc delete configmap ${name} -n ${namespace} --ignore-not-found=true`);
	}

	/**
	 * clean up all test-related ConfigMaps
	 */
	function cleanupConfigMaps(): void {
		deleteConfigMap('vscode-editor-configurations', 'openshift-devspaces');
		deleteConfigMap('default-extensions', 'admin-devspaces');
	}

	// ========== Test Execution ==========

	suiteSetup('Login to cluster and setup initial state', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		cleanupConfigMaps();
	});

	suiteSetup('Login to application', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Apply ConfigMaps that disable VSIX and set default extensions', function (): void {
		applyConfigMap('configmap-disable-vsix-installation.yaml');
		applyConfigMap('default-extensions-configmap.yaml');
	});

	test('Create and open workspace from Git repository', async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(testRepoUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Verify for VSIX disabled notifications', async function (): Promise<void> {
		const hasDisableNotification: boolean = await notificationHandler.checkForNotification('install from vsix command is disabled');
		expect(hasDisableNotification).to.be.true;
	});

	test('Perform trust author dialog', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify VSIX installation is disabled', async function (): Promise<void> {
		await verifyVsixInstallationCapability(false);
	});

	test('Verify default extensions are not auto-installed when VSIX disabled', async function (): Promise<void> {
		await verifyDefaultExtensionsInstallation(false);
	});

	test('Enable VSIX installation and create new workspace', async function (): Promise<void> {
		// clean up current workspace
		await dashboard.openDashboard();
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		registerRunningWorkspace('');

		// apply new ConfigMaps
		applyConfigMap('configmap-enable-vsix-installation.yaml');
		applyConfigMap('default-extensions-configmap.yaml');

		Logger.info('Waiting for new ConfigMap settings to take effect...');
		await driverHelper.wait(30000);

		// create new workspace
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(testRepoUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Verify default extension installation success notifications before trust dialog', async function (): Promise<void> {
		const successTexts: string[] = ['Completed installing extension', 'installed', 'extension'];
		const hasSuccessNotification: boolean = await notificationHandler.checkForAnyNotification(successTexts);
		expect(hasSuccessNotification).to.be.true;
	});

	test('Perform trust author dialog', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify VSIX installation is enabled', async function (): Promise<void> {
		await verifyVsixInstallationCapability(true);
	});

	test('Verify default extensions are auto-installed when VSIX enabled', async function (): Promise<void> {
		await verifyDefaultExtensionsInstallation(true);
	});

	suiteTeardown('Clean up ConfigMaps and workspace', async function (): Promise<void> {
		cleanupConfigMaps();
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		registerRunningWorkspace('');
		Logger.info('Cleanup completed');
	});
});
