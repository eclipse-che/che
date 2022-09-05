/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../../inversify.config';
import { CLASSES } from '../../../inversify.types';
import { ProjectTree } from '../../../pageobjects/ide/theia/ProjectTree';
import { Editor } from '../../../pageobjects/ide/theia/Editor';
import { TestConstants } from '../../../TestConstants';
import { WorkspaceHandlingTestsTheia } from '../../../testsLibrary/theia/WorkspaceHandlingTestsTheia';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { PreferencesHandlerTheia } from '../../../utils/theia/PreferencesHandlerTheia';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { Key } from 'selenium-webdriver';

const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandlingTests: WorkspaceHandlingTestsTheia = e2eContainer.get(CLASSES.WorkspaceHandlingTestsTheia);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

const devfileUrl: string = TestConstants.TS_TEST_WORKSPACE_DEVFILE_REPO || 'https://github.com/che-samples/web-nodejs-sample/tree/xml-plugin';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'web-nodejs-sample';
const pathToFile: string = `${projectName}`;
const xmlFileName: string = 'hello.xml';
const subRootFolder: string = 'app';

suite('The "VscodeXmlPlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        projectAndFileTests.waitWorkspaceReadiness(projectName, subRootFolder);
    });

    suite('Check the "vscode-xml" plugin', async () => {
        test('Set confirmExit preference to never', async () => {
            await preferencesHandler.setPreferenceUsingUI('application.confirmExit', 'never');
        });

        test('Check autocomplete', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);
            await editor.waitSuggestion(xmlFileName, 'rollback', 60000, 16, 4);
        });

        test('Check error appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.type(xmlFileName, '\$\%\^\#', 16);
            await editor.waitErrorInLine(16, xmlFileName);
        });

        test('Check error disappearance', async () => {
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(16, xmlFileName);
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

    suite ('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTestsTheia.getWorkspaceName());
        });
    });

});
