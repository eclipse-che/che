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

suite('Quarkus devfile API test', function (): void {
	const devfilesRegistryHelper: DevfilesHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const devfileID: string = 'quarkus-rest-api';
	const containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
	let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
	let devfileContext: DevfileContext;
	let devfileContent: string = '';
	let devfileName: string = '';

	suiteSetup(`Prepare login ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	test(`Create ${devfileID} workspace`, async function (): Promise<void> {
		const randomPref: string = crypto.randomBytes(4).toString('hex');
		kubernetesCommandLineToolsExecutor.namespace = API_TEST_CONSTANTS.TS_API_TEST_NAMESPACE;
		devfileContent = devfilesRegistryHelper.getDevfileContent(devfileID);
		const editorDevfileContent: string = devfilesRegistryHelper.obtainCheDevFileEditorFromCheConfigMap('editors-definitions');
		devfileName = YAML.parse(devfileContent).metadata.name;
		const uniqueName: string = YAML.parse(devfileContent).metadata.name + randomPref;
		kubernetesCommandLineToolsExecutor.workspaceName = uniqueName;

		devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
			editorContent: editorDevfileContent,
			devfileContent: devfileContent
		});
		devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
		if (devfileContext.devWorkspace.metadata) {
			devfileContext.devWorkspace.metadata.name = uniqueName;
		}
		const devWorkspaceConfigurationYamlString: string =
			devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
		const output: ShellString = kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(devWorkspaceConfigurationYamlString);
		expect(output.stdout).contains('condition met');
	});

	test('Check packaging application', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[0].exec.component;

		if (BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED()) {
			Logger.info('Test cluster is disconnected. Init Java Truststore...');
			const initJavaTruststoreCommand: string =
				'cp /home/user/init-java-truststore.sh /tmp && chmod +x /tmp/init-java-truststore.sh && /tmp/init-java-truststore.sh';
			const output: ShellString = containerTerminal.execInContainerCommand(initJavaTruststoreCommand, containerName);
			expect(output.code).eqls(0);
		}

		const workdir: string = YAML.parse(devfileContent).commands[0].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[0].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		let runCommandInBash: string = commandLine.replaceAll('$', '\\$'); // don't wipe out env. vars like "${PROJECTS_ROOT}"
		if (workdir !== undefined && workdir !== '') {
			runCommandInBash = `cd ${workdir} && ` + runCommandInBash;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const outputText: string = output.stdout.trim();
		expect(outputText).contains('BUILD SUCCESS');
	});

	test('Check running application', function (): void {
		const workdir: string = YAML.parse(devfileContent).commands[1].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[1].exec.commandLine;
		const containerName: string = YAML.parse(devfileContent).commands[1].exec.component;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		let runCommandInBash: string = commandLine.replaceAll('$', '\\$'); // don't wipe out env. vars like "${PROJECTS_ROOT}"
		if (workdir !== undefined && workdir !== '') {
			runCommandInBash = `cd ${workdir} && sh -c "(` + runCommandInBash + ' > command.log 2>&1 &) && sleep 15s && exit"';
		}

		let commandOutput: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(commandOutput.code).eqls(0);

		commandOutput = containerTerminal.execInContainerCommand(`cat ${workdir}/command.log`, containerName);
		const commandLog: string = commandOutput.stdout.trim();

		expect(commandLog).contains('Listening for transport dt_socket at address: 5005');
	});

	suiteTeardown('Delete workspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace(devfileName);
	});
});
