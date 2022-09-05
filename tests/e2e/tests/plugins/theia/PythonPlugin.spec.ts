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
import { e2eContainer } from '../../../inversify.config';
import { CLASSES } from '../../../inversify.types';
import { TestConstants } from '../../../TestConstants';
import { ProjectTree } from '../../../pageobjects/ide/theia/ProjectTree';
import { Key } from 'selenium-webdriver';
import { Editor } from '../../../pageobjects/ide/theia/Editor';
import { WorkspaceHandlingTestsTheia } from '../../../testsLibrary/theia/WorkspaceHandlingTestsTheia';
import { Logger } from '../../../utils/Logger';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { PreferencesHandlerTheia } from '../../../utils/theia/PreferencesHandlerTheia';
import { TimeoutConstants } from '../../../TimeoutConstants';

const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const workspaceHandlingTests: WorkspaceHandlingTestsTheia = e2eContainer.get(CLASSES.WorkspaceHandlingTestsTheia);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

const devFileUrl: string = 'https://github.com/che-samples/python-hello-world/tree/devfilev2';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devFileUrl}`;
const projectName: string = 'python-hello-world';
const subRootFolder: string = '.vscode';

const fileFolderPath: string = `${projectName}`;
const tabTitle: string = 'hello-world.py';

suite(`The 'PythonPlugin' test`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        projectAndFileTests.waitWorkspaceReadiness(projectName, subRootFolder);

        test('Set confirmExit preference to never', async () => {
            await preferencesHandler.setPreferenceUsingUI('application.confirmExit', 'never');
        });
    });

    suite('Language server validation', async () => {
        test('Expand project and open file in editor', async () => {
            await projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
            await editor.selectTab(tabTitle);
        });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 8, 2);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestion(tabTitle, 'sum');
        });

        test('Error highlighting', async () => {
            const textForErrorDisplaying: string = 'err';
            await editor.type(tabTitle, textForErrorDisplaying, 7);
            await editor.waitErrorInLine(7, tabTitle, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(7, tabTitle);
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
