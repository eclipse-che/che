/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { StringUtil } from '../../utils/StringUtil';
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';

suite('Empty workspace API test', function (): void {
	// works only for root user
	const namespace: string | undefined = API_TEST_CONSTANTS.TS_API_TEST_NAMESPACE;
	let clonedProjectName: string;
	let containerWorkDir: string;
	let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
	let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
	let devfileContext: DevfileContext;
	let devWorkspaceName: string | undefined;
	let containerTerminal: ContainerTerminal;

	const gitRepository: string = 'https://github.com/crw-qe/web-nodejs-sample';

	suiteSetup(`Create empty workspace with OC client ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, async function (): Promise<void> {
		const workspaceName: string = 'empty-' + Math.floor(Math.random() * 1000);
		const devfileContent: string = 'schemaVersion: 2.2.0\n' + 'metadata:\n' + `  name: ${workspaceName}\n`;

		devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
			devfileContent
		});
		devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
		devWorkspaceName = devfileContext?.devWorkspace?.metadata?.name;
		kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
		kubernetesCommandLineToolsExecutor.workspaceName = devWorkspaceName;
		kubernetesCommandLineToolsExecutor.namespace = namespace;
		containerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	});

	test('Create empty workspace', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		const devWorkspaceConfigurationYamlString: string =
			devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
		const output: ShellString = kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(devWorkspaceConfigurationYamlString);
		expect(output.stdout).contains('condition met');
	});

	suite(`Clone public repo without previous setup ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
		test('Check if public repo can be cloned', function (): void {
			containerWorkDir = containerTerminal.pwd().stdout.replace('\n', '');
			const cloneOutput: ShellString = containerTerminal.gitClone(gitRepository);
			expect(cloneOutput.stdout + cloneOutput.stderr).includes('Cloning');
		});

		test('Check if project was created', function (): void {
			clonedProjectName = StringUtil.getProjectNameFromGitUrl(gitRepository);
			expect(containerTerminal.ls().stdout).includes(clonedProjectName);
		});

		test('Check if project files are imported', function (): void {
			expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes(
				BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME
			);
		});

		suiteTeardown('Delete cloned project', function (): void {
			containerTerminal.removeFolder(`${clonedProjectName}`);
		});
	});

	suiteTeardown('Delete workspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});
});
