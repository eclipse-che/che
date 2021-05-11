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
import { DriverHelper } from '../../utils/DriverHelper';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TestConstants } from '../../TestConstants';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Key } from 'selenium-webdriver';
import { Editor } from '../../pageobjects/ide/Editor';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/devfiles/plugins/PythonPluginTest.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'python-hello-world';
const subRootFile: string = 'README.md';

const fileFolderPath: string = `${sampleName}`;
const tabTitle: string = 'hello-world.py';

suite(`The 'PythonPlugin' test`, async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await driverHelper.navigateToUrl(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(sampleName, subRootFile);
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
            await editor.waitErrorInLine(7, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(7);
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup(async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test(`Stop and remove workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
