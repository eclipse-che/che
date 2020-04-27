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
import 'reflect-metadata';
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import * as codeExecutionTests from '../../testsLibrary/CodeExecutionTests';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const sampleName: string = 'vertx-http-example';
const fileFolderPath: string = `${sampleName}/src/main/java/io/openshift/example`;
const tabTitle: string = 'HttpApplication.java';
const codeNavigationClassName: string = 'RouterImpl.class';
const buildTaskName: string = 'maven build';
const LSstarting: string = 'Activating Language Support for Java';
const stack: string = 'Java Vert.x';

suite(`${stack} test`, async () => {

    suite (`Create ${stack} workspace ${workspaceName}`, async () => {
        workspaceHandling.createAndOpenWorkspace(workspaceName, stack);
        projectAndFileTests.waitWorkspaceReadiness(workspaceName, sampleName, 'src');
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
        workspaceHandling.stopWorkspace(workspaceName);
        workspaceHandling.removeWorkspace(workspaceName);
    });

});
