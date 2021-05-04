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
import * as workspaceHandling from '../../testsLibrary/WorkspaceHandlingTests';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { e2eContainer } from '../../inversify.config';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);

const workspaceStack: string = 'Python Django';
const workspaceSampleName: string = 'django-realworld-example-app';
const workspaceRootFolderName: string = 'conduit';

const taskSetUpVenv: string = 'set up venv';
const taskInstallDependencies: string = 'install dependencies';
const taskMigrate: string = 'migrate';
const taskRunServer: string = 'run server';
const taskExpectedDialogText: string = 'Process django is now listening on port 7000. Open it ?';

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(workspaceStack);
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite('Set up venv', async () => {
        codeExecutionTests.runTask(taskSetUpVenv, 60_000);
        codeExecutionTests.closeTerminal(taskSetUpVenv);
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
        codeExecutionTests.runTaskWithNotification(taskRunServer, taskExpectedDialogText, 30_000);
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
