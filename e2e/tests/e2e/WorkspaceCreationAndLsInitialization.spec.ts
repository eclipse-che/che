/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { TYPES, CLASSES } from '../../inversify.types';
import { ILoginPage } from '../../pageobjects/login/ILoginPage';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { NameGenerator } from '../../utils/NameGenerator';
import { NewWorkspace } from '../../pageobjects/dashboard/NewWorkspace';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const namespace: string = 'che';
const sampleName: string = 'console-java-simple';
const fileFolderPath: string = `${sampleName}/src/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';

const loginPage: ILoginPage = e2eContainer.get<ILoginPage>(TYPES.LoginPage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const newWorkspace: NewWorkspace = e2eContainer.get(CLASSES.NewWorkspace);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

suite('E2E', async () => {

    suite('Login and wait dashboard', async () => {
        test('Login', async () => {
            await loginPage.login();
        });
    });

    suite('Create workspace and add plugin', async () => {
        test('Open \'New Workspace\' page', async () => {
            await newWorkspace.openPageByUI();
        });

        test('Create and open workspace', async () => {
            await newWorkspace.createAndRunWorkspace(namespace, workspaceName, 'Java Maven', sampleName);
        });
    });

    suite('Work with IDE', async () => {
        test('Wait IDE availability', async () => {
            await ide.waitWorkspaceAndIde(namespace, workspaceName);
        });

        test('Open project tree container', async () => {
            await projectTree.openProjectTreeContainer();
        });

        test('Wait project imported', async () => {
            await projectTree.waitProjectImported(sampleName, 'src');
        });

        test('Expand project and open file in editor', async () => {
            await projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
        });

        test('Check "Java Language Server" initialization by statusbar', async () => {
            await ide.waitStatusBarContains('Starting Java Language Server');
            await ide.waitStatusBarTextAbcence('Starting Java Language Server');
        });

        test('Check "Java Language Server" initialization by suggestion invoking', async () => {
            await ide.closeAllNotifications();
            await editor.waitEditorAvailable(tabTitle);
            await editor.clickOnTab(tabTitle);
            await editor.waitEditorAvailable(tabTitle);
            await editor.waitTabFocused(tabTitle);
            await editor.moveCursorToLineAndChar(tabTitle, 6, 20);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestion(tabTitle, 'append(CharSequence csq, int start, int end) : PrintStream');
        });

    });

    suite('Stop and remove workspace', async () => {
        test('Stop workspace', async () => {
            await dashboard.stopWorkspaceByUI(workspaceName);
        });

        test('Delete workspace', async () => {
            await dashboard.deleteWorkspaceByUI(workspaceName);
        });

    });

});
