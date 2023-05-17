import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { ShellString } from 'shelljs';
import { expect } from 'chai';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { TestConstants } from '../../constants/TestConstants';
import { GitUtil } from '../../utils/vsc/GitUtil';

suite(`Devfile acceptance test`, async function (): Promise<void> {
    let devfileUrl: string;
    let devWorkspaceConfigurationHelper: DevWorkspaceConfigurationHelper;
    let kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor;
    let devfileContext: DevfileContext;
    let devWorkspaceName: string | undefined;

    suiteSetup('Get DevWorkspace configuration from meta.yaml', async function (): Promise<void> {
        devfileUrl = (new GitUtil).getProjectGitLinkFromLinkToMetaYaml(TestConstants.TS_API_TEST_LINK_TO_META_YAML);
        devWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
            devfileUrl,
            projects: []
        });
        devfileContext = await devWorkspaceConfigurationHelper.generateDevfileContext();
        devWorkspaceName = devfileContext?.devWorkspace?.metadata?.name;

        kubernetesCommandLineToolsExecutor = new KubernetesCommandLineToolsExecutor(devWorkspaceName);
        kubernetesCommandLineToolsExecutor.loginToOcp();
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

    suiteTeardown('Delete DevWorkspace', async function (): Promise<void> {
        kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
    });
});


