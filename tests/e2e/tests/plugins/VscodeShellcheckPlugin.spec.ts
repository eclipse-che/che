/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { PreferencesHandler } from '../../utils/PreferencesHandler';
import { Editor } from '../../pageobjects/ide/Editor';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Key } from 'selenium-webdriver';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../utils/Logger';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/VscodeShellcheckPlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
const pathToFile: string = `${sampleName}`;
const fileName: string = 'test.sh';
let workspaceName: string;

suite(`The 'VscodeShellcheckPlugin' test`, async () => {
    suite('Create workspace', async () => {
        test('Set shellcheck path', async () => {
            const shellcheckExecutablePathPropertyName: string = 'shellcheck.executablePath';
            const shellcheckExecutablePath: string = '/bin/shellcheck';

            await preferencesHandler.setPreference(shellcheckExecutablePathPropertyName, shellcheckExecutablePath);
        });

        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            workspaceName = await workspaceNameHandler.getNameFromUrl();
            CheReporter.registerRunningWorkspace(workspaceName);

            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await ide.waitNotificationAndClickOnButton('Do you trust the authors of', 'Yes, I trust', 60_000);

            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(sampleName, subRootFolder);
        });
    });

    suite('Check the "Shellcheck" plugin', async () => {
        test('Check errors highlighting', async () => {
            // the simple " character as a result. Workaround to avoiding autocomplete.
            const errorText: string = Key.chord('"""', Key.BACK_SPACE, Key.BACK_SPACE);

            await projectTree.expandPathAndOpenFile(pathToFile, fileName);
            await editor.type(fileName, errorText, 4);
            await editor.waitErrorInLine(4);
        });

        test('Check errors highlighting disappearance', async () => {
            await editor.type(fileName, Key.DELETE, 4);
            await editor.waitErrorInLineDisappearance(4);
        });

        test('Check warning highlighting', async () => {
            await editor.waitWarningInLine(5);
        });

        test('Uncomment the 4-th row', async () => {
            await editor.type(fileName, Key.DELETE, 3);
        });

        test('Check warning highlighting disappearance', async () => {
            await editor.waitWarningInLineDisappearance(5);
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
