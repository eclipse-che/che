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
import { CLASSES, TYPES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { PluginsView } from '../../pageobjects/ide/plugins/PluginsView';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const pluginsView: PluginsView = e2eContainer.get(CLASSES.PluginsView);

const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandling: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/devfiles/plugins/InstallPluginUsingUI.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;

const pluginTitle: string = 'java11';

suite(`The 'InstallPluginUsingUI' test`, async () => {
    let workspaceName: string = '';

    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await projectTree.openProjectTreeContainer();

            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
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
