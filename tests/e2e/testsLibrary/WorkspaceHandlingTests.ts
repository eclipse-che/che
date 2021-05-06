/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { inject, injectable } from 'inversify';
import { CLASSES } from '../inversify.types';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { GetStarted } from '../pageobjects/dashboard/GetStarted';

@injectable()
export class WorkspaceHandlingTests {

    constructor(@inject(CLASSES.Dashboard) private readonly dashboard: Dashboard, @inject(CLASSES.GetStarted) private readonly getStarted: GetStarted) {}

    public createAndOpenWorkspace(stack: string) {
        test(`Open 'New Workspace' page`, async () => {
            await this.dashboard.waitPage();
            await this.dashboard.clickGetStartedButton();
            await this.getStarted.waitPage();
            await this.getStarted.clickOnSample(stack);
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
