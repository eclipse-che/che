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
import { CLASSES } from '../../inversify.types';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { e2eContainer } from '../../inversify.config';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);

const workspaceSampleName: string = 'console-scala-simple';
const workspaceRootFolderName: string = 'example';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/src/main/scala/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.scala';
const compileTaskkName: string = 'sbt compile';
const runTaskName: string = 'sbt run';
const testTaskName: string = 'sbt test';
const stack: string = 'Scala';
let workspaceName: string;

// skipping scala to enable pre-release suite to be easily used for updates until https://github.com/eclipse/che/issues/18662 is fixed
suite.skip(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stack);

        test('Register running workspace', async () => {
            workspaceName = WorkspaceHandlingTests.getWorkspaceName();
            CheReporter.registerRunningWorkspace(workspaceName);
        });

        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
    });

    suite('Validation of commands', async () => {
        codeExecutionTests.runTask(compileTaskkName, 240_000);
        codeExecutionTests.closeTerminal(compileTaskkName);
        codeExecutionTests.runTaskInputText(runTaskName, '[info] running org.eclipse.che.examples.HelloWorld', 'Test User', 120_000);
        codeExecutionTests.closeTerminal(runTaskName);
        codeExecutionTests.runTask(testTaskName, 120_000);
        codeExecutionTests.closeTerminal(testTaskName);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.errorHighlighting(tabTitle, 'Abc:', 21);
        // commonLanguageServerTests.suggestionInvoking(tabTitle, 15, 31, 'Console scala');
        commonLanguageServerTests.autocomplete(tabTitle, 25, 28, 'name: String');
        // commonLanguageServerTests.goToImplementations(tabTitle, 19, 7, codeNavigationClassName, 30_000); // not working
    });

    suite ('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
