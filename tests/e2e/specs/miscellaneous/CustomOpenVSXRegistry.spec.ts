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
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { ViewSection } from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
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
		const pathToPluginRegistry: string = '/projects/devspaces/dependencies/che-plugin-registry/';

		let projectSection: ViewSection;
		let testRepoProjectName: string;
		let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
		let workspaceName: string = '';
		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		test('Navigate to the factory URL', async function (): Promise<void> {
			await browserTabsUtil.navigateTo(FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_URL());
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
			workspaceName = WorkspaceHandlingTests.getWorkspaceName();
			expect(workspaceName, 'Workspace name was not fetched from the loading page').not.undefined;
		});

		test('Registering the running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Wait the workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Accept the project as a trusted one', async function (): Promise<void> {
			await projectAndFileTests.performTrustAuthorDialog();
		});

		test('Check if a project folder has been created', async function (): Promise<void> {
			testRepoProjectName = 'devspaces';
			Logger.debug(`new SideBarView().getContent().getSection: get ${testRepoProjectName}`);
			projectSection = await projectAndFileTests.getProjectViewSession();
			expect(await projectAndFileTests.getProjectTreeItem(projectSection, testRepoProjectName), 'Project folder was not imported').not
				.undefined;
		});

		test('Edit VSX plugin list', function (): void {
			// modify the openvsx-sync.json file with just one item - for speeding up the build an image
			kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
			kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
			kubernetesCommandLineToolsExecutor.loginToOcp();
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
			const pathToOpenVSXOriginFile: string = `${pathToPluginRegistry}openvsx-sync.json`;
			const pathToOpenVSXTmpFile: string = `${pathToPluginRegistry}temp.json`;
			const editVSCListCommand: string = `jq "[.[] | select(.id == \\"redhat.vscode-xml\\")]" ${pathToOpenVSXOriginFile} > ${pathToOpenVSXTmpFile} && mv ${pathToOpenVSXTmpFile} ${pathToOpenVSXOriginFile}`;
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(editVSCListCommand);
			expect(output.code).to.equals(0);
		});

		test('Login podman into official registry', function (): void {
			const commandToAuthenticateInRegistry: string = `podman login -u \"${process.env.REGISTRY_LOGIN}\" -p ${process.env.REGISTRY_PASSWORD} registry.redhat.io`;
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(commandToAuthenticateInRegistry);
			expect(output).contains('Login Succeeded!');
		});

		test('Build custom image', function (): void {
			const commandToBuildCustomVSXImage: string = `cd ${pathToPluginRegistry} && yes | ./build.sh`;
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(commandToBuildCustomVSXImage);

			expect(output).contains('Login Succeeded!');

			//
			// const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(commandToBuildCustomVSXImage);
			// console.log('....................................', output.toString());
		});
	}
);
