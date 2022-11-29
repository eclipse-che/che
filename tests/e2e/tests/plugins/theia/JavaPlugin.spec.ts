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
import { Ide } from '../../../pageobjects/ide/theia/Ide';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { TestConstants } from '../../../TestConstants';
import { Key } from 'selenium-webdriver';
import { Editor } from '../../../pageobjects/ide/theia/Editor';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../../utils/Logger';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { PreferencesHandlerTheia } from '../../../utils/theia/PreferencesHandlerTheia';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

const devFileUrl: string = 'https://github.com/che-samples/java-guestbook/tree/devfilev2';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devFileUrl}`;
const codeNavigationClassName: string = 'String.class';
const projectName: string = 'java-guestbook';
const subRootFolder: string = 'backend';

const fileFolderPath: string = `${projectName}/backend/src/main/java/cloudcode/guestbook/backend`;
const tabTitle: string = 'GuestBookEntry.java';

suite(`The 'JavaPlugin' test`, async () => {
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

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
    });

    suite('Language server validation', async () => {
        test('Wait until Java LS is initialized', async () => {
            await ide.checkLsInitializationStart('Activating');
            await ide.waitStatusBarTextAbsence('Activating', 900_000);
        });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 15, 1);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestion(tabTitle, 'clone() : Object');
        });

        test('Error highlighting', async () => {
            const textForErrorDisplaying: string = '$';
            await editor.type(tabTitle, textForErrorDisplaying, 15);
            await editor.waitErrorInLine(15, tabTitle, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(15, tabTitle);
        });

        test('CodeNavigation', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 9, 14);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.CONTROL, Key.F12));
            await editor.waitEditorAvailable(codeNavigationClassName, 60_000);
        });

    });

    suite('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });

    });
});
