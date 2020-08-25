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

const workspaceStack: string = 'Quarkus Tools';
const workspaceSampleName: string = 'quarkus-quickstarts';
const workspaceRootFolderName: string = 'getting-started';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/src/main/java/org/acme/getting/started`;
const fileName: string = `GreetingService.java`;

const taskPackage: string = 'Package';
const taskPackageNative: string = 'Package Native';
const taskStartNative: string = 'Start Native';
const taskExpectedDialogText: string = 'A process is now listening on port 8080';

suite(`${workspaceStack} test`, async () => {
    suite(`Create ${workspaceStack}`, async () => {
        workspaceHandler.createAndOpenWorkspace(workspaceStack);
        projectManager.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite(`Test opening the file`, async () => {
        // opening file that soon should give time for LS to initialize
        projectManager.openFile(fileFolderPath, fileName);
    });

    suite('Package Quarkus application', async () => {
        codeExecutionHelper.runTask(taskPackage, 180_000);
        codeExecutionHelper.closeTerminal(taskPackage);
    });

    suite.skip('Package Quarkus Native bundle', async () => { // enable again once https://github.com/eclipse/che/issues/17356 is fixed
        codeExecutionHelper.runTask(taskPackageNative, 600_000);
        codeExecutionHelper.closeTerminal(taskPackageNative);
    });
    suite.skip('Start Quarkus Native application', async () => { // enable again once https://github.com/eclipse/che/issues/17356 is fixed
        codeExecutionHelper.runTaskWithDialogShellAndOpenLink(taskStartNative, taskExpectedDialogText, 90_000);
    });

    suite(`'Language server validation'`, async () => {
        commonLsTests.errorHighlighting(fileName, 'error_text;', 7);
        commonLsTests.suggestionInvoking(fileName, 8, 33, 'String');
        commonLsTests.autocomplete(fileName, 8, 33, 'String');
        commonLsTests.codeNavigation(fileName, 8, 33, 'String.class');
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
