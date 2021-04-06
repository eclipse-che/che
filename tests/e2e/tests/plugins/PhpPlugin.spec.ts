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
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
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

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

const devfileUrl: string = 'https://gist.githubusercontent.com/Ohrimenko1988/d731a6e7b2ad619ab2c6fc5c7d225a90/raw/b5e9d342805a6a322f034b125fcfd623a48cb81a/PhpPlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const codeNavigationClassName: string = 'OpenDefinition.ts';
const projectName: string = 'web-php-simple';
const subRootFolder: string = 'README.md';
const sampleBodyLocator: By = By.xpath(`//body[text()='Hello World!']`);

const fileFolderPath: string = `${projectName}`;
const tabTitle: string = 'index.php';

suite(`The 'Php plugin and php-debug' tests`, async () => {
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

        //  test('Wait until JS/Typescript LS is initialised', async () => {
        //      await ide.checkLsInitializationStart('Initializing');
        //      await ide.waitStatusBarTextAbsence('Initializing', 900_000);
        //  });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 13, 3);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestion(tabTitle, 'echo');
        });

        test('Error highlighting', async () => {
            const textForErrorDisplaying: string = '*%!';
            await editor.type(tabTitle, textForErrorDisplaying, 13);
            await editor.waitErrorInLine(13, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
            await editor.type(tabTitle, Key.chord(Key.DELETE, Key.DELETE, Key.DELETE), 13);
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
            await debugView.clickOnDebugConfigurationItem('Launch currently open script');
            await debugView.clickOnRunDebugButton();
        });

        test('Wait debug connected', async () => {
            await terminal.waitTab('Debug Console', 60_000);

            // for make sure that debug really start
            // (inner processes may not be displayed)
            await driverHelper.wait(10_000);
        });

        test('Check breakpoint stopped', async () => {
            await editor.waitStoppedDebugBreakpoint(tabTitle, 13, 60_000);
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup(async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test(`Stop and remove workspace`, async () => {
            await workspaceHandling.stopAndRemoveWorkspace(workspaceName);
        });
    });
});
