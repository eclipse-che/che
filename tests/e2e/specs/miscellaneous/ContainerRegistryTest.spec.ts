/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { expect } from 'chai';
import { ViewSection } from 'monaco-page-objects';
import { ShellString } from 'shelljs';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { CONTAINER_REGISTRY_TEST_CONSTANTS } from '../../constants/CONTAINER_REGISTRY_TEST_CONSTANTS';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { ContainerRegistries } from '../../pageobjects/dashboard/ContainerRegistries';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { LoginTests } from '../../tests-library/LoginTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { registerRunningWorkspace } from '../MochaHooks';

suite(`Test private quay.io images using credentials from User Preferences ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
	const containerRegistries: ContainerRegistries = e2eContainer.get(CLASSES.ContainerRegistries);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);

	const registryUrl: string = CONTAINER_REGISTRY_TEST_CONSTANTS.TS_SELENIUM_CONTAINER_REGISTRY_URL;
	const registryUsername: string = CONTAINER_REGISTRY_TEST_CONSTANTS.TS_SELENIUM_CONTAINER_REGISTRY_USERNAME;
	const registryPassword: string = CONTAINER_REGISTRY_TEST_CONSTANTS.TS_SELENIUM_CONTAINER_REGISTRY_PASSWORD;
	const privateImage: string = CONTAINER_REGISTRY_TEST_CONSTANTS.TS_SELENIUM_CONTAINER_REGISTRY_IMAGE;
	const containerRunCapabilitiesPath: string = '/spec/devEnvironments/disableContainerRunCapabilities';
	const factoryUrl: string = BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()
		? FACTORY_TEST_CONSTANTS.TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL ||
			'https://gh.crw-qe.com/test-automation-only/dockerfile-hello-world'
		: FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL || 'https://github.com/crw-qe/dockerfile-hello-world';

	let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
	let workspaceName: string = '';
	let cheClusterName: string = '';
	let devSpacesNamespace: string = '';
	let originalContainerRunCapabilitiesSetting: string = '';
	let containerRunCapabilitiesConfigured: boolean = false;
	let containerRegistryAdded: boolean = false;

	const buildPushAndRunImageScript: string = `
set -e
cd "$PROJECT_SOURCE"
ARCH=$(uname -m)
REGISTRY="${registryUrl}"
IMG="${privateImage}"

podman login --get-login "$REGISTRY"
podman build -t "$IMG" -f "Dockerfile.$ARCH" .
podman push "$IMG"
podman rmi "$IMG"
podman run --rm "$IMG"
`;

	suiteSetup('Validate private registry configuration', function (): void {
		const missingVariables: string[] = [];
		if (!registryUsername) {
			missingVariables.push('TS_SELENIUM_CONTAINER_REGISTRY_USERNAME');
		}
		if (!registryPassword) {
			missingVariables.push('TS_SELENIUM_CONTAINER_REGISTRY_PASSWORD');
		}
		if (!privateImage) {
			missingVariables.push('TS_SELENIUM_CONTAINER_REGISTRY_IMAGE');
		}

		if (missingVariables.length > 0) {
			throw new Error(`Missing required environment variables: ${missingVariables.join(', ')}`);
		}
		if (!/^[a-z0-9.-]+(?::[0-9]+)?$/.test(registryUrl)) {
			throw new Error('TS_SELENIUM_CONTAINER_REGISTRY_URL must be a registry host without a protocol or path');
		}
		if (!/^[A-Za-z0-9./:_-]+$/.test(privateImage)) {
			throw new Error('TS_SELENIUM_CONTAINER_REGISTRY_IMAGE contains unsupported characters');
		}
		if (!privateImage.startsWith(`${registryUrl}/`)) {
			throw new Error(`TS_SELENIUM_CONTAINER_REGISTRY_IMAGE must belong to ${registryUrl}`);
		}
	});

	suiteSetup('Enable CheCluster container run capabilities', function (): void {
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.loginToOcp();

		devSpacesNamespace = shellExecutor
			.executeCommand('oc get checluster --all-namespaces -o jsonpath="{.items[0].metadata.namespace}"')
			.stdout.trim();
		expect(devSpacesNamespace, 'Dev Spaces namespace was not found').not.empty;

		cheClusterName = shellExecutor
			.executeCommand(`oc get checluster -n ${devSpacesNamespace} -o jsonpath='{.items[0].metadata.name}'`)
			.stdout.trim();
		expect(cheClusterName, 'CheCluster name was not found').not.empty;

		originalContainerRunCapabilitiesSetting = shellExecutor
			.executeCommand(
				`oc get checluster/${cheClusterName} -n ${devSpacesNamespace} -o jsonpath='{.spec.devEnvironments.disableContainerRunCapabilities}'`
			)
			.stdout.trim();

		const patchResult: ShellString = shellExecutor.executeCommand(
			`oc patch checluster/${cheClusterName} -n ${devSpacesNamespace} --type=merge ` +
				`-p '${JSON.stringify({ spec: { devEnvironments: { disableContainerRunCapabilities: false } } })}'`
		);
		expect(patchResult.code).to.equal(0, 'Failed to enable CheCluster container run capabilities');
		containerRunCapabilitiesConfigured = true;
	});

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Add quay.io credentials in User Preferences', async function (): Promise<void> {
		await containerRegistries.addContainerRegistry(registryUrl, registryUsername, registryPassword);
		containerRegistryAdded = true;
	});

	test(`Create and open new workspace from factory: ${factoryUrl}`, async function (): Promise<void> {
		await workspaceHandlingTests.createAndOpenWorkspaceFromGitRepository(factoryUrl);
	});

	test('Obtain and register the workspace name', async function (): Promise<void> {
		await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
		workspaceName = WorkspaceHandlingTests.getWorkspaceName();
		expect(workspaceName, 'Workspace name was not fetched from the loading page').not.empty;
		registerRunningWorkspace(workspaceName);
	});

	test('Wait for workspace readiness', async function (): Promise<void> {
		await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
	});

	test('Check the project files were imported', async function (): Promise<void> {
		const projectSection: ViewSection = await projectAndFileTests.getProjectViewSession();
		expect(await projectAndFileTests.getProjectTreeItem(projectSection, 'Dockerfile.x86_64'), 'Files not imported').not.undefined;
	});

	test('Connect to the workspace container', function (): void {
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
	});

	test('Build, push, and run the private quay.io image', function (): void {
		const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(buildPushAndRunImageScript);

		expect(output.code, 'Failed to build, push, and run the private image').to.equal(0);
		expect(output.stdout, 'Podman found credentials for an unexpected user').to.contain(registryUsername);
		expect(output.stdout, 'Expected output from the private image was not found').to.contain('Hello from Kubedock!');
	});

	suiteTeardown('Delete quay.io credentials from User Preferences', async function (): Promise<void> {
		if (!containerRegistryAdded) {
			return;
		}

		await containerRegistries.deleteContainerRegistry(registryUrl);
	});

	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		if (workspaceName) {
			await dashboard.openDashboard();
			await browserTabsUtil.closeAllTabsExceptCurrent();
		}
	});

	suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
		if (workspaceName) {
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(workspaceName);
		}
	});

	suiteTeardown('Unregister running workspace', function (): void {
		registerRunningWorkspace('');
	});

	suiteTeardown('Restore CheCluster container run capabilities', function (): void {
		if (!containerRunCapabilitiesConfigured) {
			return;
		}

		const restoreCommand: string = ['true', 'false'].includes(originalContainerRunCapabilitiesSetting)
			? `oc patch checluster/${cheClusterName} -n ${devSpacesNamespace} --type=merge ` +
				`-p '${JSON.stringify({
					spec: { devEnvironments: { disableContainerRunCapabilities: originalContainerRunCapabilitiesSetting === 'true' } }
				})}'`
			: `oc patch checluster/${cheClusterName} -n ${devSpacesNamespace} --type=json ` +
				`-p '${JSON.stringify([{ op: 'remove', path: containerRunCapabilitiesPath }])}'`;
		const restoreResult: ShellString = shellExecutor.executeCommand(restoreCommand);

		expect(restoreResult.code).to.equal(0, 'Failed to restore CheCluster container run capabilities setting');
	});
});
