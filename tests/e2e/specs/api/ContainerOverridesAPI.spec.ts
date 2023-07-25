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

suite('Test defining container overrides via attribute.', function (): void {
	const pathToSampleFile: string = path.resolve('resources/container-overrides.yaml');
	const workspaceName: string = YAML.parse(fs.readFileSync(pathToSampleFile, 'utf8')).metadata.name;
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = new KubernetesCommandLineToolsExecutor(workspaceName);

	suiteSetup('Login into OC client', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	suiteTeardown('Delete DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});

	test('Apply container-overrides sample as DevWorkspace with OC client', function (): void {
		kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsFile(pathToSampleFile);
		ShellExecutor.wait(5);
	});

	test('Check that fields are overridden in the Deployment for DevWorkspace', function (): void {
		const devWorkspaceFullYamlOutput: any = YAML.parse(kubernetesCommandLineToolsExecutor.getDevWorkspaceYamlConfiguration());
		expect(devWorkspaceFullYamlOutput.spec.template.components[0].attributes['container-overrides']).eqls({
			resources: {
				limits: {
					'nvidia.com/gpu': '1'
				}
			}
		});
	});
});
