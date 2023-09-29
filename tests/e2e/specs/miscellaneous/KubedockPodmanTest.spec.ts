/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { ViewSection } from 'monaco-page-objects';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { CLASSES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { expect } from 'chai';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellString } from 'shelljs';

suite('Check possibility to manage containers within a workspace with kubedock and podman', function (): void {
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
	let workspaceName: string = '';

	const testScript: string =
		'# Enable Kubedock\n' +
		'export KUBEDOCK_ENABLED=true\n' +
		'echo KUBEDOCK_ENABLED\n' +
		'/entrypoint.sh\n' +
		'cd $PROJECT_SOURCE\n' +
		'export USER=$(oc whoami)\n' +
		'export TKN=$(oc whoami -t)\n' +
		'export REG="image-registry.openshift-image-registry.svc:5000"\n' +
		'export PROJECT=$(oc project -q)\n' +
		'export IMG="${REG}/${PROJECT}/hello"\n' +
		'podman login --tls-verify=false --username ${USER} --password ${TKN} ${REG}\n' +
		'podman build -t ${IMG} .\n' +
		'podman push --tls-verify=false ${IMG}\n' +
		'podman run --rm ${IMG}';

	const factoryUrl: string = 'https://github.com/l0rd/dockerfile-hello-world';

	loginTests.loginIntoChe();

	test(`Create and open new workspace from factory:${factoryUrl}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
	});

	test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(workspaceName, 'Workspace name was not fetched from the loading page').not.undefined;
	});

	test('Register running workspace', function (): void {
		registerRunningWorkspace(workspaceName);
	});

	test('Wait workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Check the project files were imported', async function (): Promise<void> {
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, 'Dockerfile'), 'Files not imported').not.undefined;
	});

	test('Create and check container runs using kubedock and podman', function (): void {
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
		const output: ShellString = kubernetesCommandLineToolsExecutor.executeCommand(testScript);
		expect(output, 'Podman test script failed').contains('Hello from Kubedock!');
	});

	test('Stop the workspace', async function (): Promise<void> {
		await workspaceHandlingTests.stopWorkspace(workspaceName);
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	test('Delete the workspace', async function (): Promise<void> {
		await workspaceHandlingTests.removeWorkspace(workspaceName);
	});

	loginTests.logoutFromChe();
});
