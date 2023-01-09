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
import { By, error } from 'selenium-webdriver';
import { TestConstants } from '../TestConstants';

@injectable()
export class WorkspaceHandlingTests {

    public static getWorkspaceName(): string {
        return WorkspaceHandlingTests.workspaceName;
    }

    public static setWorkspaceName(workspaceName: string): void {
        WorkspaceHandlingTests.workspaceName = workspaceName;
    }

    private static START_WORKSPACE_PAGE_NAME_LOCATOR: By = By.xpath(`//div[@class="ui-container"]/div[@class="pf-c-page"]//div[@class="pf-c-content"]/h1`);
    private static READY_TO_READ_WORKSPACE_NAME_LOCATOR: By = By.xpath(`//div[@class="ui-container"]/div[@class="pf-c-page"]//div[@class="pf-c-content"]/h1[contains(.,'Starting workspace ')]`);
    private static workspaceName: string = 'undefined';
    private static parentGUID: string;

    constructor(
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard,
        @inject(CLASSES.CreateWorkspace) private readonly createWorkspace: CreateWorkspace,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
        @inject(CLASSES.ApiUrlResolver) private readonly apiUrlResolver: ApiUrlResolver,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

    public setWindowHandle(guid: string): void {
        WorkspaceHandlingTests.parentGUID = guid;
    }

    public getWindowHandle(): string {
        return WorkspaceHandlingTests.parentGUID;
    }

    public createAndOpenWorkspace(stack: string): void {
        test(`Create and open new workspace, stack:${stack}`, async () => {
            await this.dashboard.waitPage();
            Logger.debug(`Fetching user kubernetes namespace, storing auth token by getting workspaces API URL.`);
            await this.apiUrlResolver.getWorkspacesApiUrl();
            await this.dashboard.clickCreateWorkspaceButton();
            await this.createWorkspace.waitPage();
            WorkspaceHandlingTests.parentGUID = await this.browserTabsUtil.getCurrentWindowHandle();
            await this.createWorkspace.clickOnSampleForSpecificEditor(stack);
            await this.browserTabsUtil.waitAndSwitchToAnotherWindow(WorkspaceHandlingTests.parentGUID, TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
        });
    }

    public openExistingWorkspace(workspaceName: string): void {
        test('Open and start existing workspace', async () => {
            await this.dashboard.waitPage();
            Logger.debug(`Fetching user kubernetes namespace, storing auth token by getting workspaces API URL.`);
            await this.apiUrlResolver.getWorkspacesApiUrl();
            await this.dashboard.clickWorkspacesButton();
            await this.workspaces.waitPage();
            await this.workspaces.clickOpenButton(workspaceName);
        });
    }

    public obtainWorkspaceNameFromStartingPage(): void {
        test('Obtain workspace name from workspace loader page', async() => {
            Logger.info('Waiting for workspace name on workspace loader page');
            await this.driverHelper.waitVisibility(WorkspaceHandlingTests.READY_TO_READ_WORKSPACE_NAME_LOCATOR, TimeoutConstants.TS_WAIT_LOADER_PRESENCE_TIMEOUT);

            const timeout: number = TimeoutConstants.TS_IDE_LOAD_TIMEOUT;
            const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            const attempts: number = Math.ceil(timeout / polling);
            let startingWorkspaceLineContent: string = '';

            for (let i: number = 0; i < attempts; i++) {
                try {
                    startingWorkspaceLineContent = await this.driverHelper.getDriver().findElement(WorkspaceHandlingTests.START_WORKSPACE_PAGE_NAME_LOCATOR).getAttribute('innerHTML');
                } catch (err) {
                    if (err instanceof error.StaleElementReferenceError) {
                        Logger.warn(`Failed to obtain name from workspace start page, element possibly not yet visible. Retrying.`);
                        this.driverHelper.sleep(polling);
                        continue;
                    }

                    Logger.error(`Obtaining workspace name failed with an unexpected error:${err}`);
                    throw err;
                }

                // cutting away leading text
                WorkspaceHandlingTests.workspaceName = startingWorkspaceLineContent.substring('Starting workspace '.length).trim();
                if (WorkspaceHandlingTests.workspaceName !== '') {
                    Logger.info(`Obtained workspace name from workspace loader page: ${WorkspaceHandlingTests.workspaceName}`);
                    break;
                }

                this.driverHelper.sleep(polling);
            }
        });
    }

    public async stopWorkspace(workspaceName: string): Promise<void> {
        await this.dashboard.openDashboard();
        await this.dashboard.stopWorkspaceByUI(workspaceName);
    }

    public async removeWorkspace(workspaceName: string): Promise<void> {
        await this.dashboard.openDashboard();
        await this.dashboard.deleteStoppedWorkspaceByUI(workspaceName);
    }

    public async stopAndRemoveWorkspace(workspaceName: string): Promise<void> {
        await this.dashboard.openDashboard();
        await this.dashboard.stopAndRemoveWorkspaceByUI(workspaceName);
    }
}
