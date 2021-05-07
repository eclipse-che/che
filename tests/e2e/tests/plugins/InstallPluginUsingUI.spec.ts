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
import * as workspaceHandling from '../../testsLibrary/WorkspaceHandlingTests';
import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { PluginsView } from '../../pageobjects/ide/plugins/PluginsView';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const pluginsView: PluginsView = e2eContainer.get(CLASSES.PluginsView);

const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

const workspaceName: string = 'install-plugin-test';
const workspaceUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${TestConstants.TS_SELENIUM_USERNAME}/${workspaceName}`;

const pluginTitle: string = 'java11';

suite(`The 'InstallPluginUsingUI' test`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace', async () => {
            const wsConfig = await testWorkspaceUtils.getBaseDevfile();
            wsConfig.metadata!.name = workspaceName;
            await testWorkspaceUtils.createWsFromDevFile(wsConfig);
        });

        test('Wait until created workspace is started', async () => {
            await browserTabsUtil.navigateTo(workspaceUrl);
            await ide.waitAndSwitchToIdeFrame();
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
            await workspaceHandling.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
