/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { NewWorkspace, CLASSES, Dashboard } from '..';
import { e2eContainer } from '../inversify.config';

const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const newWorkspace: NewWorkspace = e2eContainer.get(CLASSES.NewWorkspace);

export function createAndOpenWorkspace(workspaceName : string, stack: string) {
    test(`Open 'New Workspace' page`, async () => {
        await newWorkspace.openPageByUI();
        await newWorkspace.createAndOpenWorkspace(workspaceName, stack);
    });
}

export function stopWorkspace(workspaceName: string) {
    test('Stop workspace', async () => {
        await dashboard.stopWorkspaceByUI(workspaceName);
    });
}

export function removeWorkspace(workspaceName: string) {
    test('Delete workspace', async () => {
        await dashboard.deleteWorkspaceByUI(workspaceName);
    });
}
