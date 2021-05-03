/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES, WorkspaceNameHandler } from '../..';
import 'reflect-metadata';
import * as codeExecutionHelper from '../../testsLibrary/CodeExecutionTests';
import * as projectManager from '../../testsLibrary/ProjectAndFileTests';
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import * as workspaceHandling from '../../testsLibrary/WorkspaceHandlingTests';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { e2eContainer } from '../../inversify.config';

const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const workspaceStack: string = 'Python';
const workspaceSampleName: string = 'python-hello-world';

const taskRunName: string = 'run';
const fileFolderPath: string = `${workspaceSampleName}`;
const fileName: string = `hello-world.py`;

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(workspaceStack);
        projectManager.waitWorkspaceReadinessNoSubfolder(workspaceSampleName);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, fileName);
    });

    suite.skip('Run Python project', async () => {
        codeExecutionHelper.runTask(taskRunName, 30_000);
        codeExecutionHelper.closeTerminal(taskRunName);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.errorHighlighting(fileName, `error_text;`, 7);
        commonLanguageServerTests.suggestionInvoking(fileName, 9, 22, 'str');
        commonLanguageServerTests.autocomplete(fileName, 9, 4, 'print');
        // commonLanguageServerTests.codeNavigation(tabTitle, 19, 7, codeNavigationClassName); // there is no codenavigation in the Python devfile
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
