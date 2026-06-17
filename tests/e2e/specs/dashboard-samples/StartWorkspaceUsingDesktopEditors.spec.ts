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
import { CLASSES } from '../../configs/inversify.types';
import { LoginTests } from '../../tests-library/LoginTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { expect } from 'chai';
import { Logger } from '../../utils/Logger';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { DriverHelper } from '../../utils/DriverHelper';

suite('Check all editors with all samples', function (): void {
	this.timeout(24000000);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

	const vsCodeDesktopSshEditor: string = '//*[@id="editor-selector-card-che-incubator/che-code-sshd/latest"]';
	const useExtensionSwitcher: string = '//label[@class="switch"]';
	const titleXpath: string = '//div[@class="header-title"]';

	let currentTabHandle: string = 'undefined';
	const pollingForCheckTitleVSCode: number = 100;
	const pollingForCheckTitleIntelliJ: number = 500;

	const allEditors: Array<{ xpath: string; name: string; type: 'vscode' | 'intellij' }> = [
		{ xpath: vsCodeDesktopSshEditor, name: 'VSCode Desktop SSH', type: 'vscode' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-clion-server/latest"]', name: 'CLion', type: 'intellij' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-goland-server/latest"]', name: 'GoLand', type: 'intellij' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-idea-server/latest"]', name: 'IntelliJ IDEA', type: 'intellij' },
		{
			xpath: '//*[@id="editor-selector-card-che-incubator/che-phpstorm-server/latest"]',
			name: 'PhpStorm',
			type: 'intellij'
		},
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-pycharm-server/latest"]', name: 'PyCharm', type: 'intellij' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-rider-server/latest"]', name: 'Rider', type: 'intellij' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-rubymine-server/latest"]', name: 'RubyMine', type: 'intellij' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-webstorm-server/latest"]', name: 'WebStorm', type: 'intellij' },
		{ xpath: '//*[@id="editor-selector-card-che-incubator/che-code-sshd-kiro/latest"]', name: 'Kiro', type: 'vscode' },
		{
			xpath: '//*[@id="editor-selector-card-che-incubator/jetbrains-sshd/latest"]',
			name: 'JetBrains SSH',
			type: 'intellij'
		}
	];

	// filter editors based on environment variable
	const selectedEditorsEnv: string = process.env.TS_SELECTED_EDITORS || 'All';
	let editorsForCheck: Array<{ xpath: string; name: string; type: 'vscode' | 'intellij' }>;

	if (selectedEditorsEnv === 'All') {
		editorsForCheck = allEditors;
		Logger.info('Running tests for all editors');
	} else {
		const selectedEditorNames: string[] = selectedEditorsEnv.split(',').map((name: string): string => name.trim());
		editorsForCheck = allEditors.filter((editor): boolean => selectedEditorNames.includes(editor.name));
		Logger.info(`Running tests for selected editors: ${editorsForCheck.map((e): string => e.name).join(', ')}`);

		if (editorsForCheck.length === 0) {
			Logger.warn(`No editors matched the selection: ${selectedEditorsEnv}`);
			Logger.warn(`Available editors: ${allEditors.map((e): string => e.name).join(', ')}`);
		}
	}

	const samplesForCheck: string[] = [
		'Empty Workspace',
		'JBoss EAP 8.0',
		'Java Lombok',
		'Node.js Express',
		'Python',
		'Quarkus REST API',
		'.NET',
		'Ansible',
		'C/C++',
		'Go',
		'PHP'
	];

	const gitRepoUrlsToCheck: string[] = [
		'https://github.com/crw-qe/quarkus-api-example-public/tree/ubi8-latest',
		'https://github.com/crw-qe/ubi9-based-sample-public/tree/ubi9-minimal'
	];

	const gitRepoUrlsToCheckAirgap: string[] = [
		'https://gh.crw-qe.com/test-automation-only/ubi8/tree/ubi8-latest',
		'https://gh.crw-qe.com/test-automation-only/ubi9-based-sample-public/tree/ubi9-minimal'
	];

	suiteSetup('Login into Che', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	async function clickOnElementByXpath(xpath: string): Promise<void> {
		Logger.debug();
		await driverHelper
			.getDriver()
			.executeScript(
				`document.evaluate('${xpath}', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click();`
			);
	}

	function clearCurrentTabHandle(): void {
		currentTabHandle = 'undefined';
	}

	async function testWorkspaceStartup(
		editorXpath: string,
		editorType: 'vscode' | 'intellij',
		sampleNameOrUrl: string,
		isUrl: boolean
	): Promise<void> {
		await dashboard.openDashboard();
		currentTabHandle = await browserTabsUtil.getCurrentWindowHandle();
		await dashboard.clickCreateWorkspaceButton();

		const pollingForCheckTitle: number = editorType === 'vscode' ? pollingForCheckTitleVSCode : pollingForCheckTitleIntelliJ;

		if (isUrl) {
			await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndGitUrl(
				editorXpath,
				sampleNameOrUrl,
				titleXpath,
				pollingForCheckTitle
			);
		} else {
			await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndSample(
				editorXpath,
				sampleNameOrUrl,
				titleXpath,
				pollingForCheckTitle
			);
		}

		if (editorType === 'vscode') {
			// read page
			const pageTextBeforeUseExtensionSwitcher: string = await driverHelper
				.getDriver()
				.executeScript('return document.body.innerText;');

			// click on "Use Extension" switcher
			await clickOnElementByXpath(useExtensionSwitcher);

			// read page
			const pageTextAfterUseExtensionSwitcher: string = await driverHelper
				.getDriver()
				.executeScript('return document.body.innerText;');

			// checks for "Install extensions" state
			expect(pageTextBeforeUseExtensionSwitcher).contains('Install the following VS Code extensions');
			Logger.info('"Install the following VS Code extensions" was found in page before "Use Extension" clicked');

			expect(pageTextBeforeUseExtensionSwitcher).contains('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running');
			Logger.info(
				'Workspace name "' + WorkspaceHandlingTests.getWorkspaceName() + ' is running" was found before "Use Extension" clicked'
			);

			// checks for SSH state
			expect(pageTextAfterUseExtensionSwitcher).contains('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running');
			Logger.info(
				'Workspace name "' + WorkspaceHandlingTests.getWorkspaceName() + ' is running" was found after "Use Extension" clicked'
			);

			expect(pageTextAfterUseExtensionSwitcher).contains('oc port-forward -n admin-devspaces');
			Logger.info('"oc port-forward -n admin-devspaces" was found after "Use Extension" clicked');

			expect(pageTextAfterUseExtensionSwitcher)
				.contains('-----BEGIN OPENSSH PRIVATE KEY-----')
				.and.contains('-----END OPENSSH PRIVATE KEY-----');
			Logger.info('SSH private key (BEGIN and END markers) was found after "Use Extension" clicked');

			expect(pageTextAfterUseExtensionSwitcher)
				.contains('HostName')
				.and.contains('User')
				.and.contains('Port')
				.and.contains('IdentityFile')
				.and.contains('UserKnownHostsFile');
			Logger.info(
				'SSH config parameters (HostName, User, Port, IdentityFile, UserKnownHostsFile) were found after "Use Extension" clicked'
			);
		} else {
			// check title for IntelliJ editors
			const headerText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(titleXpath);
			expect('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running').equal(headerText);
			Logger.info('Workspace title verified for IntelliJ editor: ' + headerText);
		}
	}

	editorsForCheck.forEach((editor): void => {
		samplesForCheck.forEach((sampleName): void => {
			test(`Test start of ${editor.name} with sample: ${sampleName}`, async function (): Promise<void> {
				await testWorkspaceStartup(editor.xpath, editor.type, sampleName, false);
			});
		});
	});

	editorsForCheck.forEach((editor): void => {
		if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
			Logger.info('Test cluster is disconnected. Using url for airgap cluster.');
			gitRepoUrlsToCheckAirgap.forEach((url): void => {
				test(`Test start of ${editor.name} with ubi url: ${url}`, async function (): Promise<void> {
					await testWorkspaceStartup(editor.xpath, editor.type, url, true);
				});
			});
		} else {
			gitRepoUrlsToCheck.forEach((url): void => {
				test(`Test start of ${editor.name} with ubi url: ${url}`, async function (): Promise<void> {
					await testWorkspaceStartup(editor.xpath, editor.type, url, true);
				});
			});
		}
	});

	teardown('Delete DevWorkspace', async function (): Promise<void> {
		Logger.info('Delete DevWorkspace. After each test.');
		if (currentTabHandle !== 'undefined') {
			await browserTabsUtil.switchToWindow(currentTabHandle);
		}

		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		if (WorkspaceHandlingTests.getWorkspaceName() !== 'undefined') {
			Logger.info('Workspace name is defined. Deleting workspace...');
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}

		WorkspaceHandlingTests.clearWorkspaceName();
		clearCurrentTabHandle();
	});
});
