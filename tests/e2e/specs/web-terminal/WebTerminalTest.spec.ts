/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { LoginTests } from '../../tests-library/LoginTests';
import { OcpMainPage } from '../../pageobjects/openshift/OcpMainPage';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { expect } from 'chai';

suite(`Login to Openshift console and start WebTerminal ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);

	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp('admin');
		shellExecutor.executeCommand('oc project openshift-operators');
	});

	loginTests.loginIntoOcpConsole();

	test('Open Web Terminal', async function (): Promise<void> {
		await ocpMainPage.waitOpenMainPage();
		await ocpMainPage.openWebTerminal();
	});

	test('Check username is correct', async function (): Promise<void> {
		const userUid = shellExecutor.executeCommand('oc get user $(oc whoami) -o jsonpath={.metadata.uid}').replace(/\n/g, '');

		// label selector for Web Terminal workspaces owned by the currently-logged in user
		const labelSelector = 'console.openshift.io/terminal=true,controller.devfile.io/creator=' + userUid;
		// namespace of this users web terminal
		const namespace = shellExecutor.executeCommand(`oc get dw -A -l ${labelSelector} -o jsonpath='{.items[0].metadata.namespace}'`);
		// devWorkspace ID for this users web terminal
		const termDwId = shellExecutor.executeCommand(`oc get dw -A -l ${labelSelector} -o jsonpath='{.items[0].status.devworkspaceId}'`);
		// use oc exec to run oc whoami inside this user's terminal
		const user = shellExecutor
			.executeCommand(`oc exec -n ${namespace} deploy/${termDwId} -c web-terminal-tooling -- oc whoami`)
			.replace(/\n/g, '');
		// above should output current user's username:
		expect(user).to.equal(shellExecutor.executeCommand('oc whoami').replace(/\n/g, ''));
	});
});
