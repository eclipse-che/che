/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, Ide, ProjectTree, Editor } from '..';
import { e2eContainer } from '../inversify.config';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
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
