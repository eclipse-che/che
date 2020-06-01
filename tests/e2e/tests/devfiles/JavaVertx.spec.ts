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
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as codeExecutionTests from '../../testsLibrary/CodeExecutionTests';
import { WorkspaceNameHandler } from '../..';

const workspaceSampleName: string = 'vertx-http-example';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/io/openshift/example`;
const tabTitle: string = 'HttpApplication.java';
const codeNavigationClassName: string = 'RouterImpl.class';
const buildTaskName: string = 'maven build';
const LSstarting: string = 'Activating Language Support for Java';
const stack: string = 'Java Vert.x';

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(stack);
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite('Language server validation', async () => {
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        commonLsTests.waitLSInitialization(LSstarting, 1_800_000, 360_000);
        commonLsTests.suggestionInvoking(tabTitle, 19, 31, 'router(Vertx vertx) : Router');
        commonLsTests.errorHighlighting(tabTitle, 'error', 20);
        commonLsTests.autocomplete(tabTitle, 19, 7, 'Router - io.vertx.ext.web');
        commonLsTests.codeNavigation(tabTitle, 19, 7, codeNavigationClassName);
    });

    suite('Validation of project build', async () => {
        codeExecutionTests.runTask(buildTaskName, 120_000);
        codeExecutionTests.closeTerminal(buildTaskName);
    });

    suite ('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup( async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
        test (`Stop worksapce`, async () => {
            await workspaceHandling.stopWorkspace(workspaceName);
        });
        test (`Remove workspace`, async () => {
            await workspaceHandling.removeWorkspace(workspaceName);
        });
    });

});
