/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
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
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { PluginsView } from '../../pageobjects/ide/plugins/PluginsView';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import CheReporter from '../../driver/CheReporter';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const pluginsView: PluginsView = e2eContainer.get(CLASSES.PluginsView);

const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandling: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/InstallPluginUsingUI.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;

const pluginTitle: string = 'java11';
let workspaceName: string;

suite(`The 'InstallPluginUsingUI' test`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            workspaceName = await workspaceNameHandler.getNameFromUrl();
            CheReporter.registerRunningWorkspace(workspaceName);

            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);

            await projectTree.openProjectTreeContainer();
        });
    });

    suite('Install plugin test', async () => {
        test('Open plugins view', async () => {
            await pluginsView.openView();
        });

        test('Search plugin', async () => {
            await pluginsView.typeTextToSearchField(pluginTitle);
            await pluginsView.waitPlugin(pluginTitle);
        });

        test('Install plugin', async () => {
            await pluginsView.clickInstallButton(pluginTitle);
            await pluginsView.waitInstalledButton(pluginTitle);
            await pluginsView.waitPluginNotification('Click here to apply changes and restart your workspace');
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remove workspace`, async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandling.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });

});
