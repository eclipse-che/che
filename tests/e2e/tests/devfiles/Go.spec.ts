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
import * as codeExecutionHelper from '../../testsLibrary/CodeExecutionTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as workspaceHandler from '../../testsLibrary/WorksapceHandlingTests';
import * as projectManager from '../../testsLibrary/ProjectAndFileTests';
import { Logger } from '../../utils/Logger';
import { PreferencesHandler } from '../../utils/PreferencesHandler';

const preferencesHalder: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);

const workspaceStack: string = 'Go';
const workspaceSampleName: string = 'src';
const workspaceRootFolderName: string = 'github.com';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/golang/example/outyet`;
const fileName: string = `main.go`;

const taskTestOutyet: string = '1.3 Test outyet';
const taskRunServer: string = '1.1 Run outyet';
// const taskStopServer: string = 'stop outyet';
const taskExpectedDialogText: string = 'A process is now listening on port 8080';

suite(`${workspaceStack} test`, async () => {

    suite(`Create ${workspaceStack} workspace`, async () => {
        test('Workaround for issue #16113', async () => {
            Logger.warn(`Manually setting a preference for golang devfile LS based on issue: https://github.com/eclipse/che/issues/16113`);
            await preferencesHalder.setUseGoLanaguageServer();
        });
        workspaceHandler.createAndOpenWorkspace(workspaceStack);
        projectManager.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
    });

    suite(`'Language server validation'`, async () => {
        projectManager.openFile(fileFolderPath, fileName);
        commonLsTests.suggestionInvoking(fileName, 42, 10, 'Parse');
        commonLsTests.autocomplete(fileName, 42, 10, 'Parse');
        commonLsTests.errorHighlighting(fileName, 'error;\n', 42);
        // commonLsTests.codeNavigation(fileName, 42, 10, 'flag.go'); // codenavigation is inconsistent https://github.com/eclipse/che/issues/16929
    });

    suite('Test golang example', async () => {
        codeExecutionHelper.runTask(taskTestOutyet, 60_000);
        codeExecutionHelper.closeTerminal(taskTestOutyet);
    });

    suite('Run golang example server', async () => {
        codeExecutionHelper.runTaskWithDialogShellAndOpenLink(taskRunServer, taskExpectedDialogText, 30_000);
        // codeExecutionHelper.runTask(taskStopServer, 5_000); // stop outyet task causes the server to die with exit code 143 https://github.com/eclipse/che/issues/17005
    });

    suite('Stop and remove workspace', async() => {
        let workspaceName = 'not defined';
        suiteSetup( async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
        test (`Stop worksapce`, async () => {
            await workspaceHandler.stopWorkspace(workspaceName);
        });
        test (`Remove workspace`, async () => {
            await workspaceHandler.removeWorkspace(workspaceName);
        });
    });

});
