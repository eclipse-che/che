import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import fs from 'fs';
import path from 'path';
import YAML from 'yaml';
import { expect } from 'chai';

suite(`Test defining container overrides via attribute.`, async function (): Promise<void> {
    const pathToSampleFile: string = path.resolve('resources/container-overrides.yaml');
    const workspaceName: string = YAML.parse(fs.readFileSync(pathToSampleFile, 'utf8')).metadata.name;
    const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = new KubernetesCommandLineToolsExecutor(workspaceName);

    suiteSetup('Login into OC client', function (): void {
        kubernetesCommandLineToolsExecutor.loginToOcp();
    });

    suiteTeardown('Delete DewWorkspace', function (): void {
        kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
    });

    test('Apply container-overrides sample as DewWorkspace with OC client', function (): void {
        kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsFile(pathToSampleFile);
        kubernetesCommandLineToolsExecutor.wait(5);
    });

    test('Check that fields are overridden in the Deployment for DewWorkspace', function (): void {
       const devWorkspaceFullYamlOutput: Object = YAML.parse(kubernetesCommandLineToolsExecutor.getDevWorkspaceYamlConfiguration());
       expect(devWorkspaceFullYamlOutput.spec.template.components[0].attributes['container-overrides']).eqls({
           resources: {
               limits: {
                   'nvidia.com/gpu': '1'
               }
           }
       });
    });
});


