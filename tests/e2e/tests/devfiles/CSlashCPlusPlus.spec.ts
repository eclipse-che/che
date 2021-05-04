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
import { Editor, CLASSES } from '../..';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

const workspaceSampleName: string = 'cpp-hello-world';
const fileFolderPath: string = `${workspaceSampleName}`;
const tabTitle: string = 'hello.cpp';
const buildTaskName: string = 'build';
const runTaskName: string = 'run';
const stack: string = 'C/C++';

suite(`${stack} test`, async () => {
    suite(`Create ${stack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(stack);
        projectAndFileTests.waitWorkspaceReadinessNoSubfolder(workspaceSampleName);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        prepareEditorForLanguageServerTests();
    });

    suite('Validation of project build', async () => {
        codeExecutionTests.runTask(buildTaskName, 30_000);
        codeExecutionTests.runTask(runTaskName, 30_000);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.errorHighlighting(tabTitle, `error_text;`, 12);
        commonLanguageServerTests.suggestionInvoking(tabTitle, 15, 22, 'test');
        commonLanguageServerTests.autocomplete(tabTitle, 15, 9, 'printf');
        // commonLanguageServerTests.codeNavigation(tabTitle, 15, 9, 'stdio.h'); currently not working because of LS not exposing Ctrl + F12 combination
    });

    suite('Stopping and deleting the workspace', async () => {
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
        await editor.moveCursorToLineAndChar(tabTitle, 6, 1);
        await editor.performKeyCombination(tabTitle, '#include <cstdio>\n');
        await editor.moveCursorToLineAndChar(tabTitle, 10, 1);
        await editor.performKeyCombination(tabTitle, '\nchar const *test = "test";\n');
        await editor.moveCursorToLineAndChar(tabTitle, 15, 5);
        await editor.performKeyCombination(tabTitle, 'printf("%s\\n", test);\n');
    });
}
