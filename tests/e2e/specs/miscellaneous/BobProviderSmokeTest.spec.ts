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
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';

import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { KubernetesCommandLineToolsExecutor, ContainerTerminal } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellString } from 'shelljs';
import { expect } from 'chai';
import { Logger } from '../../utils/Logger';
import { ApiUrlResolver } from '../../utils/workspace/ApiUrlResolver';
import { CheApiRequestHandler } from '../../utils/request-handlers/CheApiRequestHandler';
import { AxiosResponse } from 'axios';
import { V1alpha2DevWorkspaceSpecTemplateComponents } from '@devfile/api';

interface BobRunResult {
	status: string;
	last_message: string;
	stats: { tool_calls: number };
}

suite(`IBM Bob Provider Smoke Test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const stackName: string = 'Empty Workspace';
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const apiUrlResolver: ApiUrlResolver = e2eContainer.get(CLASSES.ApiUrlResolver);
	const requestHandler: CheApiRequestHandler = e2eContainer.get(CLASSES.CheApiRequestHandler);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const userPreferences: UserPreferences = e2eContainer.get(CLASSES.UserPreferences);
	const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);

	const providerName: string = 'IBM Bob CLI';
	const providerId: string = 'ibm/bob';
	const apiKey: string = BASE_TEST_CONSTANTS.TS_AI_PROVIDER_API_KEY;
	const envVarName: string = 'BOB_API_KEY';

	let loggedIn: boolean = false;
	let providerKeyAlreadyPresent: boolean = false;
	let createdProviderKey: boolean = false;
	let workspaceName: string = '';
	let workspaceContainer: string = '';

	suiteSetup('Login', async function (): Promise<void> {
		if (!apiKey) {
			this.skip();
		}
		await loginTests.loginIntoChe();
		loggedIn = true;
		await userPreferences.openUserPreferencesPage();
		await userPreferences.openAiProviderKeysTab();
		providerKeyAlreadyPresent = await userPreferences.isAiProviderKeyPresent(providerId);
		if (providerKeyAlreadyPresent) {
			Logger.info('Using the existing Bob key without replacing it');
		}
	});

	suite('Add AI Provider API key', function (): void {
		test('Navigate to AI Provider Keys tab', async function (): Promise<void> {
			await userPreferences.openUserPreferencesPage();
			await userPreferences.waitAiProviderKeysTab();
			await userPreferences.openAiProviderKeysTab();
		});

		test(`Add ${providerName} API key`, async function (): Promise<void> {
			if (providerKeyAlreadyPresent) {
				this.skip();
			}
			await userPreferences.addAiProviderKey(providerName, apiKey);
			createdProviderKey = true;
		});

		test(`Verify ${providerName} key is present in the list`, async function (): Promise<void> {
			await userPreferences.waitAiProviderKeyPresent(providerId);

			const actualEnvVarName: string = await userPreferences.getAiProviderEnvVarName(providerId);
			expect(actualEnvVarName).to.equal(envVarName);
		});
	});

	suite('Verify AI Provider on Create Workspace page', function (): void {
		test('Navigate to Create Workspace page', async function (): Promise<void> {
			await dashboard.clickCreateWorkspaceButton();
			await createWorkspace.waitPage();
		});

		test('Verify AI Provider section is visible', async function (): Promise<void> {
			await createWorkspace.waitAiProviderSectionVisible();
		});

		test('Expand "Choose an AI Provider" section', async function (): Promise<void> {
			await createWorkspace.expandChooseAiProviderSection();
		});

		test(`Verify ${providerName} provider card is visible`, async function (): Promise<void> {
			await createWorkspace.waitAiProviderCardVisible(providerId);
		});

		test('Verify "Key configured" badge is displayed', async function (): Promise<void> {
			await createWorkspace.waitAiProviderKeyConfiguredBadgeVisible(providerId);
		});
	});

	suite('Create workspace and verify AI tool', function (): void {
		test(`Create and open ${stackName}`, async function (): Promise<void> {
			const workspaceApiUrl: string = await apiUrlResolver.getWorkspacesApiUrl();
			kubernetesCommandLineToolsExecutor.namespace = workspaceApiUrl.split('/').slice(-2)[0];
			containerTerminal.namespace = kubernetesCommandLineToolsExecutor.namespace;
			await createWorkspace.setCreateNewWorkspaceCheckbox(true);
			await createWorkspace.selectAiProvider(providerId);
			const parentHandle: string = await browserTabsUtil.getCurrentWindowHandle();
			await createWorkspace.clickOnSampleNoEditorSelection(stackName);
			await browserTabsUtil.waitAndSwitchToAnotherWindow(parentHandle);
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
			workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		});

		test('Register running workspace', function (): void {
			registerRunningWorkspace(workspaceName);
		});

		test('Allocate the memory required by Bob', async function (): Promise<void> {
			const url: string = await apiUrlResolver.getWorkspaceApiUrl(workspaceName);
			const response: AxiosResponse = await requestHandler.get(url);
			const components: V1alpha2DevWorkspaceSpecTemplateComponents[] = response.data.spec.template.components;
			const index: number = components.findIndex(
				(component: V1alpha2DevWorkspaceSpecTemplateComponents): boolean =>
					component.container !== undefined && !component.name.endsWith('-injector')
			);
			expect(index, 'a workspace development container should exist').to.be.greaterThan(-1);
			workspaceContainer = components[index].name;
			await requestHandler.patch(url, [
				{ op: 'add', path: `/spec/template/components/${index}/container/memoryLimit`, value: '4Gi' }
			]);
		});

		test('Wait workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Connect to workspace container', function (): void {
			kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
			kubernetesCommandLineToolsExecutor.loginToOcp();
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
		});

		test('Verify bob binary is available in workspace', function (): void {
			const output: ShellString = containerTerminal.execInContainerCommand(
				'command -v bob && test -x /injected-tools/bob-cli/bin/node',
				workspaceContainer
			);
			expect(output.code, 'bob binary should be present in the workspace').to.equal(0);
		});

		test(`Verify ${envVarName} environment variable is set`, function (): void {
			const output: ShellString = containerTerminal.execInContainerCommand(`test -n "$${envVarName}"`, workspaceContainer);
			expect(output.code, `${envVarName} environment variable should be set`).to.equal(0);
		});

		test('Verify bob starts and reports version', function (): void {
			const output: ShellString = containerTerminal.execInContainerCommandWithTimeout('bob --version 2>&1', workspaceContainer, '30');
			expect(output.code, 'bob --version should exit with code 0').to.equal(0);
			expect(output.stdout, 'bob should return version info').to.match(/\d+\.\d+\.\d+/);
			expect(output.stdout.toLowerCase(), 'bob --version should not contain errors').to.not.contain('error');
		});

		test('Verify Bob completes an authenticated request', function (): void {
			// bob run reads stdin until EOF; oc exec -i otherwise keeps it open.
			const command: string =
				'bob run --accept-license --trust --disable-mcp --disable-subagents --format json --max-turns 1 "Reply with exactly BOB_WORKSPACE_OK. Do not use tools." </dev/null';
			const output: ShellString = containerTerminal.execInContainerCommandWithTimeout(command, workspaceContainer, '120');
			expect(output.code, 'Bob inference should succeed with the injected key').to.equal(0);
			const result: BobRunResult = JSON.parse(output.stdout);
			expect(result.status).to.equal('success');
			expect(result.last_message.trim()).to.equal('BOB_WORKSPACE_OK');
		});

		test('Verify Bob can execute a shell tool in the workspace', function (): void {
			const resultFile: string = `/tmp/bob-cli-smoke-${Date.now()}`;
			const command: string = `bob run --accept-license --trust --disable-mcp --disable-subagents --max-turns 3 --format json "Execute this command using a shell tool: printf BOB_SHELL_OK > ${resultFile}" </dev/null`;
			const output: ShellString = containerTerminal.execInContainerCommandWithTimeout(command, workspaceContainer, '120');
			expect(output.code, 'Bob shell tool execution should succeed').to.equal(0);
			const result: BobRunResult = JSON.parse(output.stdout);
			expect(result.status).to.equal('success');
			expect(result.stats.tool_calls).to.be.greaterThan(0);
			const fileOutput: ShellString = containerTerminal.execInContainerCommand(`cat ${resultFile}`, workspaceContainer);
			expect(fileOutput.code).to.equal(0);
			expect(fileOutput.stdout).to.equal('BOB_SHELL_OK');
		});
	});

	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		if (loggedIn) {
			await dashboard.openDashboard();
			await browserTabsUtil.closeAllTabsExceptCurrent();
		}
	});

	suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
		if (workspaceName) {
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		}
	});

	suiteTeardown('Delete only the key created by this test', async function (): Promise<void> {
		if (createdProviderKey) {
			await userPreferences.openUserPreferencesPage();
			await userPreferences.openAiProviderKeysTab();
			await userPreferences.deleteAiProviderKeys(providerName);
		}
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
