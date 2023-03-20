/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { CLASSES } from '../configs/inversify.types';
import { Dashboard } from '../pageobjects/dashboard/Dashboard';
import { CreateWorkspace } from '../pageobjects/dashboard/CreateWorkspace';
import { Workspaces } from '../pageobjects/dashboard/Workspaces';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { Logger } from '../utils/Logger';
import { ApiUrlResolver } from '../utils/workspace/ApiUrlResolver';
import { TimeoutConstants } from '../constants/TimeoutConstants';
import { DriverHelper } from '../utils/DriverHelper';
import { By, error } from 'selenium-webdriver';
import { TestConstants } from '../constants/TestConstants';

@injectable()
export class WorkspaceHandlingTests {

    public static getWorkspaceName(): string {
        return WorkspaceHandlingTests.workspaceName;
    }

    public static setWorkspaceName(workspaceName: string): void {
        WorkspaceHandlingTests.workspaceName = workspaceName;
    }

    private static WORKSPACE_NAME_LOCATOR: By = By.xpath(`//h1[contains(.,'Starting workspace ')]`);
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
            const timeout: number = TimeoutConstants.TS_IDE_LOAD_TIMEOUT;
            const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            const attempts: number = Math.ceil(timeout / polling);

            for (let i: number = 0; i < attempts; i++) {
                try {
                    let startingWorkspaceLineContent: string = await this.driverHelper.getDriver().findElement(WorkspaceHandlingTests.WORKSPACE_NAME_LOCATOR).getText();
                    Logger.trace(`WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage obtained starting workspace getText():${startingWorkspaceLineContent}`);
                    // cutting away leading text
                    WorkspaceHandlingTests.workspaceName = startingWorkspaceLineContent.substring('Starting workspace '.length).trim();
                    Logger.trace(`WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage trimmed workspace name from getText():${WorkspaceHandlingTests.workspaceName}`);
                    break;
                } catch (err) {
                    if (err instanceof error.StaleElementReferenceError) {
                        Logger.warn(`WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage Failed to obtain name from workspace start page, element possibly detached from DOM. Retrying.`);
                        await this.driverHelper.wait(polling);
                        continue;
                    }
                    if (err instanceof error.NoSuchElementError) {
                        Logger.warn(`WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage Failed to obtain name from workspace start page, element not visible yet. Retrying.`);
                        await this.driverHelper.wait(polling);
                        continue;
                    }
                    Logger.error(`WorkspaceHandlingTests.obtainWorkspaceNameFromStartingPage Obtaining workspace name failed with an unexpected error:${err}`);
                    throw err;
                }
            }
            if (WorkspaceHandlingTests.workspaceName !== '' && WorkspaceHandlingTests.workspaceName !== undefined) {
                Logger.info(`Obtained workspace name from workspace loader page: ${WorkspaceHandlingTests.workspaceName}`);
                return;
            }
            Logger.error(`WorkspaceHandlingTests.obtainWorkspaceNameFromSartingPage failed to obtain workspace name:${WorkspaceHandlingTests.workspaceName}`);
            throw new error.InvalidArgumentError(`WorkspaceHandlingTests.obtainWorkspaceNameFromSartingPage failed to obtain workspace name:${WorkspaceHandlingTests.workspaceName}`);
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
