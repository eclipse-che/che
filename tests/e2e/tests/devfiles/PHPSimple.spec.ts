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
import * as workspaceHandling from '../../testsLibrary/WorkspaceHandlingTests';
import { e2eContainer } from '../../inversify.config';
import { WorkspaceNameHandler, Editor, CLASSES } from '../..';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

const workspaceSampleName: string = 'php-web-simple';
const fileFolderPath: string = `${workspaceSampleName}`;
const tabTitle: string = 'index.php';
// const codeNavigationClassName: string = 'RouterImpl.class';
const depTaskName: string = 'Configure Apache Web Server DocumentRoot';
const buildTaskName: string = 'Start Apache Web Server';
const stack: string = 'PHP Simple';

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(stack);
        projectAndFileTests.waitWorkspaceReadinessNoSubfolder(workspaceSampleName);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        prepareEditorForLanguageServerTests();
    });

    suite('Configuration of dependencies', async () => {
        codeExecutionTests.runTask(depTaskName, 30_000);
    });

    suite('Validation of project build', async () => {
        codeExecutionTests.runTask(buildTaskName, 30_000);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.errorHighlighting(tabTitle, `error_text;`, 14);
        commonLanguageServerTests.suggestionInvoking(tabTitle, 14, 26, '$test');
        commonLanguageServerTests.autocomplete(tabTitle, 15, 5, 'phpinfo');
        // commonLanguageServerTests.codeNavigation(tabTitle, 19, 7, codeNavigationClassName); // there is no codenavigation in the php simple stack (no object oriented code)
    });

    suite ('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup(async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test(`Stop and remowe workspace`, async () => {
            await workspaceHandling.stopAndRemoveWorkspace(workspaceName);
        });
    });

});

export function prepareEditorForLanguageServerTests() {
    test(`Prepare file for LS tests`, async () => {
        await editor.moveCursorToLineAndChar(tabTitle, 12, 4);
        await editor.performKeyCombination(tabTitle, '\n$test = " test";');
        await editor.moveCursorToLineAndChar(tabTitle, 14, 20);
        await editor.performKeyCombination(tabTitle, ' . $test;\nphpinfo();');
    });
}
