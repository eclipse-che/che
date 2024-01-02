/** *******************************************************************
 * copyright (c) 2024 Red Hat, Inc.
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
import { DriverHelper } from '../../utils/DriverHelper';
import { WebTerminalPage } from '../../pageobjects/webterminal/WebTerminalPage';
import { expect } from 'chai';
import YAML from 'yaml';

suite(`Login to Openshift console and start WebTerminal ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);
	const webTerminal: WebTerminalPage = e2eContainer.get(CLASSES.WebTerminalPage);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);

	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const webTerminalToolContainerName: string = 'web-terminal-tooling';
	const fileForVerificationTerminalCommands: string = 'result.txt';
	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp('admin');
	});

	loginTests.loginIntoOcpConsole();
	test('Open Web Terminal after first installation', async function (): Promise<void> {
		await ocpMainPage.waitOpenMainPage();
		await driverHelper.refreshPage();
		await webTerminal.clickOnWebTerminalIcon();
	});
	test('Verify inactivity dropdown menu for admin user', async function (): Promise<void> {
		await webTerminal.clickOnProjectListDropDown();
	});
	test('Verify first started WTO widget and disabled state Project field under admin user', async function (): Promise<void> {
		await webTerminal.waitTerminalWidget();
		expect(await webTerminal.waitDisabledProjectFieldAndGetProjectName()).equal('openshift-terminal');
	});
	test('Check starting Web Terminal under admin', async function (): Promise<void> {
		kubernetesCommandLineToolsExecutor.namespace = await webTerminal.getAdminProjectName();
		await webTerminal.clickOnStartWebTerminalIcon();
		await webTerminal.waitTerminalIsStarted();
		await webTerminal.typeAndEnterIntoWebTerminal(`oc whoami > ${fileForVerificationTerminalCommands}`);
		const devWorkspaceYaml: string = shellExecutor.executeArbitraryShellScript(
			`oc get dw -n ${kubernetesCommandLineToolsExecutor.namespace} -o yaml`
		);
		kubernetesCommandLineToolsExecutor.workspaceName = YAML.parse(devWorkspaceYaml).items[0].metadata.name;
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
		const commandResult: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(
			`cat /home/user/${fileForVerificationTerminalCommands}`,
			webTerminalToolContainerName
		);
		expect(commandResult).contains('admin');
	});
	test('Verify help command under admin user', async function (): Promise<void> {
		const helpCommandExpectedResult: string =
			'oc         4.13.0          OpenShift CLI\n' +
			'kubectl    1.26.1          Kubernetes CLI\n' +
			'kustomize  4.5.7           Kustomize CLI (built-in to kubectl)\n' +
			'helm       3.11.1          Helm CLI\n' +
			'kn         1.9.2           KNative CLI\n' +
			'tkn        0.30.1          Tekton CLI\n' +
			'subctl     0.14.6          Submariner CLI\n' +
			'odo        3.15.0          Red Hat OpenShift Developer CLI\n' +
			'virtctl    0.59.2          KubeVirt CLI\n' +
			'jq         1.6             jq';
		await webTerminal.typeAndEnterIntoWebTerminal(`help > ${fileForVerificationTerminalCommands}`);
		const commandResult: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(
			`cat /home/user/${fileForVerificationTerminalCommands}`,
			webTerminalToolContainerName
		);
		expect(commandResult).contains(helpCommandExpectedResult);
	});

	test('Verify help command under admin user', async function (): Promise<void> {
		await webTerminal.typeAndEnterIntoWebTerminal('wtoctl set timeout 30s');
		await webTerminal.waitTerminalInactivity();
	});
});
