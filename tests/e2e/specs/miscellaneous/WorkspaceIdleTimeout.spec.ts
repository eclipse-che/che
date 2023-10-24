/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
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
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { DriverHelper } from '../../utils/DriverHelper';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { Locators, ModalDialog } from 'monaco-page-objects';
import { expect } from 'chai';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

suite('"Check workspace idle timeout" test', function (): void {
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
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

	const stackName: string = 'Empty Workspace';
	const cheClusterName: string = 'devspaces';
	let stopWorkspaceTimeout: number = 0;

	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp('admin');
		shellExecutor.executeCommand('oc project openshift-devspaces');

		// get current value of spec.devEnvironments.secondsOfInactivityBeforeIdling
		stopWorkspaceTimeout = Number(
			shellExecutor.executeCommand(
				`oc get checluster/${cheClusterName} -o "jsonpath={.spec.devEnvironments.secondsOfInactivityBeforeIdling}"`
			)
		);

		// set spec.devEnvironments.secondsOfInactivityBeforeIdling to 60
		shellExecutor.executeCommand(
			`oc patch checluster ${cheClusterName} --type=merge -p '{"spec":{"devEnvironments":{"secondsOfInactivityBeforeIdling": 60}}}'`
		);
	});

	suiteTeardown(function (): void {
		// restore spec.devEnvironments.secondsOfInactivityBeforeIdling to original value
		shellExecutor.executeCommand(
			`oc patch checluster ${cheClusterName} --type=merge -p '{"spec":{"devEnvironments":{"secondsOfInactivityBeforeIdling": ${stopWorkspaceTimeout}}}}'`
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

	test('Wait idle timeout dialog and click on "Return to Dashboard" button', async function (): Promise<void> {
		await driverHelper.waitVisibility(webCheCodeLocators.Dialog.details, TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
		const dialog: ModalDialog = new ModalDialog();
		expect(await dialog.getDetails()).includes('Your workspace has stopped due to inactivity.');
		await dialog.pushButton('Return to dashboard');
	});

	test('Check that the workspace has Stopped state', async function (): Promise<void> {
		await dashboard.waitPage();
		await workspaces.waitWorkspaceWithStoppedStatus(WorkspaceHandlingTests.getWorkspaceName());
	});

	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	suiteTeardown('Stop and delete the workspace by API', function (): void {
		testWorkspaceUtil.deleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
