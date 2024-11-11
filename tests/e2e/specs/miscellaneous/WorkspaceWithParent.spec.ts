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
import { ShellExecutor } from '../../utils/ShellExecutor';
import { InputBox, QuickOpenBox, QuickPickItem, ViewItem, ViewSection, Workbench } from 'monaco-page-objects';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { expect } from 'chai';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { DriverHelper } from '../../utils/DriverHelper';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';

suite(`Workspace using a parent test suite ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	let podName: string = '';

	suiteSetup(function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.namespace = BASE_TEST_CONSTANTS.TEST_NAMESPACE;
	});

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Create a workspace from child devfile', async function (): Promise<void> {
		const factoryUrl: string =
			FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL.length === 0
				? BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL + '/dashboard/#/' + 'https://github.com/testsfactory/parentDevfile'
				: BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL + '/dashboard/#/' + FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL;
		await dashboard.waitPage();
		await testWorkspaceUtil.switchOffTrustDialogWithJavaScript();
		await browserTabsUtil.navigateTo(factoryUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		// add 10 sec timeout for waiting for finishing animation of all IDE parts (Welcome parts. bottom widgets. etc.)
		// using 10 sec easier than performing of finishing animation  of all elements
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Check cloning of the test project', async function (): Promise<void> {
		const expectedProjectItems: string[] = ['.devfile.yaml', 'parent.yaml', 'README.md', 'parentdevfile'];
		const visibleContent: ViewSection = await projectAndFileTests.getProjectViewSession();
		for (const expectedProjectItem of expectedProjectItems) {
			const visibleItem: ViewItem | undefined = await projectAndFileTests.getProjectTreeItem(visibleContent, expectedProjectItem);
			expect(visibleItem).not.undefined;
		}
	});

	test('Check devfile VS Code tasks ', async function (): Promise<void> {
		const input: QuickOpenBox | InputBox = await new Workbench().openCommandPrompt();
		await input.setText('>Tasks: Run Task');
		const runTaskItem: QuickPickItem | undefined = await input.findQuickPick('Tasks: Run Task');
		await runTaskItem?.click();
		// pause for avoiding StaleElement exception. It is easier solution than try/catch or writing separate function for this
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		const devFileTask: QuickPickItem | undefined = await input.findQuickPick('devfile');
		await devFileTask?.click();
		// pause for avoiding StaleElement exception. It is easier solution than try/catch or writing separate function for this
		await driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		const firstExpectedQuickPick: QuickPickItem | undefined = await input.findQuickPick('1. This command from the devfile');
		const secondExpectedQuickPick: QuickPickItem | undefined = await input.findQuickPick('2. This command from the parent');
		expect(firstExpectedQuickPick).not.undefined;
		expect(secondExpectedQuickPick).not.undefined;
	});

	test('Check expected containers in the parent POD', function (): void {
		const getPodNameCommand: string = `${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} get pods -n ${kubernetesCommandLineToolsExecutor.namespace} --selector=controller.devfile.io/devworkspace_name=sample-using-parent --output jsonpath=\'{.items[0].metadata.name}\'`;

		podName = shellExecutor.executeArbitraryShellScript(getPodNameCommand);
		const containerNames: string = shellExecutor.executeArbitraryShellScript(
			`${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} get pod ${podName} -n ${kubernetesCommandLineToolsExecutor.namespace} --output jsonpath=\'{.spec.containers[*].name}\'`
		);
		expect(containerNames).contains('tools').and.contains('che-gateway');

		const initContainerName: string = shellExecutor.executeArbitraryShellScript(
			`${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} get pod ${podName} -n ${kubernetesCommandLineToolsExecutor.namespace} --output jsonpath=\'{.spec.initContainers[].name}\'`
		);
		expect(initContainerName).contains('che-code-injector');
	});

	test('Check expected environment variables', function (): void {
		const envList: string = shellExecutor.executeArbitraryShellScript(
			`${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} -n admin-devspaces exec -i ${podName} -c tools -- sh -c env`
		);
		expect(envList).contains('DEVFILE_ENV_VAR=true').and.contains('PARENT_ENV_VAR=true');
	});

	suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});
});
