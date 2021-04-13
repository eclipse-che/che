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
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import * as workspaceHandling from '../../testsLibrary/WorkspaceHandlingTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as codeExecutionTests from '../../testsLibrary/CodeExecutionTests';
import { WorkspaceNameHandler } from '../..';

const workspaceSampleName: string = 'vertx-http-example';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/io/openshift/example`;
const tabTitle: string = 'HttpApplication.java';
const codeNavigationClassName: string = 'RouterImpl.class';
const buildTaskName: string = 'maven build';
const stack: string = 'Java Vert.x';

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(stack);
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
        commonLsTests.errorHighlighting(tabTitle, 'error_text;', 20);
        commonLsTests.suggestionInvoking(tabTitle, 19, 31, 'router(Vertx vertx) : Router');
        commonLsTests.autocomplete(tabTitle, 19, 7, 'Router - io.vertx.ext.web');
        commonLsTests.codeNavigation(tabTitle, 19, 7, codeNavigationClassName, 30_000); // extended timout to give LS enough time to start
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
