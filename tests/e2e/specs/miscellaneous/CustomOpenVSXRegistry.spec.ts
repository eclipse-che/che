/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { DriverHelper } from '../../utils/DriverHelper';
import { LoginTests } from '../../tests-library/LoginTests';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { ContextMenu, Locators, SingleScmProvider, ViewSection } from 'monaco-page-objects';
import { OauthPage } from '../../pageobjects/git-providers/OauthPage';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { registerRunningWorkspace } from '../MochaHooks';
import { StringUtil } from '../../utils/StringUtil';
import { Logger } from '../../utils/Logger';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';

suite(
	`Create a workspace via launching a factory from the ${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_PROVIDER} repository ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`,
	function (): void {
		const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
		const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
		const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
		const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
		let projectSection: ViewSection;
		let testRepoProjectName: string;
		let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
		
		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		test('Navigate to the factory URL', async function (): Promise<void> {
			await browserTabsUtil.navigateTo(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_URL());
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		});

		test('Registering the running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Wait the workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Check if a project folder has been created', async function (): Promise<void> {
			testRepoProjectName = StringUtil.getProjectNameFromGitUrl(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL);
			Logger.debug(`new SideBarView().getContent().getSection: get ${testRepoProjectName}`);
			projectSection = await projectAndFileTests.getProjectViewSession();
			expect(await projectAndFileTests.getProjectTreeItem(projectSection, testRepoProjectName), 'Project folder was not imported').not
				.undefined;
		});

		test('Accept the project as a trusted one', async function (): Promise<void> {
			await projectAndFileTests.performTrustAuthorDialog();
		});

		test('Create and check container runs using kubedock and podman', function (): void {
			kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
			kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
			kubernetesCommandLineToolsExecutor.loginToOcp();
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(testScript);
			expect(output, 'Podman test script failed').contains('Successfully tagged');
			const runOutput: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(runTestScript);
			expect(runOutput, 'Podman test script failed').contains('Hello from Kubedock!');
		});
		
	}
);
