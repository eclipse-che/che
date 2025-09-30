/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { LoginTests } from '../../tests-library/LoginTests';
import { ActivityBar, SideBarView, ViewItem, ViewSection } from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
import { Logger } from '../../utils/Logger';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { DriverHelper } from '../../utils/DriverHelper';
import { error } from 'selenium-webdriver';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';

suite(
	`Create a workspace via launching a factory from the ${FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_REPO_URL} repository`,
	function (): void {
		const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
		const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
		const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
		const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
		const pathToPluginRegistry: string = '/projects/devspaces/dependencies/che-plugin-registry/';
		const internalRegistry: string = 'image-registry.openshift-image-registry.svc:5000';
		const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
		const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
		const viewItems: ViewItem[] | undefined = [];
		const testRepoProjectName: string = 'devspaces';
		const appVersion: string = BASE_TEST_CONSTANTS.TESTING_APPLICATION_VERSION
			? BASE_TEST_CONSTANTS.TESTING_APPLICATION_VERSION.split('.').slice(0, 2).join('.')
			: 'next';
		const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
		let currentNamespace: string | undefined = '';
		let cheClusterNamespace: string | undefined = '';
		let cheClusterName: string | undefined = '';
		let projectSection: ViewSection;
		let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
		let workspaceName: string = '';
		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		test('Navigate to the factory URL', async function (): Promise<void> {
			await browserTabsUtil.navigateTo(
				FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_URL() || 'https://github.com/redhat-developer/devspaces/'
			);
			await createWorkspace.performTrustAuthorPopup();
		});

		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
			workspaceName = WorkspaceHandlingTests.getWorkspaceName();
			expect(workspaceName, 'Workspace name was not fetched from the loading page').not.undefined;
		});

		test('Registering the running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Wait the workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Check if a project folder has been created', async function (): Promise<void> {
			Logger.debug(`new SideBarView().getContent().getSection: get ${testRepoProjectName}`);
			projectSection = await projectAndFileTests.getProjectViewSession();
			expect(await projectAndFileTests.getProjectTreeItem(projectSection, testRepoProjectName), 'Project folder was not imported').not
				.undefined;
		});
		test('Accept the project as a trusted one', async function (): Promise<void> {
			await projectAndFileTests.performTrustAuthorDialog();
		});
		test('Remove VSX plugins and set redhat.vscode-xml', function (): void {
			// modify the openvsx-sync.json file with just one item - for speeding up the build an image
			kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
			kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
			kubernetesCommandLineToolsExecutor.loginToOcp('admin');
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
			const pathToOpenVSXOriginFile: string = `${pathToPluginRegistry}openvsx-sync.json`;
			const pathToOpenVSXTmpFile: string = `${pathToPluginRegistry}temp.json`;
			const editVSCListCommand: string = `jq "[.[] | select(.id == \\"redhat.vscode-xml\\")]" ${pathToOpenVSXOriginFile} > ${pathToOpenVSXTmpFile} && mv ${pathToOpenVSXTmpFile} ${pathToOpenVSXOriginFile}`;
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(editVSCListCommand);
			expect(output.code).to.equals(0);
			const getCheClusterNamespaceCommand: string = 'oc get checluster --all-namespaces -o json | jq -r ".items[0].metadata.name"';
			const getCheClusterNameSpace: string = 'oc get checluster --all-namespaces -o json | jq -r ".items[0].metadata.namespace"';

			// get che cluster name and namespace and init variables
			cheClusterName = kubernetesCommandLineToolsExecutor.execInContainerCommand(getCheClusterNamespaceCommand).replace('\n', '');
			cheClusterNamespace = kubernetesCommandLineToolsExecutor.execInContainerCommand(getCheClusterNameSpace).replace('\n', '');
		});

		test('Login podman into official registry', function (): void {
			const commandToAuthenticateInRegistry: string = `podman login -u \"${process.env.REGISTRY_LOGIN}\" -p ${process.env.REGISTRY_PASSWORD} registry.redhat.io`;
			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(commandToAuthenticateInRegistry);
			expect(output).contains('Login Succeeded!');
		});

		test('Build and push custom image', function (): void {
			currentNamespace = kubernetesCommandLineToolsExecutor.namespace;
			const podmanPushCommand: string = `podman push ${internalRegistry}/${currentNamespace}/che-plugin-registry:${appVersion}`;
			const commandToBuildCustomVSXImage: string = `cd ${pathToPluginRegistry} && yes | ./build.sh`;
			const commandLoginIntoInternalRegistry: string =
				'podman login -u $(oc whoami | tr -d :) -p $(oc whoami -t) image-registry.openshift-image-registry.svc:5000';
			const retagImageCommand: string = `podman tag quay.io/devspaces/pluginregistry-rhel9:next  ${internalRegistry}/${currentNamespace}/che-plugin-registry:${appVersion}`;

			const output: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(commandToBuildCustomVSXImage);
			expect(output.code).equals(0);

			const outputRetagCommand: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(retagImageCommand);
			expect(outputRetagCommand.code).equals(0);

			const loginIntoInternalRegistryOutput: ShellString =
				kubernetesCommandLineToolsExecutor.execInContainerCommand(commandLoginIntoInternalRegistry);
			expect(loginIntoInternalRegistryOutput).contains('Login Succeeded!');

			const outputPushCommand: ShellString = kubernetesCommandLineToolsExecutor.execInContainerCommand(podmanPushCommand);
			expect(outputPushCommand.code).equals(0);
		});

		test('Configure Che to use the embedded Eclipse Open VSX server', function (): void {
			// create secret for using internal registry
			kubernetesCommandLineToolsExecutor.execInContainerCommand(`oc delete secret regcred -n ${cheClusterNamespace} || true`);

			const createRegistrySecretCommand: string = `oc create secret -n ${cheClusterNamespace} docker-registry regcred --docker-server=${internalRegistry} --docker-username=\$(oc whoami | tr -d :) --docker-password=\$(oc whoami -t)`;
			const createSecretOutput: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(createRegistrySecretCommand);
			expect(createSecretOutput).contains('regcred created');

			// add secret to service account
			const patchPart: string = '{\\"imagePullSecrets\\": [{\\"name\\": \\"regcred\\"}]}';
			const addSecretToServiceAccountCommand: string = `oc patch serviceaccount default -n ${cheClusterNamespace} -p "${patchPart}"`;
			const addSecretOutput: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(addSecretToServiceAccountCommand);
			expect(addSecretOutput).contains('default patched');

			// patch che cluster with custom plugin registry
			const patchVSXRegistryCommand: string = `cd ${pathToPluginRegistry} && ./patch-cluster.sh  ${internalRegistry}/${currentNamespace}/che-plugin-registry:${appVersion}`;
			const patchVSXOutput: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(patchVSXRegistryCommand);
			expect(patchVSXOutput).contains('Patched CheCluster ');

			// wait for deployment readiness after patching CHE cluster
			const waitDeploymentReadiness: string = `oc rollout status deployment plugin-registry -n ${cheClusterNamespace} --timeout=120s`;
			const waitDeploymentOutput: string = kubernetesCommandLineToolsExecutor.execInContainerCommand(waitDeploymentReadiness);
			expect(waitDeploymentOutput).contains('successfully rolled out');
		});

		test('Recreate workspace and check VSX custom plugin ', async function (): Promise<void> {
			// await this.driver.sleep(30000);
			await driverHelper.wait(10000);

			// await testWorkspaceUtil.deleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
			// registerRunningWorkspace('');
			await browserTabsUtil.navigateTo('https://github.com/eclipse-che/che');
		});
		test('Registering the running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Check Custom VSX plugin', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
			Logger.debug('new SideBarView().getContent().getSection: get che');
			projectSection = await projectAndFileTests.getProjectViewSession();
			expect(await projectAndFileTests.getProjectTreeItem(projectSection, 'che'), 'Project folder was not imported').not.undefined;
			await projectAndFileTests.performTrustDialogs();
			const extensionsView: SideBarView | undefined = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
			await driverHelper.wait(TIMEOUT_CONSTANTS.TS_COMMON_DASHBOARD_WAIT_TIMEOUT);
			const sections: ViewSection[] | undefined = await extensionsView?.getContent().getSections();
			if (sections !== undefined) {
				for (const section of sections) {
					let currentItem: ViewItem[] = [];
					// sometimes the item (not related with custom VSX) can be empty or not extended, but it is not an error for the test
					// we do not need to control none VSX items
					try {
						currentItem = await section.getVisibleItems();
					} catch (err) {
						if (err instanceof error.ElementNotInteractableError) {
							Logger.error('Current item is not interactable but test will continue');
						} else {
							throw err;
						}
					}
					if (currentItem.length > 0) {
						viewItems?.push(...currentItem);
					}
				}
			}
			expect(viewItems.length).equals(1);
			expect(await viewItems[0].getText()).contains('XML Language Support by Red Hat');
		});

		suiteTeardown('Clean up resources and restore default CHE cluster CR', async function (): Promise<void> {
			kubernetesCommandLineToolsExecutor.getPodAndContainerNames();
			try {
				kubernetesCommandLineToolsExecutor.execInContainerCommand(`oc delete secret regcred -n ${cheClusterNamespace}`);
			} catch (error) {
				Logger.error(`Error during deleting the secret: ${error}`);
			}
			try {
				const patch: string = '[{"op": "remove", "path": "/imagePullSecrets", "value": {"name": "regcred"}}]';
				const commandForRestoreServiceAccount: string = `oc patch serviceaccount default -n  ${cheClusterNamespace} --type=json -p="${patch}"`;
				kubernetesCommandLineToolsExecutor.execInContainerCommand(commandForRestoreServiceAccount);
			} catch (error) {
				Logger.error(`Error during restoring the service account: ${error}`);
			}
			try {
				const patchDeployment: string = '[{"op": "remove", "path": "/spec/components/pluginRegistry/deployment"}]';
				const patchPluginRegistry: string = '[{"op": "remove", "path": "/spec/components/pluginRegistry/openVSXURL"}]';
				const restoreDefaultDeploymentPluginRegistry: string = `oc patch checluster ${cheClusterName} --type=json -p="${patchDeployment}" -n ${cheClusterNamespace}`;
				const restoreOpenVSXRegistry: string = `oc patch checluster ${cheClusterName} --type=json -p="${patchPluginRegistry}" -n ${cheClusterNamespace}`;
				kubernetesCommandLineToolsExecutor.execInContainerCommand(restoreDefaultDeploymentPluginRegistry);
				kubernetesCommandLineToolsExecutor.execInContainerCommand(restoreOpenVSXRegistry);
			} catch (error) {
				Logger.error(`Error during restoring the VSX registry: ${error}`);
			}
			try {
				await testWorkspaceUtil.deleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
			} catch (error) {
				Logger.error(`Cannot delete the workspace by API deleting the workspace: ${error}`);
			}
		});
	}
);
