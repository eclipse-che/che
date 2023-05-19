import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { GitUtil } from '../../utils/vsc/GitUtil';
import { TestConstants } from '../../constants/TestConstants';


const gitRepository: string = 'https://github.com/crw-qe/web-nodejs-sample';

suite(`Test cloning of repo "${gitRepository}" into empty workspace.`, async function (): Promise<void> {
    // works only for root user
    const namespace: string = TestConstants.TS_API_TEST_NAMESPACE ? TestConstants.TS_API_TEST_NAMESPACE : undefined;
    const workspaceName: string = 'empty-' + Math.floor(Math.random() * 1000);
    let clonedProjectName: string;
    let containerWorkDir: string = '';

    const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = new KubernetesCommandLineToolsExecutor(workspaceName, namespace);
    const containerTerminal: KubernetesCommandLineToolsExecutor.ContainerTerminal = new KubernetesCommandLineToolsExecutor.ContainerTerminal(kubernetesCommandLineToolsExecutor);

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
        `          image: ${TestConstants.TS_API_TEST_UDI_IMAGE}\n` +
        '        name: universal-developer-image\n' +
        '  contributions:\n' +
        '    - name: che-code\n' +
        `      uri: ${TestConstants.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI}\n` +
        '      components:\n' +
        '        - name: che-code-runtime-description\n' +
        '          container:\n' +
        '            env:\n' +
        '              - name: CODE_HOST\n' +
        '                value: 0.0.0.0';

    suiteSetup('Create empty workspace with OC client', function (): void {
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
            clonedProjectName = GitUtil.getProjectNameFromGitUrl(gitRepository);
            expect(containerTerminal.ls().stdout).includes(clonedProjectName);
        });

        test('Check if files were imported ', function (): void {
            expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes(TestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
        });
    });
});


