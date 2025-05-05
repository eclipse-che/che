/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
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
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { DriverHelper } from '../../utils/DriverHelper';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { By, Locators, ModalDialog } from 'monaco-page-objects';
import { expect } from 'chai';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';

suite('"Check workspace run timeout" test', function (): void {
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
	const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

	const SECONDS_OF_RUN_BEFORE_IDLING: number = Number(process.env.TS_SELENIUM_SECONDS_OF_RUN_BEFORE_IDLING) || 60;
	const stackName: string = 'Empty Workspace';
	const cheClusterName: string = 'devspaces';
	let defaultRunTimeoutValue: number = 0;

	async function checkDialogButton(buttonName: string): Promise<void> {
		await driverHelper.waitVisibility(By.xpath(`//div[@class='dialog-buttons']//a[text()='${buttonName}']`));
	}

	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		shellExecutor.executeCommand('oc project openshift-devspaces');

		// get current value of spec.devEnvironments.secondsOfRunBeforeIdling
		defaultRunTimeoutValue = Number(
			shellExecutor.executeCommand(
				`oc get checluster/${cheClusterName} -o "jsonpath={.spec.devEnvironments.secondsOfRunBeforeIdling}"`
			)
		);

		// set spec.devEnvironments.secondsOfRunBeforeIdling to SECONDS_OF_RUN_BEFORE_IDLING
		shellExecutor.executeCommand(
			`oc patch checluster ${cheClusterName} --type=merge ` +
				`-p '{"spec":{"devEnvironments":{"secondsOfRunBeforeIdling": ${SECONDS_OF_RUN_BEFORE_IDLING}}}}'`
		);
	});

	suiteTeardown(function (): void {
		// restore spec.devEnvironments.secondsOfRunBeforeIdling to original value
		shellExecutor.executeCommand(
			`oc patch checluster ${cheClusterName} --type=merge ` +
				`-p '{"spec":{"devEnvironments":{"secondsOfRunBeforeIdling": ${defaultRunTimeoutValue}}}}'`
		);
	});

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test(`Create and open new workspace, stack:${stackName}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspace(stackName);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Wait run timeout dialog and check Dialog buttons', async function (): Promise<void> {
		await driverHelper.waitVisibility(webCheCodeLocators.Dialog.details, SECONDS_OF_RUN_BEFORE_IDLING * 1000); // ms
		const dialog: ModalDialog = new ModalDialog();
		expect(await dialog.getDetails()).includes('Your workspace has stopped because it has reached the run timeout.');
		await checkDialogButton('Cancel');
		await checkDialogButton('Return to dashboard');
		await checkDialogButton('Restart your workspace');
	});

	test('Check that the workspace has Stopped state', async function (): Promise<void> {
		await dashboard.openDashboard();
		await dashboard.clickWorkspacesButton();
		await workspaces.waitPage();
		await workspaces.waitWorkspaceWithStoppedStatus(WorkspaceHandlingTests.getWorkspaceName());
	});

	suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
		await testWorkspaceUtil.deleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
