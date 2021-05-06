/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { WorkspaceNameHandler } from '../..';
import 'reflect-metadata';
import { DriverHelper } from '../../utils/DriverHelper';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { PreferencesHandler } from '../../utils/PreferencesHandler';
import { KubernetesPlugin } from '../../pageobjects/ide/plugins/KubernetesPlugin';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const kubernetesPlugin: KubernetesPlugin = e2eContainer.get(CLASSES.KubernetesPlugin);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);

const devfileUrl: string = 'https://gist.githubusercontent.com/Ohrimenko1988/94d1a70ff94d4d4bc5f2e4678dc8d538/raw/353a2513ea9e2f61b6cb1e0a88be21efd35b353b/kubernetes-plugin-test.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
const vsKubernetesConfig = { 'vs-kubernetes.kubeconfig': '/projects/nodejs-web-app/config' };

suite(`The 'VscodeKubernetesPlugin' test`, async () => {
    suite('Create workspace', async () => {
        test('Set kubeconfig path', async () => {
            await preferencesHandler.setVscodeKubernetesPluginConfig(vsKubernetesConfig);
        });

        test('Create workspace using factory', async () => {
            await driverHelper.navigateToUrl(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(sampleName, subRootFolder);
        });
    });

    suite('Check the "Kubernetes" plugin', async () => {
        test('Check plugin is added to workspace', async () => {
            await kubernetesPlugin.openView(240_000);
        });

        test('Check plugin basic functionality', async () => {
            await kubernetesPlugin.waitListItemContains('/api-ocp46-crw-qe-com:6443/', 240_000);
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup(async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
