import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { expect } from 'chai';
import { ShellString } from 'shelljs';
import { StringUtil } from '../../utils/StringUtil';
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { APITestConstants } from '../../constants/APITestConstants';
import { BaseTestConstants } from '../../constants/BaseTestConstants';

suite(`Empty workspace API test`, async function (): Promise<void> {
    // works only for root user
    const namespace: string = APITestConstants.TS_API_TEST_NAMESPACE;
    let clonedProjectName: string;
    let containerWorkDir: string;
    let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
    let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
    let devfileContext: DevfileContext;
    let devWorkspaceName: string | undefined;
    let containerTerminal: KubernetesCommandLineToolsExecutor.ContainerTerminal;

    const gitRepository: string = 'https://github.com/crw-qe/web-nodejs-sample';

    suiteSetup('Create empty workspace with OC client', async function (): Promise<void> {
        const workspaceName: string = 'empty-' + Math.floor(Math.random() * 1000);
        const devfileContent: string =
            'schemaVersion: 2.2.0\n' +
            'metadata:\n' +
            `  name: ${workspaceName}\n`;

        devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
            devfileContent,
        });
        devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
        devWorkspaceName = devfileContext?.devWorkspace?.metadata?.name;
        kubernetesCommandLineToolsExecutor = new KubernetesCommandLineToolsExecutor(devWorkspaceName, namespace);
        containerTerminal = new KubernetesCommandLineToolsExecutor.ContainerTerminal(kubernetesCommandLineToolsExecutor);
    });

    test('Create empty workspace', async function (): Promise<void> {
        kubernetesCommandLineToolsExecutor.loginToOcp();
        const devWorkspaceConfigurationYamlString: string = await devWorkspaceConfigurationHelper.getDevWorkspaceConfigurationYamlAsString(devfileContext);
        const output: ShellString = kubernetesCommandLineToolsExecutor.applyAndWaitDevWorkspace(devWorkspaceConfigurationYamlString);
        expect(output.stdout).contains('condition met');
    });

    suite('Clone public repo without previous setup', function (): void {

        test('Check if public repo can be cloned', function (): void {
            containerWorkDir = containerTerminal.pwd().stdout.replace('\n', '');
            const cloneOutput: ShellString = containerTerminal.gitClone(gitRepository);
            expect(cloneOutput.stdout + cloneOutput.stderr).includes('Cloning');
        });

        test('Check if project was created', function (): void {
            clonedProjectName = StringUtil.getProjectNameFromGitUrl(gitRepository);
            expect(containerTerminal.ls().stdout).includes(clonedProjectName);
        });

        test('Check if project files are imported', function (): void {
            expect(containerTerminal.ls(`${containerWorkDir}/${clonedProjectName}`).stdout)
                .includes(BaseTestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME);
        });

        suiteTeardown('Delete cloned project', function (): void {
            containerTerminal.removeFolder(`${clonedProjectName}`);
        });
    });

    suiteTeardown('Delete workspace', function (): void {
        kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
    });
});


