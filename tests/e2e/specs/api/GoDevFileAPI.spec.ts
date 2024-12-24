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

suite('Go devfile API test', function (): void {
	const devfilesRegistryHelper: DevfilesHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const devfileID: string = 'go';
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
	let devfileContext: DevfileContext;
	let devfileContent: string = '';
	let dwtName: string = '';

	suiteSetup(`Prepare login ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	test(`Create  ${devfileID} workspace`, async function (): Promise<void> {
		const randomPref: string = crypto.randomBytes(4).toString('hex');
		kubernetesCommandLineToolsExecutor.namespace = API_TEST_CONSTANTS.TS_API_TEST_NAMESPACE || 'admin-devspaces';
		devfileContent = devfilesRegistryHelper.getDevfileContent(devfileID);
		const editorDevfileContent: string = devfilesRegistryHelper.obtainCheDevFileEditorFromCheConfigMap('editors-definitions');
		dwtName = YAML.parse(devfileContent).metadata.name;
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

	test('Check devfile commands', function (): void {
		const workdir: string = YAML.parse(devfileContent).commands[0].exec.workingDir;
		const buildCommandLine: string = YAML.parse(devfileContent).commands[0].exec.commandLine;
		const containerName: string = YAML.parse(devfileContent).commands[0].exec.component;

		Logger.info('"Test \'build\' command execution"');
		Logger.info(`workdir from exec section of DevFile: ${workdir}`);
		Logger.info(`commandLine from exec section of DevFile: ${buildCommandLine}`);
		const runCommandInBash: string = `cd ${workdir} && ${buildCommandLine}`;
		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		Logger.info(`Output stderr: ${output.stderr}`);
		expect(output.code).eqls(0);
		expect(output.stderr.trim()).contains('go: downloading');

		Logger.info('"Test \'run\' command execution"');
		const runCommandLine: string = YAML.parse(devfileContent).commands[1].exec.commandLine;
		Logger.info(`commandLine from exec section of DevFile: ${runCommandLine}`);
		const runCommandInBash2: string = `cd ${workdir} && sh -c "(${runCommandLine} > server.log 2>&1 &) && exit"`;
		const output2: ShellString = containerTerminal.execInContainerCommand(runCommandInBash2, containerName);
		expect(output2.code).eqls(0);
		const logOutput: ShellString = containerTerminal.execInContainerCommand(`cat ${workdir}/server.log`, containerName);
		Logger.info(`Log output: ${logOutput.stdout}`);
		expect(logOutput.stdout.trim()).contains('Web server running on port 8080');
	});

	suiteTeardown('Delete DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace(dwtName);
	});
});
