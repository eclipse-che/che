/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { KubernetesPlugin } from '../../pageobjects/ide/plugins/KubernetesPlugin';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { OpenDialogWidget, Buttons } from '../../pageobjects/ide/OpenDialogWidget';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const kubernetesPlugin: KubernetesPlugin = e2eContainer.get(CLASSES.KubernetesPlugin);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const openDialogWidget: OpenDialogWidget = e2eContainer.get(CLASSES.OpenDialogWidget);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/VscodeKubernetesPlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
let workspaceName: string;

suite(`The 'VscodeKubernetesPlugin' test`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            workspaceName = await workspaceNameHandler.getNameFromUrl();
            CheReporter.registerRunningWorkspace(workspaceName);

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

        test('Set kubeconfig', async () => {
            await topMenu.selectOption('View', 'Find Command...');
            await quickOpenContainer.typeAndSelectSuggestion('Kubernetes: Set', 'Kubernetes: Set Kubeconfig');
            await quickOpenContainer.clickOnContainerItem('+ Add new kubeconfig');
            await openDialogWidget.expandPathAndSelectFile(`projects/${sampleName}`, 'config', Buttons.Open);
        });

        test('Check plugin basic functionality', async () => {
            await kubernetesPlugin.waitListItemContains('/api-ocp46-crw-qe-com:6443/', 240_000);
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });
});
