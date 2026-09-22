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
import { AI_PROVIDER_TEST_CONSTANTS } from '../../constants/AI_PROVIDER_TEST_CONSTANTS';
import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { KubernetesCommandLineToolsExecutor, ContainerTerminal } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellString } from 'shelljs';
import { expect } from 'chai';
import { Logger } from '../../utils/Logger';

const stackName: string = 'Empty Workspace';

suite(`AI Provider Smoke Test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
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

	const providerName: string = AI_PROVIDER_TEST_CONSTANTS.TS_AI_PROVIDER_NAME;
	const providerId: string = AI_PROVIDER_TEST_CONSTANTS.TS_AI_PROVIDER_ID;
	const apiKey: string = AI_PROVIDER_TEST_CONSTANTS.TS_AI_PROVIDER_API_KEY;
	const envVarName: string = AI_PROVIDER_TEST_CONSTANTS.TS_AI_PROVIDER_ENV_VAR_NAME;

	async function deleteAiProviderKeys(): Promise<void> {
		Logger.debug('Deleting AI Provider keys if they are present');
		await userPreferences.openUserPreferencesPage();
		await userPreferences.openAiProviderKeysTab();
		await userPreferences.deleteAiProviderKeys(providerName);
	}

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
		await deleteAiProviderKeys();
	});

	suite('Add AI Provider API key', function (): void {
		test('Navigate to AI Provider Keys tab', async function (): Promise<void> {
			await userPreferences.openUserPreferencesPage();
			await userPreferences.waitAiProviderKeysTab();
			await userPreferences.openAiProviderKeysTab();
		});

		test(`Add ${providerName} API key`, async function (): Promise<void> {
			await userPreferences.addAiProviderKey(providerName, apiKey);
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
			const isBadgeVisible: boolean = await createWorkspace.isAiProviderKeyConfiguredBadgeVisible();
			expect(isBadgeVisible, '"Key configured" badge should be visible').to.be.true;
		});
	});

	suite('Create workspace and verify AI tool', function (): void {
		test(`Create and open ${stackName}`, async function (): Promise<void> {
			await workspaceHandlingTests.createAndOpenWorkspace(stackName);
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		});

		test('Register running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Wait workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Connect to workspace container', function (): void {
			kubernetesCommandLineToolsExecutor.workspaceName = WorkspaceHandlingTests.getWorkspaceName();
			kubernetesCommandLineToolsExecutor.loginToOcp();
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
		});

		test('Verify opencode binary is available in workspace', function (): void {
			const output: ShellString = containerTerminal.execInContainerCommand('which opencode || ls /injected-tools/opencode');
			expect(output.code, 'opencode binary should be present in the workspace').to.equal(0);
		});

		test(`Verify ${envVarName} environment variable is set`, function (): void {
			const output: ShellString = containerTerminal.execInContainerCommand(`printenv ${envVarName}`);
			expect(output.stdout.trim(), `${envVarName} environment variable should be set`).to.not.be.empty;
		});

		test('Verify opencode starts and reports version', function (): void {
			const output: ShellString = containerTerminal.execInContainerCommandWithTimeout('opencode --version 2>&1', undefined, '15');
			expect(output.code, 'opencode --version should exit with code 0').to.equal(0);
			expect(output.stdout, 'opencode should return version info').to.not.be.empty;
			expect(output.stdout.toLowerCase(), 'opencode --version should not contain errors').to.not.contain('error');
		});
	});

	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
