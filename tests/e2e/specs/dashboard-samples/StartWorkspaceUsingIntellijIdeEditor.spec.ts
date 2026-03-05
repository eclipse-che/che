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

	const titlexPath: string = '/html/body/h1';
	var currentTabHandle: string = 'undefined';

	const pollingForCheckTitle: number = 500;

	const editorsForCheck: string[] = [
		'//*[@id="editor-selector-card-che-incubator/che-clion-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-goland-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-idea-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-phpstorm-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-pycharm-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-rider-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-rubymine-server/latest"]',
		'//*[@id="editor-selector-card-che-incubator/che-webstorm-server/latest"]'
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
		'https://github.com/crw-qe/ubi9-based-sample-public/tree/ubi9-minimal'
	];

	const gitRepoUrlsToCheckAirgap: string[] = [
		'https://gh.crw-qe.com/test-automation-only/ubi8/tree/ubi8-latest',
		'https://gh.crw-qe.com/test-automation-only/ubi9-based-sample-public/tree/ubi9-minimal'
	];

	async function clearCurrentTabHandle() {
		currentTabHandle = 'undefined';
	}

	async function deleteWorkspace(): Promise<void> {
		await browserTabsUtil.switchToWindow(currentTabHandle);
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await dashboard.stopAndRemoveWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());

		WorkspaceHandlingTests.clearWorkspaceName();
		clearCurrentTabHandle();
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
				titlexPath,
				pollingForCheckTitle
			);
		} else {
			await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndSample(
				editorXpath,
				sampleNameOrUrl,
				titlexPath,
				pollingForCheckTitle
			);
		}

		// check title
		const headerText: string = await workspaceHandlingTests.getTextFromUIElementByXpath(titlexPath);
		expect('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running').equal(headerText);

		await deleteWorkspace();
	}

	test('Test start of Intellij IDE with default Samples', async function (): Promise<void> {
		for (const editorXpath of editorsForCheck) {
			for (const sampleName of samplesForCheck) {
				await testWorkspaceStartup(editorXpath, sampleName, false);
			}
		}
	});

	test('Test start of Intellij IDE (SSH) with ubi', async function (): Promise<void> {
		for (const editorXpath of editorsForCheck) {
			if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
				Logger.info('Test cluster is disconnected. Using url for airgap cluster.');
				for (const url of gitRepoUrlsToCheckAirgap) {
					await testWorkspaceStartup(editorXpath, url, true);
				}
			} else {
				for (const url of gitRepoUrlsToCheck) {
					await testWorkspaceStartup(editorXpath, url, true);
				}
			}
		}
	});

	suiteTeardown('Delete DevWorkspace', async function (): Promise<void> {
		Logger.info('Deleting DevWorkspace... After all.');
		if (currentTabHandle !== 'undefined') {
			await browserTabsUtil.switchToWindow(currentTabHandle);
		}

		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();

		if (WorkspaceHandlingTests.getWorkspaceName() !== 'undefined') {
			await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		}
	});
});
