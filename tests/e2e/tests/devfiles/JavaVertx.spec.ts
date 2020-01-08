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
import * as commonTestMethods from '../CommonTestMethods';
import * as commonLSTests from '../CommonLSTests';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const sampleName: string = 'vertx-http-example';
const fileFolderPath: string = `${sampleName}/src/main/java/io/openshift/example`;
const tabTitle: string = 'HttpApplication.java';
const codeNavigationClassName: string = 'RouterImpl.class';
const buildTaskName: string = 'maven build';
const LSstarting: string = 'Starting Java Language Server';

suite('Java Vert.x test', async () => {

    commonTestMethods.createAndOpenWorkspace(workspaceName, 'Java Vert.x');
    commonTestMethods.waitIdeAndProjectImported(workspaceName, sampleName, 'src');

    suite('Language server validation', async () => {
        commonTestMethods.openFileInAssociatedWorkspace(fileFolderPath, tabTitle);
        commonLSTests.waitLSInitialization(LSstarting, 1800000, 360000);
        commonLSTests.suggestionInvoking(tabTitle, 19, 31, 'router(Vertx vertx) : Router');
        commonLSTests.errorHighlighting(tabTitle, 'error', 20);
        commonLSTests.autocomplete(tabTitle, 19, 7, 'Router - io.vertx.ext.web');
        commonLSTests.codeNavigation(tabTitle, 19, 7, codeNavigationClassName);
    });

    suite('Validation of project build', async () => {
        commonTestMethods.runTaskAndCloseTerminal(buildTaskName, 120000);
    });

    commonTestMethods.stopAndRemoveWorkspace(workspaceName);

});
