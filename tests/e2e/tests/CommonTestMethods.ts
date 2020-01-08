/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { NewWorkspace, CLASSES, Ide, ProjectTree, TestConstants, QuickOpenContainer, Terminal, TopMenu, Dashboard, Editor } from '..';
import { e2eContainer } from '../inversify.config';
import { error } from 'selenium-webdriver';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const newWorkspace: NewWorkspace = e2eContainer.get(CLASSES.NewWorkspace);

export function createAndOpenWorkspace(workspaceName : string, stack: string) {
    suite('Create ' + stack + ' workspace ' + workspaceName, async () => {
        test('Open \'New Workspace\' page', async () => {
            await newWorkspace.openPageByUI();
        });

        test('Create and open workspace', async () => {
            await newWorkspace.createAndOpenWorkspace(workspaceName, stack);
        });
    });
}

export function waitIdeAndProjectImported(workspaceName : string, sampleName : string, folder: string) {
    suite('Work with IDE', async () => {
        test('Wait IDE availability', async () => {
            await ide.waitWorkspaceAndIde(namespace, workspaceName);
        });

        test('Open project tree container', async () => {
            await projectTree.openProjectTreeContainer();
        });

        test('Wait project imported', async () => {
            await projectTree.waitProjectImported(sampleName, folder);
        });

    });
}

export function runTaskAndCloseTerminal(taskName: string, timeout: number) {
    test('Run command \'' + taskName + '\'', async () => {
        await runTask(taskName);
        await quickOpenContainer.clickOnContainerItem('Continue without scanning the task output');
        await ide.waitNotification('has exited with code 0.', timeout);
    });

    test('Close the terminal tasks', async () => {
        await terminal.closeTerminalTab(taskName);
    });
}

export function stopAndRemoveWorkspace(workspaceName: string) {
    suite('Stop and remove workspace', async () => {
        test('Stop workspace', async () => {
            await dashboard.stopWorkspaceByUI(workspaceName);
        });

        test('Delete workspace', async () => {
            await dashboard.deleteWorkspaceByUI(workspaceName);
        });

    });
}

export function openFileInAssociatedWorkspace(filePath: string, fileName: string) {
    test('Expand project and open file in editor', async () => {
        await projectTree.expandPathAndOpenFileInAssociatedWorkspace(filePath, fileName);
        await editor.selectTab(fileName);
    });
}

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
