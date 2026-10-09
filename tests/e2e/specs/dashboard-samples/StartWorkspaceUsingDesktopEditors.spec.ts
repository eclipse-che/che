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
import { assert, expect } from 'chai';
import { Logger } from '../../utils/Logger';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { DriverHelper } from '../../utils/DriverHelper';
import { EditorConfig, ALL_EDITORS } from '../../constants/EDITOR_CONSTANTS';

suite('Check all editors with all samples', function (): void {
	this.timeout(24000000);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

	const useExtensionSwitcher: string = '//label[@class="switch"]';
	const intellijTitleXpath: string = '//main[@class="page-container"]/h1';
	const vsCodeTitleXpath: string = '//div[@class="header-title"]';
	const gatewayButtonXpath: string = '//a[@class="gateway-button"]';
	const openInIdeTitleXpath: string = '(//div[@class="connection-option"]/h3)[2]';
	const dashboardLinkXpath: string = '//p[@class="dashboard-link"]/a';

	let currentTabHandle: string = 'undefined';
	const pollingForCheckTitleVSCode: number = 100;
	const pollingForCheckTitleIntelliJ: number = 500;

	// filter editors based on environment variables
	const selectAllEditors: boolean = process.env.SELECT_ALL_EDITORS === 'true';
	const actualUser: string = process.env.TS_SELENIUM_OCP_USERNAME || '';
	let editorsForCheck: EditorConfig[];

	if (selectAllEditors) {
		editorsForCheck = Array.from(ALL_EDITORS.values());
	} else {
		editorsForCheck = Array.from(ALL_EDITORS.values()).filter((editor): boolean => {
			const envValue: string | undefined = process.env[editor.environmentId];
			return envValue === 'true';
		});

		if (editorsForCheck.length === 0) {
			assert.fail('No editors selected via environment variables');
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
		'https://github.com/crw-qe/ubi9-based-sample-public/tree/ubi9-minimal',
		'https://github.com/crw-qe/ubi10-based-sample-public/tree/main'
	];

	const gitRepoUrlsToCheckAirgap: string[] = [
		'https://gh.crw-qe.com/test-automation-only/ubi8/tree/ubi8-latest',
		'https://gh.crw-qe.com/test-automation-only/ubi9-based-sample-public/tree/ubi9-minimal',
		'https://gh.crw-qe.com/test-automation-only/ubi10-based-sample-public/tree/main'
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

	async function getPageTextWhenContains(marker: string, timeout: number = 30000): Promise<string> {
		Logger.debug(`marker: "${marker}"`);

		let pageText: string = '';
		await driverHelper
			.getDriver()
			.wait(async (): Promise<boolean> => {
				pageText = await driverHelper.getDriver().executeScript('return document.body.innerText;');
				return pageText.includes(marker);
			}, timeout)
			.catch((): void => {
				Logger.debug(`Timed out waiting for "${marker}"; asserting on last snapshot`);
			});

		return pageText;
	}

	function clearCurrentTabHandle(): void {
		currentTabHandle = 'undefined';
	}

	async function verifyVSCodeEditor(): Promise<void> {
		Logger.debug();

		const pageTextBeforeUseExtensionSwitcher: string = await driverHelper.getDriver().executeScript('return document.body.innerText;');

		await clickOnElementByXpath(useExtensionSwitcher);

		const pageTextAfterUseExtensionSwitcher: string = await getPageTextWhenContains('-----END OPENSSH PRIVATE KEY-----');

		expect(pageTextBeforeUseExtensionSwitcher).contains('Install the following VS Code extensions');
		Logger.debug('"Install the following VS Code extensions" was found in page before "Use Extension" clicked');

		expect(pageTextBeforeUseExtensionSwitcher).contains('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running');
		Logger.debug(
			'Workspace name "' + WorkspaceHandlingTests.getWorkspaceName() + ' is running" was found before "Use Extension" clicked'
		);

		expect(pageTextAfterUseExtensionSwitcher).contains('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running');
		Logger.debug(
			'Workspace name "' + WorkspaceHandlingTests.getWorkspaceName() + ' is running" was found after "Use Extension" clicked'
		);

		expect(pageTextAfterUseExtensionSwitcher).contains('oc port-forward -n ' + actualUser + '-devspaces');
		Logger.debug('"oc port-forward -n ' + actualUser + '-devspaces" was found');

		expect(pageTextAfterUseExtensionSwitcher, `Actual page text:\n${pageTextAfterUseExtensionSwitcher}`)
			.contains('-----BEGIN OPENSSH PRIVATE KEY-----')
			.and.contains('-----END OPENSSH PRIVATE KEY-----');
		Logger.debug('SSH private key (BEGIN and END markers) was found');

		expect(pageTextAfterUseExtensionSwitcher)
			.contains('HostName')
			.and.contains('User')
			.and.contains('Port')
			.and.contains('IdentityFile')
			.and.contains('UserKnownHostsFile');
		Logger.debug('SSH config parameters (HostName, User, Port, IdentityFile, UserKnownHostsFile) were found');
	}

	async function verifyIntelliJEditor(titleXpath: string): Promise<void> {
		Logger.debug();

		const workspaceName: string = WorkspaceHandlingTests.getWorkspaceName();

		const headerText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(titleXpath);
		expect(headerText).equal('Workspace ' + workspaceName + ' is running');
		Logger.debug('Workspace title verified for IntelliJ editor: ' + headerText);

		const pageText: string = await driverHelper.getDriver().executeScript('return document.body.innerText;');

		// "Get started" section with the connection method options
		expect(pageText).contains('Get started').and.contains('Select your preferred connection method.');
		Logger.debug('"Get started" section was found');

		expect(pageText).contains('JetBrains Gateway').and.contains('Connect using the standalone JetBrains Gateway application.');
		Logger.debug('"JetBrains Gateway" connection option was found');

		const openInIdeTitle: string = await workspaceHandlingTests.getTextFromUIElementByXpath(openInIdeTitleXpath);
		expect(openInIdeTitle).contains('Open in JetBrains');
		Logger.debug('"' + openInIdeTitle + '" connection option was found');

		expect(pageText).contains('File > Remote Development >').and.contains('connection wizard');
		Logger.debug('IDE connection wizard instructions were found');

		// the "Open Gateway" button has to point to the current workspace via a jetbrains-gateway deep link
		const gatewayButtonText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(gatewayButtonXpath);
		expect(gatewayButtonText).equal('Open Gateway');
		Logger.debug('"Open Gateway" button was found');

		const pageHtml: string = await driverHelper.getDriver().executeScript('return document.body.innerHTML;');
		expect(pageHtml)
			.contains('jetbrains-gateway://connect')
			.and.contains('dwName=' + workspaceName);
		Logger.debug('Gateway deep link for workspace "' + workspaceName + '" was found');

		// help section and the link back to the dashboard
		expect(pageText).contains('open the workspace?').and.contains('the documentation');
		Logger.debug('Help section was found');

		const dashboardLinkText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(dashboardLinkXpath);
		expect(dashboardLinkText).equal('Open Dashboard');
		Logger.debug('"Open Dashboard" link was found');
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
		const titleXpath: string = editorType === 'vscode' ? vsCodeTitleXpath : intellijTitleXpath;

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
			await verifyVSCodeEditor();
		} else {
			await verifyIntelliJEditor(titleXpath);
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
		Logger.debug('Delete DevWorkspace. After each test.');
		if (currentTabHandle !== 'undefined') {
			await browserTabsUtil.switchToWindow(currentTabHandle);
		}

		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		if (WorkspaceHandlingTests.getWorkspaceName() !== 'undefined') {
			Logger.debug('Workspace name is defined. Deleting workspace...');
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}

		WorkspaceHandlingTests.clearWorkspaceName();
		clearCurrentTabHandle();
	});
});
