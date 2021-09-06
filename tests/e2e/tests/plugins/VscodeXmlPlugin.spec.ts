/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { Key } from 'selenium-webdriver';
import { e2eContainer } from '../../inversify.config';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { Ide } from '../../pageobjects/ide/Ide';
import { CLASSES } from '../../inversify.types';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';
import { TestConstants } from '../../TestConstants';
import { TimeoutConstants } from '../../TimeoutConstants';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/VscodeXmlPlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'nodejs-web-app';
const pathToFile: string = `${projectName}`;
const xmlFileName: string = 'hello.xml';
let workspaceName: string = '';

suite('The "VscodeXmlPlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            workspaceName = await workspaceNameHandler.getNameFromUrl();
            CheReporter.registerRunningWorkspace(workspaceName);

            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await ide.waitNotificationAndClickOnButton('Do you trust the authors of', 'Yes, I trust', 60_000);
        });
    });

    suite('Check workspace readiness to work', async () => {
        test('Wait until project is imported', async () => {
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, 'app');
        });
    });

    suite('Check the "vscode-xml" plugin', async () => {
        test('Check autocomplete', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);
            await editor.waitSuggestion(xmlFileName, 'rollback', 60000, 16, 4);
        });

        test('Check error appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.type(xmlFileName, '\$\%\^\#', 16);
            await editor.waitErrorInLine(16);
        });

        test('Check error disappearance', async () => {
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(16);
        });

        test('Check auto-close tags', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.type(xmlFileName, '<test>', 21);
            await editor.waitText(xmlFileName, '<test></test>', 60000, 10000);

            await editor.performKeyCombination(xmlFileName, Key.chord(Key.CONTROL, 'z'));
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
        });

        test('To unformat the "xml" file', async () => {
            const expectedTextBeforeFormating: string = '<constant    >Hello World!!!';
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.selectTab(xmlFileName);
            await editor.moveCursorToLineAndChar(xmlFileName, 25, 22);
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.SPACE, Key.SPACE, Key.SPACE, Key.SPACE));
            await editor.waitText(xmlFileName, expectedTextBeforeFormating, 60000, 10000);
        });

        test('To format the "xml" document', async () => {
            const expectedTextAfterFormating: string = '<constant>Hello World!!!';

            await editor.type(xmlFileName, Key.chord(Key.CONTROL, Key.SHIFT, 'I'), 25);
            await editor.waitText(xmlFileName, expectedTextAfterFormating, 60000, 10000);
        });

    });

    suite('Delete workspace', async () => {
        test('Stop and remove workspace', async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });

});
