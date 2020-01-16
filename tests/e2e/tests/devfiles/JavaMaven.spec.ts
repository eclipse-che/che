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
import { NameGenerator} from '../..';
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import * as LSTests from '../../testsLibrary/LSTests';
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import * as codeExecutionTests from '../../testsLibrary/CodeExecutionTests';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const sampleName: string = 'console-java-simple';
const fileFolderPath: string = `${sampleName}/src/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';
const codeNavigationClassName: string = 'String.class';
const stack : string = 'Java Maven';
const taskName: string = 'maven build';

suite('Java Maven test', async () => {
    suite (`Create ${stack} workspace ${workspaceName}`, async () => {
        workspaceHandling.createAndOpenWorkspace(workspaceName, stack);
        projectAndFileTests.waitWorkspaceReadiness(workspaceName, sampleName, 'src');
    });

    suite('Validation of workspace build and run', async () => {
        codeExecutionTests.runTask(taskName, 120000);
        codeExecutionTests.closeTerminal(taskName);
    });

    suite('Language server validation', async () => {
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        LSTests.waitLSInitialization('Starting Java Language Server', 1800000, 360000);
        LSTests.suggestionInvoking(tabTitle, 10, 20, 'append(char c) : PrintStream');
        LSTests.errorHighlighting(tabTitle, 'error', 11);
        LSTests.autocomplete(tabTitle, 10, 11, 'System - java.lang');
        LSTests.codeNavigation(tabTitle, 9, 10, codeNavigationClassName);
    });

    suite ('Stop and remove workspace', async() => {
        workspaceHandling.stopWorkspace(workspaceName);
        workspaceHandling.removeWorkspace(workspaceName);
    });
});
