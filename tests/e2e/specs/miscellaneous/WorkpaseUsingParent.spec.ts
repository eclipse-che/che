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
import { InputBox, QuickOpenBox, QuickPickItem, SideBarView, ViewItem, ViewSection, Workbench } from 'monaco-page-objects';
import { CLASSES } from '../../configs/inversify.types';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { assert, expect } from 'chai';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import { ShellString } from 'shelljs';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from '../MochaHooks';
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
function executeArbitraryShellScript(command: string): string {
	const output: ShellString = shellExecutor.executeCommand(command);
	if (output.stderr.length > 0) {
		assert.fail(output.stderr);
	}
	return output.stdout;
}
suite('Workspace using a parent test suite', function (): void {
	loginTests.loginIntoChe();
	let podName: string = '';

	test('Create a workspace using a parent', async function (): Promise<void> {
		const factoryUrl: string = `${BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL}/dashboard/#https://github.com/testsfactory/parentDevfile`;
		await dashboard.waitPage();
		await browserTabsUtil.navigateTo(factoryUrl);
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		// sometimes the trust dialog does not appear at first time, for avoiding this problem we send click event for activating
		await new Workbench().click();
		await projectAndFileTests.performTrustAuthorDialog();
	});

	test('Check cloning of the test project', async function (): Promise<void> {
		const expectedProjectItems: string[] = ['.devfile.yaml', 'parent.yaml', 'README.md'];
		const visibleContent: ViewSection = await new SideBarView().getContent().getSection('parentdevfile');

		for (const expectedProjectItem of expectedProjectItems) {
			const visibleItem: ViewItem | undefined = await visibleContent.findItem(expectedProjectItem);
			expect(visibleItem).not.equal(undefined);
		}
	});

	test('Check devfile VS Code tasks ', async function (): Promise<void> {
		const input: QuickOpenBox | InputBox = await new Workbench().openCommandPrompt();
		await input.setText('>Tasks: Run Task');
		const runTaskItem: QuickPickItem | undefined = await input.findQuickPick('Tasks: Run Task');
		await runTaskItem?.click();
		const devFileTask: QuickPickItem | undefined = await input.findQuickPick('devfile');
		await devFileTask?.click();
		const firstExpectedQuickPick: QuickPickItem | undefined = await input.findQuickPick('1. This command from the devfile');
		const secondExpectedQuickPick: QuickPickItem | undefined = await input.findQuickPick('2. This command from the parent');
		expect(firstExpectedQuickPick).not.eqls(undefined);
		expect(secondExpectedQuickPick).not.eqls(undefined);
	});

	test('Check expected containers in the parent POD', function (): void {
		const getPodNameCommand: string = `${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} get pods --selector=controller.devfile.io/devworkspace_name=sample-using-parent --output jsonpath=\'{.items[0].metadata.name}\'`;

		podName = executeArbitraryShellScript(getPodNameCommand);
		const containerNames: string = executeArbitraryShellScript(
			`${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} get pod ${podName} --output jsonpath=\'{.spec.containers[*].name}\'`
		);
		expect(containerNames).contain('tools');
		expect(containerNames).contains('che-gateway');

		const initContainerName: string = executeArbitraryShellScript(
			`${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} get pod ${podName} --output jsonpath=\'{.spec.initContainers[].name}\'`
		);
		expect(initContainerName).contain('che-code-injector');
	});

	test('Check expected environment variables', function (): void {
		const envList: string = executeArbitraryShellScript(
			`${API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL} exec -i ${podName} -c tools -- sh -c env`
		);
		expect(envList).contain('DEVFILE_ENV_VAR=true');
		expect(envList).contain('PARENT_ENV_VAR=true');
	});
});
