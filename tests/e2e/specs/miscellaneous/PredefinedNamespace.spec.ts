/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { expect } from 'chai';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { OAUTH_CONSTANTS } from '../../constants/OAUTH_CONSTANTS';

suite(`Create predefined workspace and check it ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const predefinedNamespaceName: string = 'predefined-ns';
	const stackName: string = 'Empty Workspace';

	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);

	suiteSetup(function (): void {
		// create a predefined namespace for user using shell script and login into user dashboard
		Logger.info('Test prerequisites:');
		Logger.info(
			' (1) there is OCP user with username and user password that have been set in the TS_SELENIUM_OCP_USERNAME and TS_SELENIUM_OCP_PASSWORD variables'
		);
		Logger.info(' (2) "oc" client installed and logged into test OCP cluster with admin rights.');
		kubernetesCommandLineToolsExecutor.loginToOcp('admin');
		const predefinedProjectConfiguration: string =
			'kind: Namespace\n' +
			'apiVersion: v1\n' +
			'metadata:\n' +
			`  name: ${predefinedNamespaceName}\n` +
			'  labels:\n' +
			'    app.kubernetes.io/part-of: che.eclipse.org\n' +
			'    app.kubernetes.io/component: workspaces-namespace\n' +
			'  annotations:\n' +
			'    che.eclipse.org/username: user';
		kubernetesCommandLineToolsExecutor.applyWithoutNamespace(predefinedProjectConfiguration);
		const setEditRightsForUser: string = `oc adm policy add-role-to-user edit user -n ${predefinedNamespaceName}`;
		shellExecutor.executeCommand(setEditRightsForUser);
	});

	suiteTeardown(function (): void {
		const workspaceName: string = WorkspaceHandlingTests.getWorkspaceName();
		try {
			kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
			kubernetesCommandLineToolsExecutor.deleteProject(predefinedNamespaceName);
		} catch (e) {
			Logger.error(`Cannot remove the predefined project: ${workspaceName}, please fix it manually: ${e}`);
		}
	});

	suiteSetup('Login', async function (): Promise<void> {
		const userName: string = 'user';
		await loginTests.loginIntoChe(userName);
		if (OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME !== userName) {
			await loginTests.logoutFromChe();
			await loginTests.loginIntoChe(userName);
		}
	});
	// create the Empty workspace using CHE Dashboard
	test(`Create and open new workspace, stack:${stackName}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace(stackName);
	});
	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
	});

	// verify that just created workspace with unique name is present in the predefined namespace
	test('Validate the created workspace is present in predefined namespace', function (): void {
		const workspaceName: string = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.namespace = predefinedNamespaceName;
		const ocDevWorkspaceOutput: string = kubernetesCommandLineToolsExecutor.getDevWorkspaceYamlConfiguration();
		expect(ocDevWorkspaceOutput).includes(workspaceName);
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
