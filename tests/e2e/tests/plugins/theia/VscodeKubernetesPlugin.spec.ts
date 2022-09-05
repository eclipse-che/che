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
import { e2eContainer } from '../../../inversify.config';
import { CLASSES } from '../../../inversify.types';
import { Ide } from '../../../pageobjects/ide/theia/Ide';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { TestConstants } from '../../../TestConstants';
import { KubernetesPlugin } from '../../../pageobjects/ide/theia/plugins/KubernetesPlugin';
import { ProjectTree } from '../../../pageobjects/ide/theia/ProjectTree';
import { WorkspaceHandlingTestsTheia } from '../../../testsLibrary/theia/WorkspaceHandlingTestsTheia';
import { Logger } from '../../../utils/Logger';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { TopMenu } from '../../../pageobjects/ide/theia/TopMenu';
import { QuickOpenContainer } from '../../../pageobjects/ide/theia/QuickOpenContainer';
import { OpenDialogWidget, Buttons } from '../../../pageobjects/ide/theia/OpenDialogWidget';

const workspaceHandlingTests: WorkspaceHandlingTestsTheia = e2eContainer.get(CLASSES.WorkspaceHandlingTestsTheia);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const kubernetesPlugin: KubernetesPlugin = e2eContainer.get(CLASSES.KubernetesPlugin);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const openDialogWidget: OpenDialogWidget = e2eContainer.get(CLASSES.OpenDialogWidget);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/VscodeKubernetesPlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';

suite(`The 'VscodeKubernetesPlugin' test`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        test('Wait until created workspace is started', async () => {
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await ide.waitNotificationAndClickOnButton('Do you trust the authors of', 'Yes, I trust', 60_000);

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
                await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTestsTheia.getWorkspaceName());
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });
});
