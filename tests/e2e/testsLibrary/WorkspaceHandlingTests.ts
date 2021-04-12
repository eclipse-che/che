/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, Dashboard } from '..';
import { e2eContainer } from '../inversify.config';
import { CreateWorkspace as CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { Logger } from '../utils/Logger';

const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);

export function createAndOpenWorkspace(stack: string) {
    test(`Open 'New Workspace' page`, async () => {
        Logger.trace(`WorkspaceHandlingTests.createAndOpenWorkspace wait for dashboard`);
        await dashboard.waitPage();
        Logger.trace(`WorkspaceHandlingTests.createAndOpenWorkspace click Create workspace button`);
        await dashboard.clickCreateWorkspaceButton();
        Logger.trace(`WorkspaceHandlingTests.createAndOpenWorkspace wait for getting started page`);
        await createWorkspace.waitPage();
        Logger.trace(`WorkspaceHandlingTests.createAndOpenWorkspace click on sample ${stack}`);
        await createWorkspace.clickOnSample(stack);
    });
}

export async function stopWorkspace(workspaceName: string) {
    await dashboard.stopWorkspaceByUI(workspaceName);
}

export async function removeWorkspace(workspaceName: string) {
    await dashboard.deleteWorkspaceByUI(workspaceName);
}

export async function stopAndRemoveWorkspace(workspaceName: string) {
    await dashboard.stopAndRemoveWorkspaceByUI(workspaceName);
}
