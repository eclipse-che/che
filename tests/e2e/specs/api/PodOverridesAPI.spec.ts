/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import fs from 'fs';
import path from 'path';
import YAML from 'yaml';
import { expect } from 'chai';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { DevfilesHelper } from '../../utils/DevfilesHelper';
import { DevWorkspaceConfigurationHelper } from '../../utils/DevWorkspaceConfigurationHelper';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { V1alpha2DevWorkspaceTemplate } from '@devfile/api';

suite(`Test defining pod overrides via attribute ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const pathToSampleFile: string = path.resolve('resources/pod-overrides.yaml');
	const podOverridesConfiguration: string = fs.readFileSync(pathToSampleFile, 'utf8');
	const workspaceName: string = YAML.parse(podOverridesConfiguration).metadata.name;
	const cheCodePath: string = '/tmp/che-code.yaml';
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const devfilesHelper: DevfilesHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
	let configurationToApply: string = podOverridesConfiguration;

	suiteSetup('Login into OC client', async function (): Promise<void> {
		kubernetesCommandLineToolsExecutor.loginToOcp();

		const cheCodeContent: string = devfilesHelper.obtainCheDevFileEditorFromCheConfigMap('editors-definitions').toString();
		fs.writeFileSync(cheCodePath, cheCodeContent);

		const configurationHelper: DevWorkspaceConfigurationHelper = new DevWorkspaceConfigurationHelper({
			devfileContent: `schemaVersion: 2.2.2\nmetadata:\n  name: ${workspaceName}\n`,
			editorContent: fs.readFileSync(cheCodePath, 'utf8')
		});
		const devfileContext: DevfileContext = await configurationHelper.generateDevfileContext();
		const editorTemplate: V1alpha2DevWorkspaceTemplate = devfileContext.devWorkspaceTemplates[0];
		const editorTemplateName: string = YAML.parse(podOverridesConfiguration).spec.contributions[0].kubernetes.name;

		expect(editorTemplate.metadata?.name).to.equal(editorTemplateName);
		configurationToApply = `${YAML.stringify(editorTemplate)}---\n${podOverridesConfiguration}`;
	});

	suiteTeardown('Delete DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
		if (fs.existsSync(cheCodePath)) {
			fs.unlinkSync(cheCodePath);
		}
	});

	test('Apply pod-overrides sample as DevWorkspace with OC client', function (): void {
		kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsStringOutput(configurationToApply);
		shellExecutor.wait(5);
	});

	test('Check that fields are overridden in the Deployment for DevWorkspace', function (): void {
		const devWorkspaceFullYamlOutput: any = YAML.parse(kubernetesCommandLineToolsExecutor.getDevWorkspaceYamlConfiguration());
		expect(devWorkspaceFullYamlOutput.spec.template.attributes['pod-overrides']).eqls({
			metadata: {
				annotations: {
					'io.kubernetes.cri-o.userns-mode': 'auto:size=65536;map-to-root=true',
					'io.openshift.userns': 'true',
					'openshift.io/scc': 'container-build'
				}
			},
			spec: {
				runtimeClassName: 'kata',
				schedulerName: 'stork'
			}
		});
	});
});
