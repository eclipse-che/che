/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { WorkspaceNameHandler} from '../..';
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import * as codeExecutionTests from '../../testsLibrary/CodeExecutionTests';

const workspaceSampleName: string = 'console-java-simple';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';
const codeNavigationClassName: string = 'String.class';
const stack : string = 'Java Maven';
const taskName: string = 'maven build';

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(stack);
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite('Validation of workspace build and run', async () => {
        codeExecutionTests.runTask(taskName, 120_000);
        codeExecutionTests.closeTerminal(taskName);
    });

    suite('Language server validation', async () => {
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        commonLsTests.suggestionInvoking(tabTitle, 10, 20, 'append(char c) : PrintStream');
        commonLsTests.errorHighlighting(tabTitle, 'error', 11);
        commonLsTests.autocomplete(tabTitle, 10, 11, 'System - java.lang');
        commonLsTests.codeNavigation(tabTitle, 9, 10, codeNavigationClassName);
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
