/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import {Logger} from "../../utils/Logger";
import {DevfilesRegistryHelper} from "../../utils/DevfilesRegistryHelper";
import {KubernetesCommandLineToolsExecutor} from "../../utils/KubernetesCommandLineToolsExecutor";
import {ShellExecutor} from "../../utils/ShellExecutor";

suite('Ansible devfile API test', function (): void {
    const devfilesRegistryHelper: DevfilesRegistryHelper = e2eContainer.get(CLASSES.DevfilesRegistryHelper);
    const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(CLASSES.KubernetesCommandLineToolsExecutor);
    const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);

    suiteSetup(`Prepare login ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, async function (): Promise<void> {
        kubernetesCommandLineToolsExecutor.loginToOcp();
    });

    test('Test getting URL', function (): void {
        const podName: string = shellExecutor.executeArbitraryShellScript(`oc get pods -n ${BASE_TEST_CONSTANTS.TEST_NAMESPACE} | grep dashboard | awk \'{print $1}\'`).trim()
        const containerName: string = shellExecutor.executeArbitraryShellScript(`oc get pod -n ${BASE_TEST_CONSTANTS.TEST_NAMESPACE} ${podName} -o jsonpath=\'{.spec.containers[*].name}\'`)
        const devFileContent: string = devfilesRegistryHelper.obtainDevFileContentUsingPod(podName,containerName,'ansible');

    });



    suiteTeardown('Delete workspace', function (): void {
        Logger.trace('Tearing down the workspace');
    });
});
