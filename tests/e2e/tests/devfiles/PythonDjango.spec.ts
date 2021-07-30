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
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { e2eContainer } from '../../inversify.config';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);

const workspaceStack: string = 'Python Django';
const workspaceSampleName: string = 'django-realworld-example-app';
const workspaceRootFolderName: string = 'conduit';

const taskInstallDependencies: string = 'install dependencies';
const taskMigrate: string = 'migrate';
const taskRunServer: string = 'run server';
const taskRunServerInDebugMode: string = 'run server in debug mode';
const taskExpectedDialogText: string = 'Process django is now listening on port 7000. Open it ?';
const taskExpectedDialogTextInDebugMode: string = 'A new process is now listening on port 5678 but this port is not a current endpoint.          Would you want to add a redirect for this port so it becomes available ?';
let workspaceName: string;

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(workspaceStack);

        test('Register running workspace', async () => {
            workspaceName = WorkspaceHandlingTests.getWorkspaceName();
            CheReporter.registerRunningWorkspace(workspaceName);
        });

        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite('Install dependencies', async () => {
        codeExecutionTests.runTask(taskInstallDependencies, 60_000);
        codeExecutionTests.closeTerminal(taskInstallDependencies);
    });

    suite('Migrate Django application project', async () => {
        codeExecutionTests.runTask(taskMigrate, 30_000);
        codeExecutionTests.closeTerminal(taskMigrate);
    });

    suite('Run django server', async () => {
        codeExecutionTests.runTaskWithNotificationAndOpenLinkPreviewNoUrl(taskRunServer, taskExpectedDialogText, 30_000);
    });

    suite('Run django server in debug mode', async () => {
        codeExecutionTests.runTaskWithNotification(taskRunServerInDebugMode, taskExpectedDialogTextInDebugMode, 30_000);
    });

    suite ('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });
});
