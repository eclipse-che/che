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
import * as codeExecutionHelper from '../../testsLibrary/CodeExecutionTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as projectManager from '../../testsLibrary/ProjectAndFileTests';
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import { DriverHelper } from '../../utils/DriverHelper';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { PreferencesHandler } from '../../utils/PreferencesHandler';
import { RightToolBar } from '../../pageobjects/ide/RightToolBar';
import { KubernetesPlugin } from '../../pageobjects/ide/plugins/KubernetesPlugin';
import { error } from 'selenium-webdriver';


const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const leftToolbar: RightToolBar = e2eContainer.get(CLASSES.RightToolBar);
const kubernetesPlugin: KubernetesPlugin = e2eContainer.get(CLASSES.KubernetesPlugin);

const devfileUrl: string = 'https://gist.githubusercontent.com/Ohrimenko1988/94d1a70ff94d4d4bc5f2e4678dc8d538/raw/353a2513ea9e2f61b6cb1e0a88be21efd35b353b/kubernetes-plugin-test.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
const vsKubernetesConfig = { "vs-kubernetes.kubeconfig": "/projects/nodejs-web-app/config" }

suite(`The 'VscodeKubernetesPlugin' test`, async () => {
    let workspaceName: string = '';

    suite('Create workspace', async () => {
        test('Set kubeconfig path', async () => {
            const pathToKubeconfig: string = '/projects/nodejs-web-app/config'
            // await preferencesHandler.setKubeconfig(pathToKubeconfig);
        });

        test('Create workspace using factory', async () => {
            await preferencesHandler.setVscodeKubernetesPluginConfig(vsKubernetesConfig);
            await driverHelper.navigateToUrl(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);

            projectManager.waitWorkspaceReadiness(sampleName, subRootFolder);

            const workspaceUrl = await (await driverHelper.getDriver()).getCurrentUrl();
        })
    });

    suite('Test', async () => {
        test('Test', async () => {
            await kubernetesPlugin.openView(240_000);
            await kubernetesPlugin.waitListItemContains('/api-ocp46-crw-qe-com:6443/', 240_000);
        });
    });

});
