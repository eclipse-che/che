import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { API_TEST_CONSTANTS } from '../../constants/API_TEST_CONSTANTS';
import {BASE_TEST_CONSTANTS} from "../../constants/BASE_TEST_CONSTANTS";
import {e2eContainer} from "../../configs/inversify.config";
import {CLASSES} from "../../configs/inversify.types";
import { ContainerTerminal, KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
const gitRepository: string = 'https://github.com/crw-qe/web-nodejs-sample';

suite(`Test cloning of repo "${gitRepository}" into empty workspace.`, async function (): Promise<void> {
    // works only for root user
    const workspaceName: string = 'empty-' + Math.floor(Math.random() * 1000);
    let clonedProjectName: string= 'web-nodejs-sample';
    let containerWorkDir: string = '';
    let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
    let containerTerminal: ContainerTerminal;
    containerTerminal = e2eContainer.get(CLASSES.ContainerTerminal);
    const emptyYaml: string =
        'apiVersion: workspace.devfile.io/v1alpha2\n' +
        'kind: DevWorkspace\n' +
        'metadata:\n' +
        `  name: ${workspaceName}\n` +
        'spec:\n' +
        '  started: true\n' +
        '  template:\n' +
        '    components:\n' +
        '      - container:\n' +
        `          image: ${API_TEST_CONSTANTS.TS_API_TEST_UDI_IMAGE}\n` +
        '        name: universal-developer-image\n' +
        '  contributions:\n' +
        '    - name: che-code\n' +
        `      uri: ${API_TEST_CONSTANTS.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI}\n` +
        '      components:\n' +
        '        - name: che-code-runtime-description\n' +
        '          container:\n' +
        '            env:\n' +
        '              - name: CODE_HOST\n' +
        '                value: 0.0.0.0';

    suiteSetup('Create empty workspace with OC client', function (): void {
        kubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
        kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
        kubernetesCommandLineToolsExecutor.loginToOcp();
        kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(emptyYaml);
    });

    suiteTeardown('Delete workspace', function (): void {
        kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
    });

    suite('Clone public repo without previous setup', function (): void {
        suiteTeardown('Delete cloned project', function (): void {
            containerTerminal.removeFolder(`${clonedProjectName}`);
        });

        test('Check if public repo can be cloned', function (): void {
            containerWorkDir = containerTerminal.pwd().stdout.replace('\n', '');
            const cloneOutput: ShellString = containerTerminal.gitClone(gitRepository);
            expect(cloneOutput.stdout + cloneOutput.stderr).includes('Cloning');
        });

        test('Check if project was created', function (): void {
             expect(containerTerminal.ls(`${containerWorkDir}`).stdout).includes(
                clonedProjectName
            );
        });

        test('Check if files were imported ', function (): void {
            expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes(BASE_TEST_CONSTANTS.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
        });
    });
});


