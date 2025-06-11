/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
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

suite('Ansible devfile API test', function (): void {
	const devfilesRegistryHelper: DevfilesHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	const devfileID: string = 'ansible';
	const serviceNameToPortForward: string = BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME() + '-dashboard';
	// namespace where the application is deployed to port-forward the service
	const applicationNamespace: string = 'openshift-' + BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME();
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

		Logger.info('Inject kubeconfig to workspace');
		kubernetesCommandLineToolsExecutor.startTcpPortForward(serviceNameToPortForward, applicationNamespace);
		const devworkspaceId: string = kubernetesCommandLineToolsExecutor.getDevWorkspaceId();
		kubernetesCommandLineToolsExecutor.injectKubeConfig(devworkspaceId);
	});

	test('Check "molecule-create" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[0].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[0].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[0].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		const safeCommandLine: string = commandLine.replace(
			'/source\s+\$HOME\/\.bashrc/',
			'[ -f "$HOME/.bashrc" ] && . "$HOME/.bashrc" || true'
		);

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${safeCommandLine}`;
		} else {
			runCommandInBash = `${safeCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const recapBlocks: string[] = output.stdout.split(/PLAY RECAP/g).slice(1);
		recapBlocks.forEach((block): void => {
			expect(block).match(/failed\s*=\s*0/);
		});

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('was installed successfully');
		expect(outputText).to.not.match(/failed\s*=\s*[1-9]\d*/);
	});

	test('Check "molecule-list" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[1].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[1].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[1].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		const safeCommandLine: string = commandLine.replace(
			'/source\s+\$HOME\/\.bashrc/',
			'[ -f "$HOME/.bashrc" ] && . "$HOME/.bashrc" || true'
		);

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${safeCommandLine}`;
		} else {
			runCommandInBash = `${safeCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('molecule');
		expect(outputText).to.include('ansible');
		expect(outputText).to.include('default');
		expect(outputText).to.include('true');
	});

	test('Check "molecule-converge" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[2].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[2].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[2].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		const safeCommandLine: string = commandLine.replace(
			'/source\s+\$HOME\/\.bashrc/',
			'[ -f "$HOME/.bashrc" ] && . "$HOME/.bashrc" || true'
		);

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${safeCommandLine}`;
		} else {
			runCommandInBash = `${safeCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const recapBlocks: string[] = output.stdout.split(/PLAY RECAP/g).slice(1);
		recapBlocks.forEach((block): void => {
			expect(block).match(/failed\s*=\s*0/);
		});

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('PLAY [Converge]');
		expect(outputText).to.not.match(/failed\s*=\s*[1-9]\d*/);
	});

	test('Check "molecule-verify" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[3].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[3].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[3].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		const safeCommandLine: string = commandLine.replace(
			'/source\s+\$HOME\/\.bashrc/',
			'[ -f "$HOME/.bashrc" ] && . "$HOME/.bashrc" || true'
		);

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${safeCommandLine}`;
		} else {
			runCommandInBash = `${safeCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const recapBlocks: string[] = output.stdout.split(/PLAY RECAP/g).slice(1);
		recapBlocks.forEach((block): void => {
			expect(block).match(/failed\s*=\s*0/);
		});

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('PLAY [Verify]');
		expect(outputText).to.not.match(/failed\s*=\s*[1-9]\d*/);
	});

	test('Check "molecule-destroy" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[4].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[4].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[4].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		const safeCommandLine: string = commandLine.replace(
			'/source\s+\$HOME\/\.bashrc/',
			'[ -f "$HOME/.bashrc" ] && . "$HOME/.bashrc" || true'
		);

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${safeCommandLine}`;
		} else {
			runCommandInBash = `${safeCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const recapBlocks: string[] = output.stdout.split(/PLAY RECAP/g).slice(1);
		recapBlocks.forEach((block): void => {
			expect(block).match(/failed\s*=\s*0/);
		});

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('PLAY [Destroy]');
		expect(outputText).to.not.match(/failed\s*=\s*[1-9]\d*/);
	});

	test('Check "molecule-test" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[5].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[5].exec.workingDir;
		const commandLine: string = YAML.parse(devfileContent).commands[5].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: ${commandLine}`);

		const safeCommandLine: string = commandLine.replace(
			'/source\s+\$HOME\/\.bashrc/',
			'[ -f "$HOME/.bashrc" ] && . "$HOME/.bashrc" || true'
		);

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${safeCommandLine}`;
		} else {
			runCommandInBash = `${safeCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const recapBlocks: string[] = output.stdout.split(/PLAY RECAP/g).slice(1);
		recapBlocks.forEach((block): void => {
			expect(block).match(/failed\s*=\s*0/);
		});

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('PLAY [Create]');
		expect(outputText).to.include('PLAY [Converge]');
		expect(outputText).to.include('PLAY [Verify]');
		expect(outputText).to.not.match(/failed\s*=\s*[1-9]\d*/);
	});

	test('Check "ansible-navigator" command', function (): void {
		const containerName: string = YAML.parse(devfileContent).commands[6].exec.component;
		const workdir: string = YAML.parse(devfileContent).commands[6].exec.workingDir;
		const rawCommandLines: string = YAML.parse(devfileContent).commands[6].exec.commandLine;
		Logger.info(`workdir from exec section of DevWorkspace file: ${workdir}`);
		Logger.info(`commandLine from exec section of DevWorkspace file: \n${rawCommandLines}`);

		const modifiedLines: string[] = rawCommandLines
			.trim()
			.split('\n')
			.map((line): string => {
				const trimmed: string = line.trim();
				if (trimmed.startsWith('if [ ! -d') || trimmed === 'fi') {
					return ''; // remove conditional checks
				}
				if (trimmed.startsWith('ansible-navigator')) {
					// '--help' with '--mode stdout' disables interactive mode and ensures stable, testable output from ansible-navigator
					const safeCommandLine: string = trimmed + ' --help --mode stdout';
					return safeCommandLine;
				}

				return trimmed;
			})
			.filter(Boolean);

		const modifiedScript: string = modifiedLines.join(' && ');
		const patchedCommandLine: string = `bash -c \"${modifiedScript}\"`;

		let runCommandInBash: string;
		if (workdir) {
			runCommandInBash = `cd ${workdir} && ${patchedCommandLine}`;
		} else {
			runCommandInBash = `${patchedCommandLine}`;
		}

		const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
		expect(output.code).eqls(0);

		const outputText: string = output.stdout.trim();
		expect(outputText).to.include('ansible-navigator');
		expect(outputText).to.include('collections');
		expect(outputText).to.include('config');
		expect(outputText).to.include('settings');
		expect(outputText).to.include('welcome');
	});

	test('Check removing molecule pod afer deleting workspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace(devfileName);
		kubernetesCommandLineToolsExecutor.waitRemovingMoleculePod();
	});

	suiteTeardown('Delete DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.stopTcpPortForward();
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace(devfileName);
	});
});
