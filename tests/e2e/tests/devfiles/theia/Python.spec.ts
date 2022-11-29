/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES } from '../../../inversify.types';
import 'reflect-metadata';
import { LanguageServerTestsTheia } from '../../../testsLibrary/theia/LanguageServerTestsTheia';
import { e2eContainer } from '../../../inversify.config';
import { CodeExecutionTestsTheia } from '../../../testsLibrary/theia/CodeExecutionTestsTheia';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const commonLanguageServerTests: LanguageServerTestsTheia = e2eContainer.get(CLASSES.LanguageServerTestsTheia);
const codeExecutionTests: CodeExecutionTestsTheia = e2eContainer.get(CLASSES.CodeExecutionTestsTheia);

const workspaceStack: string = 'Python';
const workspaceSampleName: string = 'python-hello-world';

const taskRunName: string = 'run';
const fileFolderPath: string = `${workspaceSampleName}`;
const fileName: string = `hello-world.py`;

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(workspaceStack);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        projectAndFileTests.waitWorkspaceReadinessNoSubfolder(workspaceSampleName, false);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, fileName);
    });

    suite.skip('Run Python project', async () => {
        codeExecutionTests.runTask(taskRunName, 30_000);
        codeExecutionTests.closeTerminal(taskRunName);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.errorHighlighting(fileName, `error_text;`, 7);
        commonLanguageServerTests.suggestionInvoking(fileName, 9, 22, 'str');
        commonLanguageServerTests.autocomplete(fileName, 9, 4, 'print');
        // commonLanguageServerTests.goToImplementations(tabTitle, 19, 7, codeNavigationClassName); // there is no codenavigation in the Python devfile
    });

    suite ('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
