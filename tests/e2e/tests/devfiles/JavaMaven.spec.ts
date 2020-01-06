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
import { error, Key } from 'selenium-webdriver';
import { Container } from 'inversify';
import { NameGenerator, TestConstants, inversifyConfig, CLASSES, Dashboard, NewWorkspace, Ide, ProjectTree, TopMenu, Editor, QuickOpenContainer, Terminal } from '../..';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const sampleName: string = 'console-java-simple';
const fileFolderPath: string = `${sampleName}/src/main/java/org/eclipse/che/examples`;
const tabTitle: string = 'HelloWorld.java';
const codeNavigationClassName: string = 'String.class';

const e2eContainer: Container = inversifyConfig.e2eContainer;
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const newWorkspace: NewWorkspace = e2eContainer.get(CLASSES.NewWorkspace);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);

suite('Java Maven test', async () => {
    suite('Create and run workspace ' + workspaceName, async () => {
        test(`Open 'New Workspace' page`, async () => {
            await newWorkspace.openPageByUI();
        });

        test('Create and open workspace', async () => {
            await newWorkspace.createAndOpenWorkspace(workspaceName, 'Java Maven');
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
    });

    suite('Validation of workspace build and run', async () => {
        test('Build application', async () => {
            let taskName: string = 'maven build';
            await runTask(taskName);
            await quickOpenContainer.clickOnContainerItem('Continue without scanning the task output');
            await ide.waitNotification('has exited with code 0.', 120000);
       });

        test('Close the terminal tasks', async () => {
            await terminal.closeTerminalTab('maven build');
        });

        test('Run application', async () => {
            let taskName: string = 'maven build and run';
            await runTask(taskName);
            await quickOpenContainer.clickOnContainerItem('Continue without scanning the task output');

            await ide.waitNotification('has exited with code 0.', 120000);
        });

        test('Close the terminal tasks', async () => {
            await terminal.closeTerminalTab('maven build and run');
        });
    });

    suite('Language server validation', async () => {
        test('Expand project and open file in editor', async () => {
            await projectTree.expandPathAndOpenFileInAssociatedWorkspace(fileFolderPath, tabTitle);
            await editor.selectTab(tabTitle);
        });

        test('Java LS initialization', async () => {
            await ide.waitStatusBarContains('Starting Java Language Server', 20000);
            await ide.waitStatusBarTextAbsence('Starting Java Language Server', 1800000);
            await ide.waitStatusBarTextAbsence('Building workspace', 360000);
        });

        test('Suggestion invoking', async () => {
            await ide.closeAllNotifications();
            await editor.waitEditorAvailable(tabTitle);
            await editor.clickOnTab(tabTitle);
            await editor.waitEditorAvailable(tabTitle);
            await editor.waitTabFocused(tabTitle);
            await editor.moveCursorToLineAndChar(tabTitle, 10, 20);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestion(tabTitle, 'append(char c) : PrintStream');
        });

        test('Error highlighting', async () => {
            await editor.type(tabTitle, 'error', 11);
            await editor.waitErrorInLine(11);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(7);
        });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 10, 11);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestion(tabTitle, 'System - java.lang');
        });

        test('Codenavigation', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 9, 10);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.CONTROL, Key.F12));
            await editor.waitEditorAvailable(codeNavigationClassName);
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

    async function runTask(task: string) {
        await topMenu.selectOption('Terminal', 'Run Task...');
        try {
            await quickOpenContainer.waitContainer();
        } catch (err) {
            if (err instanceof error.TimeoutError) {
                console.warn(`After clicking to the "Terminal" -> "Run Task ..." the "Quick Open Container" has not been displayed, one more try`);

                await topMenu.selectOption('Terminal', 'Run Task...');
                await quickOpenContainer.waitContainer();
            }
        }

        await quickOpenContainer.clickOnContainerItem(task);
    }
});
