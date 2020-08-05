/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { WorkspaceNameHandler } from '../..';
import 'reflect-metadata';
import * as codeExecutionHelper from '../../testsLibrary/CodeExecutionTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as workspaceHandler from '../../testsLibrary/WorksapceHandlingTests';
import * as projectManager from '../../testsLibrary/ProjectAndFileTests';

const workspaceStack: string = 'NodeJS Express Web Application';
const workspaceSampleName: string = 'nodejs-web-app';
const workspaceRootFolderName: string = 'app';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}`;
const fileName: string = `app.js`;

const taskDownloadDependencies: string = 'download dependencies';
const taskRunWebApp: string = 'run the web app';
const taskExpectedDialogText: string = 'A process is now listening on port 3000';

suite(`${workspaceStack} test`, async () => {
    suite(`Create ${workspaceStack}`, async () => {
        workspaceHandler.createAndOpenWorkspace(workspaceStack);
        projectManager.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });
    suite('Download dependencies', async () => {
        codeExecutionHelper.runTask(taskDownloadDependencies, 60_000);
        codeExecutionHelper.closeTerminal(taskDownloadDependencies);
    });
    suite(`'Language server validation'`, async () => {
        projectManager.openFile(fileFolderPath, fileName);
        commonLsTests.errorHighlighting(fileName, 'error text;\n', 17);
        commonLsTests.suggestionInvoking(fileName, 15, 20, 'require');
        commonLsTests.autocomplete(fileName, 15, 20, 'require');
        // commonLsTests.codeNavigation(fileName, 19, 10, 'index.d.ts'); // codenavigation is inconsistent https://github.com/eclipse/che/issues/16929
    });
    suite('Run nodejs application', async () => {
        codeExecutionHelper.runTaskWithDialogShellAndOpenLink(taskRunWebApp, taskExpectedDialogText, 30_000);
    });
    suite('Stop and remove workspace', async() => {
        let workspaceName = 'not defined';
        suiteSetup( async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
        test (`Stop worksapce`, async () => {
            await workspaceHandler.stopWorkspace(workspaceName);
        });
        test (`Remove workspace`, async () => {
            await workspaceHandler.removeWorkspace(workspaceName);
        });
    });
});
