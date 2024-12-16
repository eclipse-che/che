/** *******************************************************************
 * copyright (c) 2024 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { DevfilesHelper } from '../../utils/DevfilesHelper';
import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { ShellString } from 'shelljs';
import { expect } from 'chai';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import YAML from 'yaml';
import { Logger } from '../../utils/Logger';
import crypto from 'crypto';

suite('NodeJS Express devfile API test', function (): void {
	const devfilesRegistryHelper: DevfilesHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const devfileID: string = 'nodejs-express';
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
	let devfileContext: DevfileContext;
	let devfileContent: string = '';

	function startCommand(devfileContent: string, commandNubmer: number, expectedMessage: string, timeout: string = '0'): void {
		let output: ShellString = new ShellString('');
		const workdir: string = YAML.parse(devfileContent).commands[commandNubmer].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[commandNubmer].exec.commandLine;
		const containerName: string = YAML.parse(devfileContent).commands[commandNubmer].exec.component;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);
		const runCommandInBash: string = `cd ${workdir} && ${commandLine}`;

		if (timeout !== '0') {
			output = containerTerminal.execInContainerCommandWithTimeout(runCommandInBash, containerName);
			expect(output.code).eqls(124);
		} else {
			output = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
			expect(output.code).eqls(0);
		}

		expect(output.stdout.trim()).contains(expectedMessage);
	}

	suiteSetup(`Prepare login ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	test(`Create  ${devfileID} workspace`, async function (): Promise<void> {
		const randomPref: string = crypto.randomBytes(4).toString('hex');
		kubernetesCommandLineToolsExecutor.namespace = API_TEST_CONSTANTS.TS_API_TEST_NAMESPACE || 'admin-devspaces';
		devfileContent = devfilesRegistryHelper.getDevfileContent(devfileID);
		const editorDevfileContent: string = devfilesRegistryHelper.obtainCheDevFileEditorFromCheConfigMap('editors-definitions');
		const uniqName: string = YAML.parse(devfileContent).metadata.name + randomPref;
		kubernetesCommandLineToolsExecutor.workspaceName = uniqName;

		devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
			editorContent: editorDevfileContent,
			devfileContent: devfileContent
		});
		devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
		if (devfileContext.devWorkspace.metadata) {
			devfileContext.devWorkspace.metadata.name = uniqName;
		}
		const devWorkspaceConfigurationYamlString: string =
			devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
		const output: ShellString = kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(devWorkspaceConfigurationYamlString);
		expect(output.stdout).contains('condition met');
	});

	test('Check running application', function (): void {
		// check `npm install` command
		startCommand(devfileContent, 0, 'Run `npm audit` for details.');

		// check `run` command
		startCommand(devfileContent, 1, 'Example app listening on port 3000!', '15');
	});

	suiteTeardown('Delete workspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});
});
