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

suite('Cpp devfile API test', function (): void {
	const devfilesRegistryHelper: DevfilesHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const devfileID: string = 'cpp';
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
	let devfileContext: DevfileContext;
	let devfileContent: string = '';
	let dwtName: string = '';
	const workDirPath: string = 'c-plus-plus/strings';
	const fileName: string = 'knuth_morris_pratt.cpp';
	const tasksJsonPath: string = 'c-plus-plus/.vscode/tasks.json';

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

	test('Check commands', function (): void {
		let output: ShellString;
		const toolsComponent: any = YAML.parse(devfileContent).components.find((component: any): boolean => component.name === 'tools');
		const containerName: string = toolsComponent ? toolsComponent.name : 'Component not found';
		Logger.info(`container from components section of Devfile:: ${containerName}`);
		const tasksJsonContent: string = containerTerminal.execInContainerCommand(`cat ${tasksJsonPath}`, containerName).stdout;
		const parsedJson: any = YAML.parse(tasksJsonContent);

		Logger.info('"Check \'build current algorithm\' command"');
		const buildTaskCommand: string = parsedJson.tasks[0].command;
		const buildCommand: string = buildTaskCommand.replaceAll('${file}', fileName).replaceAll(/'/g, '"');
		Logger.info(`commandLine from tasks.json file of project: ${buildCommand}`);
		const runCommandInBash: string = `cd ${workDirPath} && ${buildCommand}`;
		output = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);
		expect(output.stdout.trim()).contains('Build complete');

		Logger.info('"Check \'run current algorithm\' command"');
		const runTaskCommand: string = parsedJson.tasks[1].command;
		const runCommand: string = runTaskCommand.replaceAll('${file}', fileName).replaceAll(/'/g, '"');
		Logger.info(`commandLine from tasks.json file of project: ${runCommand}`);
		const runCommandInBash2: string = `cd ${workDirPath} && ${runCommand}`;
		output = containerTerminal.execInContainerCommand(runCommandInBash2, containerName);
		expect(output.code).eqls(0);
		expect(output.stdout.trim()).contains('Found');
	});

	suiteTeardown('Delete workspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace(dwtName);
	});
});
