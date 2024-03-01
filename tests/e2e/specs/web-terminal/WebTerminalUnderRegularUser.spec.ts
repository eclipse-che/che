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
import { TimeUnits, WebTerminalPage } from '../../pageobjects/webterminal/WebTerminalPage';
import { expect } from 'chai';
import YAML from 'yaml';
import { afterEach } from 'mocha';
import { By } from 'selenium-webdriver';

suite(`Login to Openshift console and check WebTerminal ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const webTerminalToolContainerName: string = 'web-terminal-tooling';
	const testProjectName: string = 'wto-under-regular-user-test';
	const fileForVerificationTerminalCommands: string = 'result.txt';
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);
	const webTerminal: WebTerminalPage = e2eContainer.get(CLASSES.WebTerminalPage);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);

	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	suiteTeardown(function (): void {
		kubernetesCommandLineToolsExecutor.deleteProject(testProjectName);
	});

	afterEach(function (): void {
		try {
			shellExecutor.executeArbitraryShellScript(`oc delete dw --all -n ${testProjectName}`);
		} catch (e) {
			console.log(`cannot delete the  ${testProjectName} under regular user:`, e);
		}
	});

	loginTests.loginIntoOcpConsole();

	test('Open WebTerminal after first installation', async function (): Promise<void> {
		await ocpMainPage.waitOpenMainPage();
		await driverHelper.refreshPage();
		await webTerminal.clickOnWebTerminalIcon();
	});

	test('Check WebTerminal with creating new Openshift Project', async function (): Promise<void> {
		kubernetesCommandLineToolsExecutor.namespace = testProjectName;
		await webTerminal.typeProjectName(testProjectName);
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
		const currentUserName: string = await driverHelper.waitAndGetText(By.css('span[data-test="username"]'));
		expect(commandResult).contains(currentUserName);
	});

	test('Check running WebTerminal in the existed Project with custom timeout', async function (): Promise<void> {
		await webTerminal.findAndSelectProject(testProjectName);
		await webTerminal.clickOnTimeoutButton();
		await webTerminal.clickOnTimeUnitDropDown();
		await webTerminal.selectTimeUnit(TimeUnits.Seconds);
		await webTerminal.setTimeoutByEntering(20);
		await webTerminal.clickOnStartWebTerminalButton();
		await webTerminal.waitTerminalIsStarted();
		await webTerminal.waitTerminalInactivity();
	});
});
