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
import { TestConstants } from '../../TestConstants';
import { e2eContainer } from '../../inversify.config';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { NewWorkspace } from '../../pageobjects/dashboard/NewWorkspace';
import { CLASSES, TYPES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { Terminal } from '../../pageobjects/ide/Terminal';
import 'reflect-metadata';
import { error, Key } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { ContextMenu } from '../../pageobjects/ide/ContextMenu';

const workspaceName: string = NameGenerator.generate('wksp-test-', 5);
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const sampleName: string = 'java-web-vertx';
const fileFolderPath: string = `${sampleName}/src/main/java/io/vertx/examples/spring`;
const tabTitle: string = 'SpringExampleRunner.java';
const codeNavigationClassName: string = 'ApplicationContext.class';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const newWorkspace: NewWorkspace = e2eContainer.get(CLASSES.NewWorkspace);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const contextMenu: ContextMenu = e2eContainer.get(CLASSES.ContextMenu);

suite('Java Vert.x test', async () => {
    suite('Login and wait dashboard', async () => {
        test('Login', async () => {
            await driverHelper.navigateToUrl(TestConstants.TS_SELENIUM_BASE_URL);
            await loginPage.login();
        });
    });

    suite('Create Java Vert.x workspace ' + workspaceName, async () => {
        test('Open \'New Workspace\' page', async () => {
            await newWorkspace.openPageByUI();
        });

        test('Create and open workspace', async () => {
            await newWorkspace.createAndOpenWorkspace(workspaceName, 'Java Vert.x');
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

    suite('Language server validation', async () => {
        test('Expand project and open file in editor', async () => {
            await projectTree.expandPathAndOpenFileInAssociatedWorkspace(fileFolderPath, tabTitle);
            await editor.selectTab(tabTitle);
        });

        test('Java LS initialization', async () => {
            await ide.checkLsInitializationStart('Starting Java Language Server');
            await ide.waitStatusBarTextAbsence('Starting Java Language Server', 1800000);
            await ide.waitStatusBarTextAbsence('Building workspace', 360000);
        });

        test('Suggestion invoking', async () => {
            await ide.closeAllNotifications();
            await editor.waitEditorAvailable(tabTitle);
            await editor.clickOnTab(tabTitle);
            await editor.waitEditorAvailable(tabTitle);
            await editor.waitTabFocused(tabTitle);
            await editor.moveCursorToLineAndChar(tabTitle, 19, 11);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionWithScrolling(tabTitle, 'cancelTimer(long arg0) : boolean');
        });

        test('Error highlighting', async () => {
            await editor.type(tabTitle, 'error', 21);
            await editor.waitErrorInLine(21);
            await editor.performKeyCombination(tabTitle, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(21);
        });

        test('Autocomplete', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 18, 15);
            await editor.pressControlSpaceCombination(tabTitle);
            await editor.waitSuggestionContainer();
            await editor.waitSuggestionWithScrolling(tabTitle, 'Vertx - io.vertx.core');
        });

        test('Codenavigation', async () => {
            await editor.moveCursorToLineAndChar(tabTitle, 17, 15);
            try {
                await editor.performKeyCombination(tabTitle, Key.chord(Key.CONTROL, Key.F12));
                await editor.waitEditorAvailable(codeNavigationClassName);
            } catch (err) {
                // workaround for issue: https://github.com/eclipse/che/issues/14520
                if (err instanceof error.TimeoutError) {
                    checkCodeNavigationWithContextMenu();
                }
            }
        });

    });

    suite('Validation of project build', async () => {
        test('Build application', async () => {
            let taskName: string = 'maven build';
            await runTask(taskName);
            await quickOpenContainer.clickOnContainerItem('Continue without scanning the task output');
            await ide.waitNotification('Task ' + taskName + ' has exited with code 0.', 60000);
        });

        test('Close the terminal tasks', async () => {
            await terminal.closeTerminalTab('maven build');
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

    async function checkCodeNavigationWithContextMenu() {
        await contextMenu.invokeContextMenuOnActiveElementWithKeys();
        await contextMenu.waitContextMenuAndClickOnItem('Go to Definition');
        console.log('Known isuue https://github.com/eclipse/che/issues/14520.');
    }

});
