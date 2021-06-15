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

const workspaceSampleName: string = 'vertx-http-example';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/io/openshift/example`;
const tabTitle: string = 'HttpApplication.java';
const codeNavigationClassName: string = 'RouterImpl.class';
const buildTaskName: string = 'maven build';
const stack: string = 'Java Vert.x';
let workspaceName: string;

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
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

    suite('Validation of project build', async () => {
        codeExecutionTests.runTask(buildTaskName, 120_000);
        codeExecutionTests.closeTerminal(buildTaskName);
    });

    suite('Language server validation', async () => {
        commonLanguageServerTests.errorHighlighting(tabTitle, 'error_text;', 20);
        commonLanguageServerTests.suggestionInvoking(tabTitle, 19, 31, 'router(Vertx vertx) : Router');
        commonLanguageServerTests.autocomplete(tabTitle, 19, 7, 'Router - io.vertx.ext.web');
        commonLanguageServerTests.goToImplementations(tabTitle, 19, 7, codeNavigationClassName, 30_000); // extended timout to give LS enough time to start
    });

    suite ('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
