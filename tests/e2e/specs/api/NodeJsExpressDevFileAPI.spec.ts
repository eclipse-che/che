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
	let devfileName: string = '';

	suiteSetup(`Prepare login ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	test(`Create ${devfileID} workspace`, async function (): Promise<void> {
		const randomPref: string = crypto.randomBytes(4).toString('hex');
		kubernetesCommandLineToolsExecutor.namespace = API_TEST_CONSTANTS.TS_API_TEST_NAMESPACE || 'admin-devspaces';
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
		expect(outputText).contains('Run `npm audit` for details.');
	});

	test('Check "run the web app" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[0].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[1].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[1].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		let runCommandInBash: string = commandLine.replaceAll('$', '\\$'); // don't wipe out env. vars like "${PROJECTS_ROOT}"
		if (workdir !== undefined && workdir !== '') {
			runCommandInBash = `cd ${workdir} && ` + runCommandInBash;
		}

		const output: ShellString = containerTerminal.execInContainerCommandWithTimeout(runCommandInBash, containerName);
		expect(output.code).eqls(124);

		const outputText: string = output.stdout.trim();
		expect(outputText).contains('Example app listening on port 3000!');
	});

	test('Check "stop the web app" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[0].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[4].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[4].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		// Pretrier changes next line to `replaceAll("'", "'\"'\"'")` that throws an error from eslint.
		// prettier-ignore
		let runCommandInBash: string = commandLine.replaceAll('\'', '\'\"\'\"\'');

		if (workdir !== undefined && workdir !== '') {
			runCommandInBash = `cd ${workdir} && ` + runCommandInBash;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const outputText: string = output.stdout.trim();
		expect(outputText).contains('Done.');
	});

	test('Check "Run the web app (debugging enabled)" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[0].exec.component;

		const workdir: string = YAML.parse(devfileContent).commands[3].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[3].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		let runCommandInBash: string = commandLine.replaceAll('$', '\\$'); // don't wipe out env. vars like "${PROJECTS_ROOT}"
		if (workdir !== undefined && workdir !== '') {
			runCommandInBash = `cd ${workdir} && ` + runCommandInBash;
		}

		const output: ShellString = containerTerminal.execInContainerCommandWithTimeout(runCommandInBash, containerName);
		expect(output.code).eqls(124);

		const outputText: string = output.stdout.trim();
		expect(outputText).contains('Example app listening on port 3000!');
	});

	suiteTeardown('Delete workspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace(devfileName);
	});
});
