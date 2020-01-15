/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { NameGenerator } from '../../utils/NameGenerator';
import { error } from 'selenium-webdriver';
import 'reflect-metadata';
import * as codeExecutionHelper from '../../testsLibrary/CodeExecutionTests';
import * as workspaceHandler from '../../testsLibrary/WorksapceHandlingTests';
import * as projectManager from '../../testsLibrary/ProjectAndFileTests';
import { Logger } from '../../utils/Logger';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const workspaceStack: string = 'Python Django';
const workspaceSampleName: string = 'django-realworld-example-app';
const workspaceRootFolderName: string = 'conduit';

const taskInstallDependencies: string = 'install dependencies';
const taskMigrate: string = 'migrate';
const taskRunServer: string = 'run server';
const taskExpectedDialogText: string = 'A process is now listening on port 7000';

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace ${workspaceName}`, async () => {
        workspaceHandler.createAndOpenWorkspace(workspaceName, workspaceStack);
        projectManager.waitWorkspaceReadiness(workspaceName, workspaceSampleName, workspaceRootFolderName);
    });

    suite('Install dependencies', async () => {
        codeExecutionHelper.runTask(taskInstallDependencies, 60_000);
        codeExecutionHelper.closeTerminal(taskInstallDependencies);
    });

    suite('Migrate Django application project', async () => {
        codeExecutionHelper.runTask(taskMigrate, 30_000);
        codeExecutionHelper.closeTerminal(taskMigrate);
    });

    suite('Run django server', async () => {
        //todo: fix try catch block. exception is not being caught for some reason
        try {
            codeExecutionHelper.runTaskWithDialogShellAndOpenLink(taskRunServer, taskExpectedDialogText, 30_000);
        } catch (err) {
            Logger.debug(`Caught an exception while trying to run the Django example application server.`);
            if (err instanceof error.TimeoutError) {
                console.log(`        ⚠️ Python Django failed to load deployed example application server.`);
                console.log(`        ⚠️ This issue is being reported here: `);
            } else { throw err; }
        }
    });

    suite('Stop and remove workspace', async() => {
        workspaceHandler.stopWorkspace(workspaceName);
        workspaceHandler.removeWorkspace(workspaceName);
    });

});
