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

suite('Check Visual Studio Code (desktop) (SSH) with all samples', function (): void {
	this.timeout(6000000);
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

	const vsCodeDesktopSshEditor: string = '//*[@id="editor-selector-card-che-incubator/che-code-sshd/latest"]';
	const titlexPath: string = '/html/body/h1';
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

	async function deleteWorkspace(): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await dashboard.stopAndRemoveWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
	}

	suiteSetup('Login into Che', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	async function testWorkspaceStartup(sampleNameOrUrl: string, isUrl: boolean): Promise<void> {
		await dashboard.openDashboard();
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

		await deleteWorkspace();
	}

	test('Test start of VSCode (desktop) (SSH) with default Samples', async function (): Promise<void> {
		for (const sampleName of samplesForCheck) {
			await testWorkspaceStartup(sampleName, false);
		}
	});

	test('Test start of VSCode (desktop) (SSH) with ubi', async function (): Promise<void> {
		if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
			Logger.info('Test cluster is disconnected. Using url for airgap cluster.');
			for (const url of gitRepoUrlsToCheckAirgap) {
				await testWorkspaceStartup(url, true);
			}
		} else {
			for (const url of gitRepoUrlsToCheck) {
				await testWorkspaceStartup(url, true);
			}
		}
	});

	suiteTeardown('Delete DevWorkspace', async function (): Promise<void> {
		Logger.info('Deleting DevWorkspace...');
		await deleteWorkspace();
	});
});
