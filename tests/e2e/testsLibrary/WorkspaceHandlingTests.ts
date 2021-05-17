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
import { inject, injectable } from 'inversify';
import { CLASSES } from '../inversify.types';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { Workspaces } from '../pageobjects/dashboard/Workspaces';

@injectable()
export class WorkspaceHandlingTests {

    constructor(
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard,
        @inject(CLASSES.CreateWorkspace) private readonly createWorkspace: CreateWorkspace,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces) {}

    public createAndOpenWorkspace(stack: string) {
        test(`Open 'New Workspace' page`, async () => {
            await this.dashboard.waitPage();
            await this.dashboard.clickCreateWorkspaceButton();
            await this.createWorkspace.waitPage();
            await this.createWorkspace.clickOnSample(stack);
        });
    }

    public openExistingWorkspace(workspaceName: string) {
        test('Start workspace', async () => {
            await this.dashboard.waitPage();
            await this.dashboard.clickWorkspacesButton();
            await this.workspaces.waitPage();
            await this.workspaces.clickOpenButton(workspaceName);
        });
    }

    public async stopWorkspace(workspaceName: string) {
        await this.dashboard.stopWorkspaceByUI(workspaceName);
    }

    public async removeWorkspace(workspaceName: string) {
        await this.dashboard.deleteWorkspaceByUI(workspaceName);
    }

    public async stopAndRemoveWorkspace(workspaceName: string) {
        await this.dashboard.stopAndRemoveWorkspaceByUI(workspaceName);
    }
}
