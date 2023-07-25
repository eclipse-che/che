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
import { assert } from 'chai';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import { LoginTests } from '../../tests-library/LoginTests';
import { registerRunningWorkspace } from '../MochaHooks';

const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
const util: any = require('node:util');
const exec: any = util.promisify(require('node:child_process').exec);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const predefinedNamespaceName: string = 'predefined-ns';

async function runShellScript(shellCommandToExecution: string): Promise<string> {
	const { stdout, stderr } = await exec(shellCommandToExecution);
	console.log(stdout);
	console.error(stderr);
	return stdout;
}

suite('Create predefined workspace and check it ', function (): void {
	let workspaceName: string = '';

	const setEditRightsForUser: string = `oc adm policy add-role-to-user edit user -n ${predefinedNamespaceName}`;
	const getDevWorkspaceFromPredefinedNameSpace: string = `oc get dw -n ${predefinedNamespaceName}`;
	const deletePredefinedNamespace: string = `oc delete project ${predefinedNamespaceName}`;
	const createPredefinedProjectCommand: string =
		'cat <<EOF | oc apply -f - \n' +
		'kind: Namespace\n' +
		'apiVersion: v1\n' +
		'metadata:\n' +
		`  name: ${predefinedNamespaceName}\n` +
		'  labels:\n' +
		'    app.kubernetes.io/part-of: che.eclipse.org\n' +
		'    app.kubernetes.io/component: workspaces-namespace\n' +
		'  annotations:\n' +
		'    che.eclipse.org/username: user\n' +
		'EOF';
	// create a predefined namespace for user using shell script and login into user dashboard
	suiteSetup(async function (): Promise<void> {
		Logger.info('Test prerequisites:');
		Logger.info(
			' (1) there is OCP user with username and user password that have been set in the TS_SELENIUM_OCP_USERNAME and TS_SELENIUM_OCP_PASSWORD variables'
		);
		Logger.info(' (2) "oc" client installed and logged into test OCP cluster with admin rights.');

		await runShellScript(createPredefinedProjectCommand);
		await runShellScript(setEditRightsForUser);
	});

	suiteTeardown(async (): Promise<void> => {
		const workspaceName: string = WorkspaceHandlingTests.getWorkspaceName();
		try {
			await runShellScript(deletePredefinedNamespace);
		} catch (e) {
			Logger.error(`Cannot remove the predefined project: ${workspaceName}, please fix it manually: ${e}`);
		}
	});

	loginTests.loginIntoChe();
	// create the Empty workspace using CHE Dashboard
	test(`Create and open new workspace, stack:${'Empty Workspace'}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace('Empty Workspace');
	});
	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
	});

	// verify that just created workspace with unique name is present in the predefined namespace
	test('Validate the created workspace is present in predefined namespace', async function (): Promise<void> {
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		registerRunningWorkspace(workspaceName);
		const ocDevWorkspaceOutput: string = await runShellScript(getDevWorkspaceFromPredefinedNameSpace);
		assert.isTrue(ocDevWorkspaceOutput.includes(workspaceName));
	});

	loginTests.logoutFromChe();
});
