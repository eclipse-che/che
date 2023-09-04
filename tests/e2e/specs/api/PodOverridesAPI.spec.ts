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

suite('Test defining pod overrides via attribute.', function (): void {
	const pathToSampleFile: string = path.resolve(
		`resources/pod-overrides${BASE_TEST_CONSTANTS.IS_CLUSTER_DISCONNECTED() ? '-airgap' : ''}.yaml`
	);
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

	test('Apply pod-overrides sample as DevWorkspace with OC client', function (): void {
		kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsFile(pathToSampleFile);
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
