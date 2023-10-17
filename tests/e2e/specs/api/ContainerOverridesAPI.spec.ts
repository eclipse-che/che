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
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';

suite(`Test defining container overrides via attribute ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const pathToSampleFile: string = path.resolve('resources/container-overrides.yaml');
	const workspaceName: string = YAML.parse(fs.readFileSync(pathToSampleFile, 'utf8')).metadata.name;
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);

	suiteSetup('Login into OC client', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	suiteTeardown('Delete DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});

	test('Apply container-overrides sample as DevWorkspace with OC client', function (): void {
		kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsFile(pathToSampleFile);
		shellExecutor.wait(5);
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
