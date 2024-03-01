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
	const defaultWTOProjectNameForAdmin: string = 'openshift-terminal';
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const webTerminalToolContainerName: string = 'web-terminal-tooling';
	const fileForVerificationTerminalCommands: string = 'result.txt';

	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp('admin');
	});

	suiteTeardown(function (): void {
		shellExecutor.executeArbitraryShellScript(`oc delete dw --all -n ${defaultWTOProjectNameForAdmin}`);
	});
	loginTests.loginIntoOcpConsole();
	test('Open Web Terminal after first installation', async function (): Promise<void> {
		await ocpMainPage.waitOpenMainPage();
		await driverHelper.refreshPage();
		await webTerminal.clickOnWebTerminalIcon();
	});
	test('Verify first started WTO widget and disabled state Project field under admin user', async function (): Promise<void> {
		await webTerminal.waitTerminalWidget();
		expect(await webTerminal.waitDisabledProjectFieldAndGetProjectName()).equal(defaultWTOProjectNameForAdmin);
	});
	test('Check starting Web Terminal under admin', async function (): Promise<void> {
		kubernetesCommandLineToolsExecutor.namespace = await webTerminal.getAdminProjectName();
		await webTerminal.clickOnStartWebTerminalButton();
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
			'oc         \\d+\\.\\d+\\.\\d+          OpenShift CLI\n' +
			'kubectl    \\d+\\.\\d+\\.\\d+          Kubernetes CLI\n' +
			'kustomize  \\d+\\.\\d+\\.\\d+           Kustomize CLI \\(built-in to kubectl\\)\n' +
			'helm       \\d+\\.\\d+\\.\\d+          Helm CLI\n' +
			'kn         \\d+\\.\\d+\\.\\d+           KNative CLI\n' +
			'tkn        \\d+\\.\\d+\\.\\d+          Tekton CLI\n' +
			'subctl     \\d+\\.\\d+\\.\\d+          Submariner CLI\n' +
			'odo        \\d+\\.\\d+\\.\\d+          Red Hat OpenShift Developer CLI\n' +
			'virtctl    \\d+\\.\\d+\\.\\d+          KubeVirt CLI\n' +
			'jq         \\d+\\.\\d+             jq';

		await webTerminal.typeAndEnterIntoWebTerminal(`help > ${fileForVerificationTerminalCommands}`);
		const commandResult: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(
			`cat /home/user/${fileForVerificationTerminalCommands}`,
			webTerminalToolContainerName
		);
		expect(commandResult).to.match(new RegExp(helpCommandExpectedResult));
	});

	test('Verify help command under admin user', async function (): Promise<void> {
		await webTerminal.typeAndEnterIntoWebTerminal('wtoctl set timeout 30s');
		await webTerminal.waitTerminalInactivity();
	});
});
