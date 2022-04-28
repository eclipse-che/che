/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES } from '../../inversify.types';
import 'reflect-metadata';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { e2eContainer } from '../../inversify.config';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);

const workspaceStack: string = 'Quarkus CLI';
const workspaceSampleName: string = 'quarkus-quickstarts';
const workspaceRootFolderName: string = 'getting-started-command-mode';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/src/main/java/org/acme/getting/started/commandmode`;
const fileName: string = `GreetingService.java`;

const taskPackage: string = 'Package';
const taskPackageNative: string = 'Package Native';
const taskStartNative: string = 'Start Native';

suite(`${workspaceStack} test`, async () => {
    suite(`Create ${workspaceStack}`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(workspaceStack);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);
    });

    suite(`Test opening the file`, async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, fileName);
    });

    suite('Package Quarkus application', async () => {
        codeExecutionTests.runTask(taskPackage, 180_000);
        codeExecutionTests.closeTerminal(taskPackage);
    });

    suite('Package Quarkus Native bundle', async () => {
        codeExecutionTests.runTask(taskPackageNative, 600_000);
        codeExecutionTests.closeTerminal(taskPackageNative);
    });

    // test is being skipped because of broken devfile, link: https://github.com/eclipse/che/issues/18982
    suite.skip('Start Quarkus Native application', async () => {
        codeExecutionTests.runTaskInputText(taskStartNative, 'Enter your name', 'Test User', 90_000);
    });

    suite(`'Language server validation'`, async () => {
        commonLanguageServerTests.errorHighlighting(fileName, 'error_text;', 7);
        commonLanguageServerTests.suggestionInvoking(fileName, 8, 33, 'String');
        commonLanguageServerTests.autocomplete(fileName, 8, 33, 'String');
        commonLanguageServerTests.goToImplementations(fileName, 8, 33, 'String.class', 30_000); // extended timout to give LS enough time to start
    });

    suite('Stop and remove workspace', async() => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
