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
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { Logger } from '../utils/Logger';
import { ApiUrlResolver } from '../utils/workspace/ApiUrlResolver';
import { TimeoutConstants } from '../TimeoutConstants';
import { DriverHelper } from '../utils/DriverHelper';
import { Ide } from '../pageobjects/ide/Ide';
import { By, error } from 'selenium-webdriver';

@injectable()
export class WorkspaceHandlingTests {

    private static START_WORKSPACE_PAGE_NAME_LOCATOR: By = By.xpath(`//div[@class="ui-container"]/div[@class="pf-c-page"]//div[@class="pf-c-content"]/h1`);
    private static workspaceName: string = 'undefined';
    private static parentGUID: string;

    public static getWorkspaceName(): string {
        return WorkspaceHandlingTests.workspaceName;
    }

    public static setWorkspaceName(workspaceName: string) {
        WorkspaceHandlingTests.workspaceName = workspaceName;
    }

    public setWindowHandle(guid: string) {
        WorkspaceHandlingTests.parentGUID = guid;
    }

    public getWindowHandle(): string {
        return WorkspaceHandlingTests.parentGUID;
    }

    constructor(
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard,
        @inject(CLASSES.CreateWorkspace) private readonly createWorkspace: CreateWorkspace,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
        @inject(CLASSES.ApiUrlResolver) private readonly apiUrlResolver: ApiUrlResolver,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) {}

    public createAndOpenWorkspace(stack: string) {
        test(`Create and open new workspace, stack:${stack}`, async () => {
            await this.dashboard.waitPage();
            Logger.debug(`Fetching user kubernetes namespace, storing auth token by getting workspaces API URL.`);
            await this.apiUrlResolver.getWorkspacesApiUrl();
            await this.dashboard.clickCreateWorkspaceButton();
            await this.createWorkspace.waitPage();
            WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
            await this.createWorkspace.clickOnSample(stack);
            await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
        });
    }

    public openExistingWorkspace(workspaceName: string) {
        test('Open and start existing workspace', async () => {
            await this.dashboard.waitPage();
            Logger.debug(`Fetching user kubernetes namespace, storing auth token by getting workspaces API URL.`);
            await this.apiUrlResolver.getWorkspacesApiUrl();
            await this.dashboard.clickWorkspacesButton();
            await this.workspaces.waitPage();
            await this.workspaces.clickOpenButton(workspaceName);
        });
    }

    public obtainWorkspaceNameFromStartingPage() {
        test('Obtain workspace name from workspace loader page', async() => {
            try {
                await this.driverHelper.waitVisibility(WorkspaceHandlingTests.START_WORKSPACE_PAGE_NAME_LOCATOR, TimeoutConstants.TS_WAIT_LOADER_PRESENCE_TIMEOUT);
                // it takes a while to update the element with the workspace name
                await this.driverHelper.wait(TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
                let startingWorkspaceLineContent = await this.driverHelper.getDriver().findElement(WorkspaceHandlingTests.START_WORKSPACE_PAGE_NAME_LOCATOR).getAttribute('innerHTML');
                // cutting away leading text
                WorkspaceHandlingTests.workspaceName = startingWorkspaceLineContent.substring('Starting workspace '.length).trim();
                Logger.info(`Obtained workspace name from workspace loader page: ${WorkspaceHandlingTests.workspaceName}`);
            } catch (err) {
                Logger.error(`Failed to obtain workspace name from workspace loader page: ${err}`);
                throw err;
            }
        });
    }

    public switchBackToFirstOpenIdeTabFromLeftToRight() {
        test('WorkspaceHandlingTests.switchBackToIdeTab', async () => {
            let tabs = await this.driverHelper.getDriver().getAllWindowHandles();
            Logger.trace(`WorkspaceHandlingTests.switchBackToIdeTab Found ${tabs.length} window handles, iterating...`);
            for (let i = 0; i < tabs.length; i++) {
                await this.browserTabsUtil.switchToWindow(tabs[i]);
                try {
                    await this.ide.waitIde(TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
                    Logger.debug(`WorkspaceHandlingTests.switchBackToIdeTab located and switched to IDE tab`);
                    return;
                } catch (err) {
                    if (err instanceof error.TimeoutError) {
                        Logger.warn(`WorkspaceHandlingTests.switchBackToIdeTab Locator timed out, trying with another window handle.`);
                        continue;
                    }
                    Logger.error(`WorkspaceHandlingTests.switchBackToIdeTab Received unexpected exception while trying to locate IDE tab:${err}`);
                    throw err;
                }
            }
            Logger.error(`WorkspaceHandlingTests.switchBackToIdeTab Failed to locate IDE tab, out of window handles.`);
        });
    }

    public async stopWorkspace(workspaceName: string) {
        await this.dashboard.openDashboard();
        await this.dashboard.stopWorkspaceByUI(workspaceName);
    }

    public async removeWorkspace(workspaceName: string) {
        await this.dashboard.openDashboard();
        await this.dashboard.deleteStoppedWorkspaceByUI(workspaceName);
    }

    public async stopAndRemoveWorkspace(workspaceName: string) {
        await this.dashboard.openDashboard();
        await this.dashboard.stopAndRemoveWorkspaceByUI(workspaceName);
    }
}
