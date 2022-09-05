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
import { Ide } from '../../../pageobjects/ide/theia/Ide';
import { ProjectTree } from '../../../pageobjects/ide/theia/ProjectTree';
import { Editor } from '../../../pageobjects/ide/theia/Editor';
import { TestConstants } from '../../../TestConstants';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { Terminal } from '../../../pageobjects/ide/theia/Terminal';
import { Logger } from '../../../utils/Logger';
import { WorkspaceHandlingTestsTheia } from '../../../testsLibrary/theia/WorkspaceHandlingTestsTheia';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { Key } from 'selenium-webdriver';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const workspaceHandlingTests: WorkspaceHandlingTestsTheia = e2eContainer.get(CLASSES.WorkspaceHandlingTestsTheia);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/main/tests/e2e/files/devfiles/plugins/VscodeValePlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'che-docs';
const pathToFile: string = `${projectName}/modules/administration-guide/partials`;
const docFileName: string = 'assembly_authenticating-users.adoc';

suite('The "VscodeValePlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        test('Wait until created workspace is started', async () => {
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await ide.waitNotificationAndClickOnButton('Do you trust the authors of', 'Yes, I trust', 60_000);
        });
    });

    suite('Check workspace readiness to work', async () => {
        test('Wait until project is imported', async () => {
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, 'modules');
        });
    });

    suite('Check the "vale" plugin', async () => {
        test('Check warning in the editor appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, docFileName);
            await editor.waitInfoInLine(16, docFileName);
        });

        test('Open the "Problems" terminal tab', async () => {
            await editor.type(docFileName, Key.chord(Key.CONTROL, Key.SHIFT, 'm'), 3);
            await terminal.waitTab('Problems', 60_000);
        });

        test('Check the vale plugin output in the "Problems" tab', async () => {
            await terminal.waitTextInProblemsTab('Keep sentences short and to the point', 60_000);
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
