import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { ShellString } from 'shelljs';
import { expect } from 'chai';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { StringUtil } from '../../utils/StringUtil';
import { DevfilesRegistryHelper } from '../../utils/DevfilesRegistryHelper';
import { Logger } from '../../utils/Logger';

/**
 * Dynamically generating tests
 * info: https://mochajs.org/#delayed-root-suite
 */

(async function (): Promise<void> {

    const devfilesRegistryHelper: DevfilesRegistryHelper = new DevfilesRegistryHelper();
    const devfileSamples: any = await devfilesRegistryHelper.collectPathsToDevfilesFromRegistry();

    for (const devfileSample of devfileSamples) {
        suite(`Devfile acceptance test suite for ${devfileSample.name}`, async function (): Promise<void> {
            this.bail(false);
            this.timeout(1500000); // 25 minutes because build of Quarkus sample takes 20+ minutes
            let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
            let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
            let containerTerminal: KubernetesCommandLineToolsExecutor.ContainerTerminal;
            let devfileContext: DevfileContext;
            let devWorkspaceName: string | undefined;
            let clonedProjectName: string;
            let containerWorkDir: string;
            let devfilesBuildCommands: any[] = [];

            test('Get DevWorkspace configuration', async function (): Promise<void> {
                devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
                    devfileUrl: devfileSample.link,
                });
                devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
                devWorkspaceName = devfileContext?.devWorkspace?.metadata?.name;

                kubernetesCommandLineToolsExecutor = new KubernetesCommandLineToolsExecutor(devWorkspaceName);
                containerTerminal = new KubernetesCommandLineToolsExecutor.ContainerTerminal(kubernetesCommandLineToolsExecutor);
                kubernetesCommandLineToolsExecutor.loginToOcp();
            });

            test('Collect build commands from the devfile', async function (): Promise<void> {
                if (devfileContext.devfile.commands === undefined) {
                    Logger.info(`Devfile does not contains any commands.`);
                } else {
                    devfileContext.devfile.commands.forEach((command: any) => {
                        if (command.exec.group.kind === 'build') {
                            Logger.debug(`Build command found: ${command.exec.commandLine}`);
                            devfilesBuildCommands.push(command);
                        }
                    });
                }
            });

            test('Create DevWorkspace', async function (): Promise<void> {
                const devWorkspaceConfigurationYamlString: string = await devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
                const applyOutput: ShellString = kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsStringOutput(devWorkspaceConfigurationYamlString);

                expect(applyOutput.stdout)
                    .contains('devworkspacetemplate')
                    .and.contains('devworkspace')
                    .and.contains.oneOf(['created', 'configured']);

            });

            test('Wait until DevWorkspace has status "ready"', async function (): Promise<void> {
                expect(kubernetesCommandLineToolsExecutor.waitDevWorkspace().stdout).contains('condition met');
            });

            test('Check if project was created', function (): void {
                clonedProjectName = StringUtil.getProjectNameFromGitUrl(devfileSample.link);
                expect(containerTerminal.ls().stdout).includes(clonedProjectName);
            });

            test('Check if project files are imported', function (): void {
                containerWorkDir = containerTerminal.pwd().stdout.replace('\n', '');
                expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes(`devfile.yaml`);
            });

            test(`Check if build commands returns success`, function (): void {
                if (devfilesBuildCommands.length === 0) {
                    Logger.info(`Devfile does not contains build commands.`);
                } else {
                    let workingDir: string;
                    devfilesBuildCommands.forEach((command) => {
                        Logger.info(`command.exec.commandLine: ${command.exec.commandLine}`);
                        Logger.info(`command.exec.component: ${command.exec.component}`);
                        Logger.info(`command.exec.workingDir: ${command.exec.workingDir}`);

                        workingDir = StringUtil.getFullWorkingDirPathExplicit(command.exec.workingDir, containerTerminal);
                        const commandString: string = `cd ${workingDir} && ${command.exec.commandLine}`;
                        Logger.info(`Full build command to be executed: ${commandString}`);

                        const output: ShellString = containerTerminal.executeCommand(commandString, command.exec.component);
                        expect(output.code).eqls(0);
                    });
                }
            });

            test('Delete DevWorkspace', async function (): Promise<void> {
                kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
            });
        });
    }

    run();
})();
