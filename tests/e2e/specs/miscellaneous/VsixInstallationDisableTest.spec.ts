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

	const TEST_REPO_URL: string = 'https://github.com/RomanNikitenko/web-nodejs-sample/tree/install-from-vsix-disabled-7-100';
	const RESOURCES_PATH: string = path.join(__dirname, '../../../resources');
	const DEFAULT_EXTENSIONS: string[] = ['YAML'];
	const VSIX_FILE_NAME: string = 'redhat.vscode-yaml-1.17.0.vsix';

	// configMap related constants
	const CONFIG_MAP_NAMES: { VSIX_CONFIG: string; DEFAULT_EXTENSIONS: string } = {
		VSIX_CONFIG: 'vscode-editor-configurations',
		DEFAULT_EXTENSIONS: 'default-extensions'
	};
	const CONFIG_MAP_NAMESPACES: { VSIX_CONFIG: string; DEFAULT_EXTENSIONS: string } = {
		VSIX_CONFIG: 'openshift-devspaces',
		DEFAULT_EXTENSIONS: 'admin-devspaces'
	};
	const CONFIG_FILES: { ENABLE_VSIX: string; DEFAULT_EXTENSIONS: string; DISABLE_VSIX: string } = {
		DISABLE_VSIX: 'configmap-disable-vsix-installation.yaml',
		ENABLE_VSIX: 'configmap-enable-vsix-installation.yaml',
		DEFAULT_EXTENSIONS: 'default-extensions-configmap.yaml'
	};

	let workspaceName: string = '';

	/**
	 * checks if the "Install from VSIX" command is available in Command Palette
	 * @param expectedVisible - Whether the command should be visible or not
	 */
	async function checkCommandPaletteForVsixCommand(expectedVisible: boolean): Promise<void> {
		await commandPalette.openCommandPalette();
		await commandPalette.searchCommand('Install from VSIX');
		const commandAvailable: boolean = await commandPalette.isCommandVisible('Extensions: Install from VSIX...');
		await commandPalette.closeCommandPalette();

		expect(commandAvailable).to.equal(
			expectedVisible,
			`Command Palette should ${expectedVisible ? 'contain' : 'not contain'} Install from VSIX command`
		);
	}

	/**
	 * checks if the "Install from VSIX" action is available in Extensions view menu
	 * @param expectedVisible - Whether the action should be visible or not
	 */
	async function checkExtensionsMenuForVsixAction(expectedVisible: boolean): Promise<void> {
		await extensionsView.openExtensionsView();
		await extensionsView.openMoreActionsMenu();
		const extensionMenuAvailable: boolean = await extensionsView.isMoreActionsMenuItemVisible('Install from VSIX');
		await extensionsView.closeMoreActionsMenu();

		expect(extensionMenuAvailable).to.equal(
			expectedVisible,
			`Extensions view should ${expectedVisible ? 'contain' : 'not contain'} Install from VSIX action`
		);
	}

	/**
	 * checks if the "Install Extension VSIX" action is available in Explorer context menu
	 * @param expectedVisible - Whether the action should be visible or not
	 */
	async function checkExplorerContextMenuForVsixAction(expectedVisible: boolean): Promise<void> {
		await explorerView.openFileContextMenu(VSIX_FILE_NAME);
		const contextMenuAvailable: boolean = await explorerView.isContextMenuItemVisible('Install Extension VSIX');
		await explorerView.closeContextMenu();

		expect(contextMenuAvailable).to.equal(
			expectedVisible,
			`Explorer context menu should ${expectedVisible ? 'contain' : 'not contain'} Install Extension VSIX action`
		);
	}

	/**
	 * runs all UI checks for VSIX installation capability
	 * Verifies command palette, extensions menu, and explorer context menu
	 * @param expectedEnabled - Whether VSIX installation should be enabled or not
	 */
	async function checkVsixInstallationInUI(expectedEnabled: boolean): Promise<void> {
		Logger.info(`Checking VSIX installation capability in UI - expected enabled: ${expectedEnabled}`);

		await checkCommandPaletteForVsixCommand(expectedEnabled);
		await checkExtensionsMenuForVsixAction(expectedEnabled);
		await checkExplorerContextMenuForVsixAction(expectedEnabled);
	}

	/**
	 * verifies whether default VSIX extensions were auto-installed or not
	 * @param shouldBeInstalled - Whether the extensions should be installed or not
	 */
	async function checkDefaultExtensionsInstallation(shouldBeInstalled: boolean): Promise<void> {
		Logger.info(`Checking default VSIX extensions auto-installation - expected installed: ${shouldBeInstalled}`);

		await extensionsView.openExtensionsView();
		const installedExtensions: string[] = await extensionsView.getInstalledExtensionNames();

		Logger.debug(`Found installed extensions: ${installedExtensions.join(', ')}`);

		for (const extensionName of DEFAULT_EXTENSIONS) {
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
	 * applies a ConfigMap from a file to the cluster
	 * @param configFileName - Name of the ConfigMap file to apply
	 */
	function applyConfigMap(configFileName: string): void {
		const configPath: string = path.join(RESOURCES_PATH, configFileName);
		const configContent: string = fs.readFileSync(configPath, 'utf8');
		shellExecutor.executeCommand(`oc apply -f - <<EOF\n${configContent}\nEOF`);
	}

	/**
	 * deletes a ConfigMap from the cluster
	 * @param name - Name of the ConfigMap
	 * @param namespace - Namespace of the ConfigMap
	 */
	function deleteConfigMap(name: string, namespace: string): void {
		Logger.debug(`Deleting ConfigMap ${name} from namespace ${namespace}`);
		shellExecutor.executeCommand(`oc delete configmap ${name} -n ${namespace} --ignore-not-found=true`);
	}

	/**
	 * removes all ConfigMaps created by the test
	 */
	function cleanupConfigMaps(): void {
		deleteConfigMap(CONFIG_MAP_NAMES.VSIX_CONFIG, CONFIG_MAP_NAMESPACES.VSIX_CONFIG);
		deleteConfigMap(CONFIG_MAP_NAMES.DEFAULT_EXTENSIONS, CONFIG_MAP_NAMESPACES.DEFAULT_EXTENSIONS);
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
		applyConfigMap(CONFIG_FILES.DISABLE_VSIX);
		applyConfigMap(CONFIG_FILES.DEFAULT_EXTENSIONS);
	});

	test('Create and open workspace from Git repository', async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(TEST_REPO_URL);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Verify VSIX disabled notifications', async function (): Promise<void> {
		const hasDisableNotification: boolean = await notificationHandler.checkForNotification('install from vsix command is disabled');
		expect(hasDisableNotification).to.be.true;
	});

	test('Perform trust author dialog', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify VSIX installation is disabled in UI', async function (): Promise<void> {
		await checkVsixInstallationInUI(false);
	});

	test('Verify default extensions are not auto-installed when VSIX disabled', async function (): Promise<void> {
		await checkDefaultExtensionsInstallation(false);
	});

	test('Enable VSIX installation and create new workspace', async function (): Promise<void> {
		// clean up current workspace
		await dashboard.openDashboard();
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		registerRunningWorkspace('');

		// apply new ConfigMaps
		applyConfigMap(CONFIG_FILES.ENABLE_VSIX);
		applyConfigMap(CONFIG_FILES.DEFAULT_EXTENSIONS);

		Logger.info('Waiting for new ConfigMap settings to take effect...');
		await driverHelper.wait(30000);

		// create new workspace
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(TEST_REPO_URL);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Verify default extension installation success notifications', async function (): Promise<void> {
		const successTexts: string[] = ['Completed installing extension', 'installed', 'extension'];
		const hasSuccessNotification: boolean = await notificationHandler.checkForAnyNotification(successTexts);
		expect(hasSuccessNotification).to.be.true;
	});

	test('Perform trust author dialog', async function (): Promise<void> {
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Verify VSIX installation is enabled in UI', async function (): Promise<void> {
		await checkVsixInstallationInUI(true);
	});

	test('Verify default extensions are auto-installed when VSIX enabled', async function (): Promise<void> {
		await checkDefaultExtensionsInstallation(true);
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
