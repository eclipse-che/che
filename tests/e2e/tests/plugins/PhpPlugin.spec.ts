/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { WorkspaceNameHandler } from '../..';
import 'reflect-metadata';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide, LeftToolbarButton } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Key } from 'selenium-webdriver';
import { Editor } from '../../pageobjects/ide/Editor';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/PhpPluginTest.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'php-web-simple';
const subRootFolder: string = 'README.md';

const fileFolderPath: string = `${projectName}`;
const tabTitle: string = 'index.php';

suite(`The 'PhpPlugin' tests`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, subRootFolder);
        });
    });

    suite('The php plugin test', async () => {
        test('Open file', async () => {
            await projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
            await editor.selectTab(tabTitle);
        });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 13, 2);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestion(tabTitle, 'error_log');
        });

        test('Error highlighting', async () => {
            const textForErrorDisplaying: string = '*';
            await editor.type(tabTitle, textForErrorDisplaying, 13);
            await editor.waitErrorInLine(13, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
            await editor.type(tabTitle, Key.chord(Key.DELETE), 13);
            await editor.waitErrorInLineDisappearance(13);
        });
    });

    suite(`The 'Php-debug' plugin test`, async () => {
        test('Activate breakpoint', async () => {
            await projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
            await editor.activateBreakpoint(tabTitle, 13);
        });

        test('Run debug', async () => {
            await topMenu.selectOption('View', 'Debug');
            await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
            await debugView.clickOnDebugConfigurationDropDown();
            await debugView.clickOnDebugConfigurationItem('Launch currently open script (php-web-simple)');
            await debugView.clickOnRunDebugButton();
        });

        test('Check breakpoint stopped', async () => {
            await editor.waitStoppedDebugBreakpoint(tabTitle, 13, 60_000);
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                let workspaceName = await WorkspaceNameHandler.getNameFromUrl();
                await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });
});
