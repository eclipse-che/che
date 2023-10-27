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
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { StringUtil } from '../../utils/StringUtil';
import { Logger } from '../../utils/Logger';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { DevfilesRegistryHelper } from '../../utils/DevfilesRegistryHelper';
import { MOCHA_CONSTANTS } from '../../constants/MOCHA_CONSTANTS';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';

/**
 * dynamically generating tests
 * info: https://mochajs.org/#delayed-root-suite
 */
void (async function (): Promise<void> {
	const devfilesRegistryHelper: DevfilesRegistryHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);

	let devfileSamples: any = [];
	if (
		MOCHA_CONSTANTS.MOCHA_DELAYED_SUITE &&
		!API_TEST_CONSTANTS.TS_API_ACCEPTANCE_TEST_REGISTRY_URL().includes(BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL)
	) {
		devfileSamples = await devfilesRegistryHelper.collectPathsToDevfilesFromRegistry(false);
	}
	for (const devfileSample of devfileSamples) {
		suite(`Devfile acceptance test suite for ${devfileSample.name}`, function (): void {
			this.bail(false);
			this.timeout(1500000); // 25 minutes because build of Quarkus sample takes 20+ minutes
			let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
			let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
			let containerTerminal: ContainerTerminal;
			let devfileContext: DevfileContext;
			let devWorkspaceName: string | undefined;
			let clonedProjectName: string;
			let containerWorkDir: string;
			const devfilesBuildCommands: any[] = [];

			test('Get DevWorkspace configuration', async function (): Promise<void> {
				devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
					devfileUrl: devfileSample.link
				});
				devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
				devWorkspaceName = devfileContext?.devWorkspace?.metadata?.name;
				kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
				kubernetesCommandLineToolsExecutor.workspaceName = devWorkspaceName;
				containerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
				kubernetesCommandLineToolsExecutor.loginToOcp();
			});

			test('Collect build commands from the devfile', function (): void {
				if (devfileContext.devfile.commands === undefined) {
					Logger.info('Devfile does not contains any commands.');
				} else {
					devfileContext.devfile?.commands?.forEach((command: any): void => {
						if (command.exec?.group?.kind === 'build') {
							Logger.debug(`Build command found: ${command.exec.commandLine}`);
							devfilesBuildCommands.push(command);
						}
					});
				}
			});

			test('Create DevWorkspace', function (): void {
				const devWorkspaceConfigurationYamlString: string =
					devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
				const applyOutput: ShellString =
					kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsStringOutput(devWorkspaceConfigurationYamlString);

				expect(applyOutput.stdout)
					.contains('devworkspacetemplate')
					.and.contains('devworkspace')
					.and.contains.oneOf(['created', 'configured']);
			});

			test('Wait until DevWorkspace has status "ready"', function (): void {
				expect(kubernetesCommandLineToolsExecutor.waitDevWorkspace().stdout).contains('condition met');
			});

			test('Check if project was created', function (): void {
				clonedProjectName = StringUtil.getProjectNameFromGitUrl(devfileSample.link);
				expect(containerTerminal.ls().stdout).includes(clonedProjectName);
			});

			test('Check if project files are imported', function (): void {
				containerWorkDir = containerTerminal.pwd().stdout.replace('\n', '');
				expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes('devfile.yaml');
			});

			test('Check if build commands returns success', function (): void {
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
		});
	}

	run();
})();
