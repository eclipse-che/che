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
import { CLASSES } from '../../../inversify.types';
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

const stack: string = 'Java Spring Boot';
const workspaceSampleName: string = 'java-web-spring';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/org/springframework/samples/petclinic`;
const tabTitle: string = 'PetClinicApplication.java';
const codeNavigationClassName: string = 'SpringApplication.class';
const buildTaskName: string = 'maven build';
const runTaskName: string = 'run webapp';
const runTaskExpectedDialogue: string = 'Process 8080-tcp is now listening on port 8080. Open it ?';

suite(`${stack} test`, async () => {
    suite(`Create ${stack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stack);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
    });

    suite('Validation of workspace build', async () => {
        codeExecutionTests.runTask(buildTaskName, 720_000);
        codeExecutionTests.closeTerminal(buildTaskName);
    });

    suite('Validation of workspace execution', async () => {
        codeExecutionTests.runTaskWithNotification(runTaskName, runTaskExpectedDialogue, 120_000);
        codeExecutionTests.closeTerminal(runTaskName);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.autocomplete(tabTitle, 32, 56, 'args : String[]');
        commonLanguageServerTests.errorHighlighting(tabTitle, 'error_text', 30);
        commonLanguageServerTests.goToImplementations(tabTitle, 32, 23, codeNavigationClassName);
        commonLanguageServerTests.suggestionInvoking(tabTitle, 32, 23, 'run(Class<?>');
    });

    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
