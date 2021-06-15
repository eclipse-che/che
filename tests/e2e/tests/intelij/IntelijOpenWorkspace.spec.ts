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
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../TestConstants';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import CheReporter from '../../driver/CheReporter';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const workspaceNameHandler: WorkspaceNameHandler = e2eContainer.get(CLASSES.WorkspaceNameHandler);

const dashboardUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}`;

suite('The "IntelijOpenWorkspace" userstory', async () => {
    suite('Open workspace', async () => {
        test('Open workspace', async () => {
            await browserTabsUtil.navigateTo(`${dashboardUrl}/dashboard/#/ide/admin/java-11-intellij`);
        });

        test('Wait workspace', async () => {
            await dashboard.waitWorkspaceStartingPage();
            CheReporter.registerRunningWorkspace(await workspaceNameHandler.getNameFromUrl());
            await ide.waitAndSwitchToIdeFrame();
            await waitIntelijWorkspace();
        });
    });

});

async function waitIntelijWorkspace() {
    await driverHelper.waitVisibility(By.css('div#noVNC_container canvas'), 300000);
    await driverHelper.wait(10000);
}
