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
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import { Key, By } from 'selenium-webdriver';
import { Editor } from '../../pageobjects/ide/Editor';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { Terminal } from '../../pageobjects/ide/Terminal';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);

const devfileUrl: string = 'https://gist.githubusercontent.com/Ohrimenko1988/e972965b5f5e64e79a5c627ee0a034bd/raw/a85cd44b3bb4201527487d713333538112c0e875/typescript-node-debug2.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const codeNavigationClassName: string = 'OpenDefinition.ts';
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
const sampleBodyLocator: By = By.xpath(`//body[text()='Hello World!']`);

const fileFolderPath: string = `${sampleName}`;
const debugFileFolderPath: string = `${sampleName}/app`
const debugFile: string = 'app.js'
const tabTitle: string = 'typescript-node-debug.ts';

suite(`The 'TypescriptPlugin and Node-debug' tests`, async () => {
    suite('Create workspace', async () => {
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

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
    });

    suite('The Typescript plugin test', async () => {
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
        test('test', async () => {
            await topMenu.runTask('run the web app (debugging enabled)');
            await ide.waitNotification('Process nodejs is now listening on port 3000.');

            const currentWindow = await (await driverHelper.getDriver()).getWindowHandle();
            await ide.clickOnNotificationButton('Process nodejs is now listening on port 3000.', 'Open In New Tab')

            await driverHelper.DefaultWindow
            await driverHelper.waitVisibility(sampleBodyLocator, 60_000)
            const applicationPreviewWindow: string = await (await driverHelper.getDriver()).getWindowHandle();

            await (await driverHelper.getDriver()).switchTo().window(currentWindow);
            await ide.waitAndSwitchToIdeFrame(60000);

            await projectTree.expandPathAndOpenFile(debugFileFolderPath, debugFile);
            await editor.activateBreakpoint(debugFile, 19)

            await topMenu.selectOption('View', 'Debug');
            await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
            await debugView.clickOnDebugConfigurationDropDown();
            await debugView.clickOnDebugConfigurationItem('Attach to Remote (nodejs-web-app)');
            await debugView.clickOnRunDebugButton();
            await waitDebugConnected()


            await (await driverHelper.getDriver()).switchTo().window(applicationPreviewWindow);
            await driverHelper.waitVisibility(sampleBodyLocator, 60_000)
            await (await driverHelper.getDriver()).navigate().refresh()


            await (await driverHelper.getDriver()).switchTo().window(currentWindow);
            await ide.waitAndSwitchToIdeFrame(60000);
            await editor.waitStoppedDebugBreakpoint(debugFile, 19, 60_000);
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

async function waitDebugConnected() {
    await terminal.waitTab('Debug Console', 60_000);

    // for make sure that debug really start 
    await driverHelper.wait(10_000)
}


async function waitAndSwitchToAnotherWindow(currentWindowHandle: string, timeout: number) {
    await driverHelper.waitUntilTrue(async () => {
        const windowHandles: string[] = await (await driverHelper.getDriver()).getAllWindowHandles();

        return windowHandles.length > 1;
    }, timeout);

    const windowHandles: string[] = await (await driverHelper.getDriver()).getAllWindowHandles();

    windowHandles.forEach(async windowHandle => {
        if (windowHandle !== currentWindowHandle) {
            await driverHelper.getDriver().getWindowHandle();
            return;
        }
    });
}
