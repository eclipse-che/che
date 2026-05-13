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
import { By } from 'selenium-webdriver';

suite('Check Visual Studio Code (desktop) (SSH) with all samples', function (): void {
	this.timeout(6000000);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

	const vsCodeDesktopSshEditor: string = '//*[@id="editor-selector-card-che-incubator/che-code-sshd/latest"]';

	const useExtensionSwitcher: string = '//label[@class="switch"]';
	const useExtensionPageId: string = '//div[@id="docs-parent"]';

	const titlexPath: string = '//div[@class="header-title"]';
	const ocPortForwardxPath: string = '//*[@id="port-forward"]';
	const sshKeyxPath: string = '//*[@id="key"]';
	const sshKonfigxPath: string = '//*[@id="config"]';

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
		await driverHelper.waitAndClick(By.xpath(xpath));
	}

	async function testWorkspaceStartup(sampleNameOrUrl: string, isUrl: boolean): Promise<void> {
		await dashboard.openDashboard();
		await dashboard.clickCreateWorkspaceButton();

		if (isUrl) {
			await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndGitUrl(
				vsCodeDesktopSshEditor,
				sampleNameOrUrl,
				titlexPath
			);
		} else {
			await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndSample(
				vsCodeDesktopSshEditor,
				sampleNameOrUrl,
				titlexPath
			);
		}

		const useExtensionPageText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(useExtensionPageId);
		expect(useExtensionPageText).contains('Install the following VS Code extensions');
		// toggle UseExtension switcher
		await clickOnElementByXpath(useExtensionSwitcher);
		// check title
		const headerText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(titlexPath);
		expect('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running').equal(headerText);
		// check oc-port-forwarding
		const ocPortForward: string = await workspaceHandlingTests.getTextFromUIElementByXpath(ocPortForwardxPath);
		expect(ocPortForward).contains('oc port-forward -n admin-devspaces');
		// check ssh key
		const sshKey: string = await workspaceHandlingTests.getTextFromUIElementByXpath(sshKeyxPath);
		expect(sshKey).contains('-----BEGIN OPENSSH PRIVATE KEY-----').and.contains('-----END OPENSSH PRIVATE KEY-----');
		// check .ssh/kofig
		const sshKonfig: string = await workspaceHandlingTests.getTextFromUIElementByXpath(sshKonfigxPath);
		expect(sshKonfig)
			.contains('HostName')
			.and.contains('User')
			.and.contains('Port')
			.and.contains('IdentityFile')
			.and.contains('UserKnownHostsFile');
	}

	samplesForCheck.forEach((sampleName): void => {
		test('Test start of VSCode (desktop) (SSH) with default Samples', async function (): Promise<void> {
			await testWorkspaceStartup(sampleName, false);
		});
	});

	if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
		Logger.info('Test cluster is disconnected. Using url for airgap cluster.');
		gitRepoUrlsToCheckAirgap.forEach((url): void => {
			test('Test start of VSCode (desktop) (SSH) with ubi', async function (): Promise<void> {
				await testWorkspaceStartup(url, true);
			});
		});
	} else {
		gitRepoUrlsToCheck.forEach((url): void => {
			test('Test start of VSCode (desktop) (SSH) with ubi', async function (): Promise<void> {
				await testWorkspaceStartup(url, true);
			});
		});
	}

	teardown('Delete DevWorkspace', async function (): Promise<void> {
		Logger.info('Delete DevWorkspace. After each test.');

		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		if (WorkspaceHandlingTests.getWorkspaceName() !== 'undefined') {
			Logger.info('Workspace name is defined. Deleting workspace...');
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}

		WorkspaceHandlingTests.clearWorkspaceName();
	});
});
