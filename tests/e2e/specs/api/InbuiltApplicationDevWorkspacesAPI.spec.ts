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
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { ShellString } from 'shelljs';
import { expect } from 'chai';
import { StringUtil } from '../../utils/StringUtil';
import { DevfilesRegistryHelper } from '../../utils/DevfilesRegistryHelper';
import { Logger } from '../../utils/Logger';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import { MOCHA_CONSTANTS } from '../../constants/MOCHA_CONSTANTS';

/**
 * dynamically generating tests
 * info: https://mochajs.org/#delayed-root-suite
 */

void (async function (): Promise<void> {
	const devfilesRegistryHelper: DevfilesRegistryHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	let devfileSamples: any;
	if (MOCHA_CONSTANTS.MOCHA_DELAYED_SUITE) {
		devfileSamples = await devfilesRegistryHelper.collectPathsToDevfilesFromRegistry(
			true,
			API_TEST_CONSTANTS.TS_API_TEST_DEV_WORKSPACE_LIST?.split(',')
		);
	}

	for (const devfileSample of devfileSamples) {
		suite(
			`Inbuilt DevWorkspaces test suite for "${devfileSample.name}" sample ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`,
			function (): void {
				this.bail(false);
				let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
				let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
				let containerTerminal: ContainerTerminal;
				let devfileContextYaml: any;
				let devWorkspaceName: string | undefined;
				let clonedProjectName: string;
				let containerWorkDir: string;
				const devfilesBuildCommands: any[] = [];

				test('Get DevWorkspace configuration', function (): void {
					devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({});
					devfileContextYaml = devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationsAsYaml(
						devfileSample.devWorkspaceConfigurationString
					);
					devWorkspaceName = devfileContextYaml.DevWorkspace.metadata.name;
					kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
					kubernetesCommandLineToolsExecutor.workspaceName = devWorkspaceName;
					containerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
					kubernetesCommandLineToolsExecutor.loginToOcp();
				});

				test('Collect build commands from the devfile', function (): void {
					if (devfileContextYaml.DevWorkspace.spec.template.commands === undefined) {
						Logger.info('Devfile does not contains any commands.');
					} else {
						devfileContextYaml.DevWorkspace.spec.template.commands.forEach((command: any): void => {
							if (command.exec?.group?.kind === 'build') {
								Logger.debug(`Build command found: ${command.exec.commandLine}`);
								devfilesBuildCommands.push(command);
							}
						});
					}
				});

				test('Create and wait DevWorkspace', function (): void {
					const output: ShellString = kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsStringOutput(
						devfileSample.devWorkspaceConfigurationString
					);
					expect(output.stdout)
						.contains('devworkspacetemplate')
						.and.contains('devworkspace')
						.and.contains.oneOf(['created', 'configured']);
				});

				test('Wait until DevWorkspace has status "ready"', function (): void {
					this.timeout(TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
					expect(kubernetesCommandLineToolsExecutor.waitDevWorkspace().stdout).contains('condition met');
				});

				test('Check if project was created', function (): void {
					clonedProjectName = devfileContextYaml.DevWorkspace.spec.template.projects[0].name;
					expect(containerTerminal.ls().stdout).includes(clonedProjectName);
				});

				test('Check if project files are imported', function (): void {
					containerWorkDir = containerTerminal.pwd().stdout.replace('\n', '');
					expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes('devfile.yaml');
				});

				test('Check if build commands returns success', function (): void {
					this.test?.timeout(1500000); // 25 minutes because build of Quarkus sample takes 20+ minutes
					if (devfilesBuildCommands.length === 0) {
						Logger.info('Devfile does not contains build commands.');
					} else {
						devfilesBuildCommands.forEach((command): void => {
							Logger.info(`command.exec: ${JSON.stringify(command.exec)}`);

							const commandString: string = StringUtil.updateCommandEnvsToShStyle(
								`cd ${command.exec.workingDir} && ${command.exec.commandLine}`
							);
							Logger.info(`Full build command to be executed: ${commandString}`);

							const output: ShellString = containerTerminal.execInContainerCommand(commandString, command.exec.component);
							expect(output.code).eqls(0);
						});
					}
				});

				test('Delete DevWorkspace', function (): void {
					kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
				});
			}
		);
	}

	run();
})();
