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
import * as commonTestMethods from '../CommonTestMethods';
import * as commonLSTests from '../CommonLSTests';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const sampleName: string = 'console-java-simple';
const fileFolderPath: string = `${sampleName}/src/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';
const codeNavigationClassName: string = 'String.class';

suite('Java Maven test', async () => {
    commonTestMethods.createAndOpenWorkspace(workspaceName, 'Java Maven');
    commonTestMethods.waitIdeAndProjectImported(workspaceName, sampleName, 'src');

    suite('Validation of workspace build and run', async () => {
        commonTestMethods.runTaskAndCloseTerminal('maven build', 120000);
        commonTestMethods.runTaskAndCloseTerminal('maven build and run', 120000);
    });

    suite('Language server validation', async () => {
        commonTestMethods.openFileInAssociatedWorkspace(fileFolderPath, tabTitle);

        commonLSTests.waitLSInitialization('Starting Java Language Server', 1800000, 360000);
        commonLSTests.suggestionInvoking(tabTitle, 10, 20, 'append(char c) : PrintStream');
        commonLSTests.errorHighlighting(tabTitle, 'error', 11);
        commonLSTests.autocomplete(tabTitle, 10, 11, 'System - java.lang');
        commonLSTests.codeNavigation(tabTitle, 9, 10, codeNavigationClassName);
    });
    commonTestMethods.stopAndRemoveWorkspace(workspaceName);
});
