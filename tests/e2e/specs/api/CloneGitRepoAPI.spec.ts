import { OpenshiftClientExecutor } from '../../utils/OpenshiftClientExecutor';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { GitUtil } from '../../utils/vsc/GitUtil';
import { TestConstants } from '../../constants/TestConstants';

const gitRepository: string = 'https://github.com/crw-qe/web-nodejs-sample';

suite(`Test cloning of repo "${gitRepository}" into empty workspace.`, async function (): Promise<void> {
    // works only for root user
    const namespace: string = 'admin-devspaces';
    const workspaceName: string = 'empty-' + Math.floor(Math.random() * 1000);
    const clonedProjectName: string = GitUtil.getProjectNameFromGitUrl(gitRepository);
    let containerWorkDir: string = '';

    const openshiftClientExecutor: OpenshiftClientExecutor = new OpenshiftClientExecutor(workspaceName, namespace);
    const containerTerminal: OpenshiftClientExecutor.ContainerTerminal = new OpenshiftClientExecutor.ContainerTerminal(openshiftClientExecutor);

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
        openshiftClientExecutor.loginToOcp();
        openshiftClientExecutor.applyAndWaitDevWorkspace(emptyYaml);
    });

    suiteTeardown('Delete workspace', function (): void {
        openshiftClientExecutor.deleteDevWorkspace();
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
            expect(containerTerminal.ls().stdout).includes(clonedProjectName);
        });

        test('Check if files were imported ', function (): void {
            expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout).includes(TestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
        });
    });
});


