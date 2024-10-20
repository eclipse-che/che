/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
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
import {Logger} from "../../utils/Logger";
import {DevfilesRegistryHelper} from "../../utils/DevfilesRegistryHelper";
import {ContainerTerminal, KubernetesCommandLineToolsExecutor} from "../../utils/KubernetesCommandLineToolsExecutor";
import {ShellExecutor} from "../../utils/ShellExecutor";
import {DevWorkspaceConfigurationHelper} from "../../utils/DevWorkspaceConfigurationHelper";
import {DevfileContext} from "@eclipse-che/che-devworkspace-generator/lib/api/devfile-context";
import {ShellString} from "shelljs";
import {expect} from "chai";
import {API_TEST_CONSTANTS} from "../../constants/API_TEST_CONSTANTS";
import YAML from "yaml";
import {StringUtil} from "../../utils/StringUtil";
import {string} from "yaml/dist/schema/common/string";


suite('Ansible devfile API test', function (): void {
    const devfilesRegistryHelper: DevfilesRegistryHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
    const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
    let containerTerminal: ContainerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
    let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
    let devfileContext: DevfileContext;
    let devfileContent: string ='';

    suiteSetup(`Prepare login ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, async function (): Promise<void> {
        kubernetesCommandLineToolsExecutor.loginToOcp();
    });

    test('Create  PHP DevSpace', async function (): Promise<void> {
        kubernetesCommandLineToolsExecutor.namespace = 'admin-devspaces';
        devfileContent = devfilesRegistryHelper.getDevfileContent('php');
        const editorDevfileContent: string = devfilesRegistryHelper.obtainCheDevFileEditor('editors-definitions');
        kubernetesCommandLineToolsExecutor.workspaceName = YAML.parse(devfileContent).metadata.name;

        devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
            editorContent: editorDevfileContent, devfileContent: devfileContent
        });
        devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();

        const devWorkspaceConfigurationYamlString: string =
            devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
        const output: ShellString = kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(devWorkspaceConfigurationYamlString);
        expect(output.stdout).contains('condition met');
    });

    test('Check running application', async function (): Promise<void> {
        const workdir:string = YAML.parse(devfileContent).commands[0].exec.workingDir;
        const commandLine:string = YAML.parse(devfileContent).commands[0].exec.commandLine;
        const containerName:string = YAML.parse(devfileContent).commands[0].exec.component;
        const runCommandInBash:string = `cd ${workdir} && ${commandLine}`;
        const output: ShellString = containerTerminal.execInContainerCommand(runCommandInBash, containerName);
        expect(output.code).eqls(0);
        expect(output.stdout.trim()).contains('Hello, world!');

    });

    suiteTeardown('Delete workspace', function (): void {
        kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
    });
});
