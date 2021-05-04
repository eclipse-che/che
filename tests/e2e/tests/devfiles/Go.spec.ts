/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES, WorkspaceNameHandler } from '../..';
import { e2eContainer } from  '../../inversify.config';
import 'reflect-metadata';
import { Logger } from '../../utils/Logger';
import { PreferencesHandler } from '../../utils/PreferencesHandler';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);

const workspaceStack: string = 'Go';
const workspaceSampleName: string = 'src';
const workspaceRootFolderName: string = 'github.com';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/golang/example/outyet`;
const fileName: string = `main.go`;

const taskRunServer: string = '1.1 Run outyet';
const taskStopServer: string = '1.2 Stop outyet';
const taskTestOutyet: string = '1.3 Test outyet';
const notificationText: string = 'Process 8080-tcp is now listening on port 8080. Open it ?';

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        test('Workaround for issue #16113', async () => {
            Logger.warn(`Manually setting a preference for golang devfile LS based on issue: https://github.com/eclipse/che/issues/16113`);
            await preferencesHandler.setUseGoLanaguageServer();
        });
        workspaceHandlingTests.createAndOpenWorkspace(workspaceStack);
        projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, fileName);
    });

    suite('Test golang example', async () => {
        codeExecutionTests.runTask(taskTestOutyet, 60_000);
        codeExecutionTests.closeTerminal(taskTestOutyet);
    });

    suite('Run golang example server', async () => {
        codeExecutionTests.runTaskWithNotification(taskRunServer, notificationText, 40_000);
        codeExecutionTests.runTask(taskStopServer, 5_000);
    });

    suite(`'Language server validation'`, async () => {
        commonLanguageServerTests.suggestionInvoking(fileName, 42, 10, 'Parse');
        commonLanguageServerTests.autocomplete(fileName, 42, 10, 'Parse');
        commonLanguageServerTests.errorHighlighting(fileName, 'error;\n', 42);
        // commonLanguageServerTests.codeNavigation(fileName, 42, 10, 'flag.go'); // codenavigation is inconsistent https://github.com/eclipse/che/issues/16929
    });

    suite('Stop and remove workspace', async() => {
        let workspaceName = 'not defined';
        suiteSetup(async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });

        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
        });
    });

});
