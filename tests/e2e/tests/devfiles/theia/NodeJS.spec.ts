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
import { WorkspaceHandlingTestsTheia } from '../../../testsLibrary/theia/WorkspaceHandlingTestsTheia';
import CheReporter from '../../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTestsTheia = e2eContainer.get(CLASSES.WorkspaceHandlingTestsTheia);
const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const commonLanguageServerTests: LanguageServerTestsTheia = e2eContainer.get(CLASSES.LanguageServerTestsTheia);
const codeExecutionTests: CodeExecutionTestsTheia = e2eContainer.get(CLASSES.CodeExecutionTestsTheia);

const workspaceStack: string = 'NodeJS Express Web Application';
const workspaceSampleName: string = 'nodejs-web-app';
const workspaceRootFolderName: string = 'app';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}`;
const fileName: string = `app.js`;

const taskDownloadDependencies: string = 'download dependencies';
const taskRunWebApp: string = 'run the web app';
const taskExpectedDialogText: string = 'Process nodejs is now listening on port 3000. Open it ?';

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack}`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(workspaceStack);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTestsTheia.getWorkspaceName());
        });
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, fileName);
    });

    suite('Download dependencies', async () => {
        codeExecutionTests.runTask(taskDownloadDependencies, 60_000);
        codeExecutionTests.closeTerminal(taskDownloadDependencies);
    });

    suite('Run nodejs application', async () => {
        codeExecutionTests.runTaskWithNotification(taskRunWebApp, taskExpectedDialogText, 30_000);
    });

    suite(`'Language server validation'`, async () => {
        commonLanguageServerTests.errorHighlighting(fileName, 'error text;\n', 17);
        commonLanguageServerTests.suggestionInvoking(fileName, 15, 20, 'require');
        commonLanguageServerTests.autocomplete(fileName, 15, 20, 'require');
        // commonLanguageServerTests.goToImplementations(fileName, 19, 10, 'index.d.ts'); // codenavigation is inconsistent https://github.com/eclipse/che/issues/16929
    });

    suite('Stop and remove workspace', async() => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTestsTheia.getWorkspaceName());
        });
    });
});
