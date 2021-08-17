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
import { DriverHelper } from '../../utils/DriverHelper';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide, LeftToolbarButton } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Key, By } from 'selenium-webdriver';
import { Editor } from '../../pageobjects/ide/Editor';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { Terminal } from '../../pageobjects/ide/Terminal';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import CheReporter from '../../driver/CheReporter';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/TypescriptNodeDebug2PluginTest.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const codeNavigationClassName: string = 'OpenDefinition.ts';
const projectName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
const sampleBodyLocator: By = By.xpath(`//body[text()='Hello World!']`);

const fileFolderPath: string = `${projectName}`;
const debugFileFolderPath: string = `${projectName}/app`;
const debugFile: string = 'app.js';
const tabTitle: string = 'typescript-node-debug.ts';
let workspaceName: string;

suite(`The 'TypescriptPlugin and Node-debug' tests`, async () => {
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
            await projectTree.waitProjectImported(projectName, subRootFolder);
        });
    });

    suite('The Typescript plugin test', async () => {
        test('Open file', async () => {
            await projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
            await editor.selectTab(tabTitle);
        });

        test('Wait until JS/Typescript LS is initialised', async () => {
            await ide.checkLsInitializationStart('Initializing');
            await ide.waitStatusBarTextAbsence('Initializing', 900_000);
        });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 8, 22);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestion(tabTitle, 'OpenDefinition');
        });

        test('Error highlighting', async () => {
            const textForErrorDisplaying: string = '//';
            await editor.type(tabTitle, textForErrorDisplaying, 5);
            await editor.waitErrorInLine(4, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
            await editor.type(tabTitle, Key.chord(Key.DELETE, Key.DELETE), 5);
            await editor.waitErrorInLineDisappearance(4);
        });

        test('Codenavigation', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 8, 22);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.CONTROL, Key.F12));
            await editor.waitEditorAvailable(codeNavigationClassName, 60_000);
        });
    });

    suite(`The 'Node-debug' plugin test`, async () => {
        let currentWindow: string = '';
        let applicationPreviewWindow: string = '';

        test('Run application in debug mode', async () => {
            await topMenu.runTask('run the web app (debugging enabled), Global');
            await ide.waitNotification('Process nodejs is now listening on port 3000.');

            currentWindow = await browserTabsUtil.getCurrentWindowHandle();
        });

        test('Open application in the new editor window', async () => {
            await ide.clickOnNotificationButton('Process nodejs is now listening on port 3000.', 'Open In New Tab');
            await browserTabsUtil.waitAndSwitchToAnotherWindow(currentWindow, 60_000);
            await browserTabsUtil.waitContentAvailableInTheNewTab(sampleBodyLocator, 60_000);

            applicationPreviewWindow = await browserTabsUtil.getCurrentWindowHandle();
        });

        test('Switch back to the IDE window', async () => {
            await browserTabsUtil.switchToWindow(currentWindow);
            await ide.waitAndSwitchToIdeFrame(60_000);
        });

        test('Activate breakpoint', async () => {
            await projectTree.expandPathAndOpenFile(debugFileFolderPath, debugFile);
            await editor.activateBreakpoint(debugFile, 19);
        });

        test('Run debug', async () => {
            await topMenu.selectOption('View', 'Debug');
            await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);

            // workaround for the issue: https://github.com/eclipse/che/issues/20067
            await debugView.clickOnDebugConfigurationDropDown();
            await debugView.clickOnDebugConfigurationItem('Add Configuration...');

            await debugView.clickOnDebugConfigurationDropDown();
            await debugView.clickOnDebugConfigurationItem('Attach to Remote (nodejs-web-app)');
            await debugView.clickOnRunDebugButton();
        });

        test('Wait debug connected', async () => {
            await terminal.waitTab('Debug Console', 60_000);

            // for make sure that debug really start
            // (inner processes may not be displayed)
            await driverHelper.wait(10_000);
        });

        test('Refresh application sample window', async () => {
            await browserTabsUtil.switchToWindow(applicationPreviewWindow);
            await browserTabsUtil.waitContentAvailableInTheNewTab(sampleBodyLocator, 60_000);

            await browserTabsUtil.refreshForDebug();
        });

        test('Check breakpoint stopped', async () => {
            await browserTabsUtil.switchToWindow(currentWindow);
            await ide.waitAndSwitchToIdeFrame(60000);

            await editor.waitStoppedDebugBreakpoint(debugFile, 19, 60_000);
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
