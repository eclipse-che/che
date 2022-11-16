/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES } from '../../../inversify.types';
import { e2eContainer } from  '../../../inversify.config';
import 'reflect-metadata';
import { Logger } from '../../../utils/Logger';
import { PreferencesHandlerTheia } from '../../../utils/theia/PreferencesHandlerTheia';
import { LanguageServerTestsTheia } from '../../../testsLibrary/theia/LanguageServerTestsTheia';
import { CodeExecutionTestsTheia } from '../../../testsLibrary/theia/CodeExecutionTestsTheia';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const commonLanguageServerTests: LanguageServerTestsTheia = e2eContainer.get(CLASSES.LanguageServerTestsTheia);
const codeExecutionTests: CodeExecutionTestsTheia = e2eContainer.get(CLASSES.CodeExecutionTestsTheia);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

const workspaceStack: string = 'Go';
const workspaceSampleName: string = 'golang-example';
const workspaceSubfolderName: string = 'template';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceSubfolderName}`;
const fileName: string = `main.go`;

const taskRunServer: string = 'run-outyet';
const taskStopServer: string = 'stop-outyet';
const taskTestOutyet: string = 'test-outyet';
const notificationText: string = 'Process 8080-tcp is now listening on port 8080. Open it ?';

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(workspaceStack);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceSubfolderName, false);
        test('Workaround for issue #16113', async () => {
            Logger.warn(`Manually setting a preference for golang devFile LS based on issue: https://github.com/eclipse/che/issues/16113`);
            await preferencesHandler.setPreferenceUsingUI('go.useLanguageServer', true);
        });
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, fileName);
    });

    suite('Test golang example', async () => {
        codeExecutionTests.runTask(taskTestOutyet, 80_000);
        codeExecutionTests.closeTerminal(taskTestOutyet);
    });

    suite('Run golang example server', async () => {
        codeExecutionTests.runTaskWithNotification(taskRunServer, notificationText, 40_000);
        codeExecutionTests.runTask(taskStopServer, 8_000);
    });

    suite(`'Language server validation'`, async () => {
        commonLanguageServerTests.suggestionInvoking(fileName, 41, 49, 'Parse');
        commonLanguageServerTests.autocomplete(fileName, 41, 49, 'Parse');
        commonLanguageServerTests.errorHighlighting(fileName, 'error;\n', 41);
        // commonLanguageServerTests.goToImplementations(fileName, 42, 10, 'flag.go'); // codenavigation is inconsistent https://github.com/eclipse/che/issues/16929
    });

    suite('Stop and remove workspace', async() => {
        test(`Stop and remove workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });

});
