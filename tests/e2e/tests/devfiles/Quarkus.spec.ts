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
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import * as projectManager from '../../testsLibrary/ProjectAndFileTests';

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
        workspaceHandling.createAndOpenWorkspace(workspaceStack);
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

    suite('Package Quarkus Native bundle', async () => {
        codeExecutionHelper.runTask(taskPackageNative, 600_000);
        codeExecutionHelper.closeTerminal(taskPackageNative);
    });

    // test is being skipped because of broken devfile, link: https://github.com/eclipse/che/issues/18982
    suite.skip('Start Quarkus Native application', async () => {
        codeExecutionHelper.runTaskInputText(taskStartNative, 'Enter your name', 'Test User', 90_000);
    });

    suite(`'Language server validation'`, async () => {
        commonLsTests.errorHighlighting(fileName, 'error_text;', 7);
        commonLsTests.suggestionInvoking(fileName, 8, 33, 'String');
        commonLsTests.autocomplete(fileName, 8, 33, 'String');
        commonLsTests.codeNavigation(fileName, 8, 33, 'String.class', 30_000); // extended timout to give LS enough time to start
    });

    suite('Stop and remove workspace', async() => {
        let workspaceName = 'not defined';
        suiteSetup(async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test(`Stop and remowe workspace`, async () => {
            await workspaceHandling.stopAndRemoveWorkspace(workspaceName);
        });
    });
});
