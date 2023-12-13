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
import { DriverHelper } from '../../utils/DriverHelper';
import { WebTerminalPage } from '../../pageobjects/webterminal/WebTerminalPage';

suite(`Login to Openshift console and start WebTerminal ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);
	const webTerminal: WebTerminalPage = e2eContainer.get(CLASSES.WebTerminalPage);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

	suiteSetup(function (): void {
		// kubernetesCommandLineToolsExecutor.loginToOcp('admin');
	// 	shellExecutor.executeCommand('oc project openshift-operators');
	});

	// loginTests.loginIntoOcpConsole();

	test.skip('Open Web Terminal after first installation', async function (): Promise<void> {
		await ocpMainPage.waitOpenMainPage();
		await driverHelper.refreshPage();
		await webTerminal.clickOnWebTerminalIcon();
	});
	test.skip('Verify inactivity dropdown menu for admin user', async function (): Promise<void> {
		//	await webTerminal.clickOnProjectListDropDown();
	});
	test('Verify disabled state Project field and check project name for admin user', async function (): Promise<void> {
		await webTerminal.cropScreen();
		//	expect(await webTerminal.waitDisabledProjectFieldAndGetProjectName()).equal('openshift-terminal');
	});
});
