/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { By } from 'selenium-webdriver';
import { CLASSES, Ide, ProjectTree, Editor } from '..';
import { e2eContainer } from '../inversify.config';
import { DriverHelper } from '../utils/DriverHelper';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

export function waitWorkspaceReadiness(sampleName : string, folder: string) {
    test('Wait for workspace readiness', async () => {
        await ide.waitAndSwitchToIdeFrame();
        await ide.waitIde();
        await projectTree.openProjectTreeContainer();
        await projectTree.waitProjectImported(sampleName, folder);
    });
}

export function waitWorkspaceReadinessNoSubfolder(sampleName : string) {
    test('Wait for workspace readiness', async () => {
        await ide.waitAndSwitchToIdeFrame();
        await ide.waitIde();
        await projectTree.openProjectTreeContainer();
        await projectTree.waitProjectImportedNoSubfolder(sampleName);
    });
}

export function openFile(filePath: string, fileName: string) {
    test('Expand project and open file in editor', async () => {
        await projectTree.expandPathAndOpenFile(filePath, fileName);
        await editor.selectTab(fileName);
    });
}

export function checkFileNotExists(filePath: string) {
    test('Check that file is not exist in project', async () => {
        await projectTree.waitItemDisappearance(filePath);
    });
}

export function checkProjectBranchName(branchName: string) {
    test('Check branch name is ${}', async () => {
        await driverHelper.waitVisibility(By.xpath(`//div[@id='theia-statusBar']/div//span[text()=' ${branchName}']`));
    });
}
