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

const stack: string = 'Java Spring Boot';
const workspaceSampleName: string = 'java-web-spring';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/org/springframework/samples/petclinic`;
const tabTitle: string = 'PetClinicApplication.java';
const codeNavigationClassName: string = 'SpringApplication.class';
const buildTaskName: string = 'maven build';
const runTaskName: string = 'run webapp';
const runTaskExpectedDialogue: string = 'Process 8080-tcp is now listening on port 8080. Open it ?';
let workspaceName: string;

suite(`${stack} test`, async () => {
    suite(`Create ${stack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stack);

        test('Register running workspace', async () => {
            workspaceName = WorkspaceHandlingTests.getWorkspaceName();
            CheReporter.registerRunningWorkspace(workspaceName);
        });

        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
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
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });
});
