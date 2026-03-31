/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import fs from 'fs';
import path from 'path';
import YAML from 'yaml';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { LoginTests } from '../../tests-library/LoginTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { expect } from 'chai';
import { Logger } from '../../utils/Logger';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';

suite('Check Intellij IDE desktop Editor with all samples', function (): void {
	this.timeout(24000000);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const pathToSampleFile: string = path.resolve('resources/default-devfile.yaml');
	const workspaceName: string = YAML.parse(fs.readFileSync(pathToSampleFile, 'utf8')).metadata.name;
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

	const titleXpath: string = '/html/body/h1';
	let currentTabHandle: string = 'undefined';

	const pollingForCheckTitle: number = 500;

	const editorsForCheck: string[] = [
		'//*[@id="editor-selector-card-che-incubator/che-clion-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-goland-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-idea-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-phpstorm-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-pycharm-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-rider-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-rubymine-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-webstorm-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/jetbrains-sshd/latest"]'
	];

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
		'https://github.com/crw-qe/ubi9-based-sample-public/tree/ubi9-minimal' // ubi9-minimal-demo
	];

	const gitRepoUrlsToCheckAirgap: string[] = [
		'https://gh.crw-qe.com/test-automation-only/ubi8/tree/ubi8-latest',
		'https://gh.crw-qe.com/test-automation-only/ubi9-based-sample-public/tree/ubi9-minimal'
	];

	function clearCurrentTabHandle(): void {
		currentTabHandle = 'undefined';
	}

	suiteSetup('Login into Che', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	async function testWorkspaceStartup(editorXpath: string, sampleNameOrUrl: string, isUrl: boolean): Promise<void> {
		await dashboard.openDashboard();
		currentTabHandle = await browserTabsUtil.getCurrentWindowHandle();

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

		// check title
		const headerText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(titleXpath);
		expect('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running').equal(headerText);
	}

	editorsForCheck.forEach((editorXpath): void => {
		samplesForCheck.forEach((sampleName): void => {
			test(`Test start of Editor with xPath: ${editorXpath} and with sample name: ${sampleName}`, async function (): Promise<void> {
				await testWorkspaceStartup(editorXpath, sampleName, false);
			});
		});
	});

	editorsForCheck.forEach((editorXpath): void => {
		if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
			Logger.info('Test cluster is disconnected. Using url for airgap cluster.');
			gitRepoUrlsToCheckAirgap.forEach((gitUbiUrl): void => {
				test(`Test start of Editor with xPath: ${editorXpath} and with ubi url: ${gitUbiUrl}`, async function (): Promise<void> {
					await testWorkspaceStartup(editorXpath, gitUbiUrl, true);
				});
			});
		} else {
			gitRepoUrlsToCheck.forEach((gitUbiUrl): void => {
				test(`Test start of Editor with xPath: ${editorXpath} and with ubi url: ${gitUbiUrl}`, async function (): Promise<void> {
					await testWorkspaceStartup(editorXpath, gitUbiUrl, true);
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
			Logger.info('Workspace name is defined. Deleting workspace...')
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}

		WorkspaceHandlingTests.clearWorkspaceName();
		clearCurrentTabHandle();
	});
});
