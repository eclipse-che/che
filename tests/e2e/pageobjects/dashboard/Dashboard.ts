/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../constants/TestConstants';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { Workspaces } from './Workspaces';
import { Logger } from '../../utils/Logger';

@injectable()
export class Dashboard {
    private static readonly WORKSPACES_BUTTON_XPATH: string = `//div[@id='page-sidebar']//a[contains(text(), 'Workspaces (')]`;
    private static readonly CREATE_WORKSPACE_BUTTON_XPATH: string = `//div[@id='page-sidebar']//a[text()='Create Workspace']`;
    private static readonly LOADER_PAGE_STEP_TITLES_XPATH: string = '//*[@data-testid="step-title"]';
    private static readonly STARTING_PAGE_LOADER_CSS: string = '.main-page-loader';
    private static readonly LOADER_ALERT_XPATH: string = '//*[@data-testid="loader-alert"]';
    private static readonly LOGOUT_BUTTON_XPATH: string = '//button[text()="Logout"]';

    private static getUserDropdownMenuButtonLocator(): By {
        Logger.debug(`Dashboard.getUserDropdownMenuButtonLocator: get current user.`);

        const currentUser: string = TestConstants.TS_SELENIUM_OCP_USERNAME;
        Logger.debug(`Dashboard.getUserDropdownMenuButtonLocator: ${currentUser}.`);

        return By.xpath(`//*[text()="${currentUser}"]//parent::button`);

    }

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
                @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces) {
    }

    async stopWorkspaceByUI(workspaceName: string): Promise<void> {
        Logger.debug(`Dashboard.stopWorkspaceByUI "${workspaceName}"`);

        await this.clickWorkspacesButton();
        await this.workspaces.waitPage();
        await this.workspaces.waitWorkspaceListItem(workspaceName);
        await this.workspaces.waitWorkspaceWithRunningStatus(workspaceName);

        await this.workspaces.stopWorkspaceByActionsButton(workspaceName);
        await this.workspaces.waitWorkspaceWithStoppedStatus(workspaceName);
    }

    async deleteStoppedWorkspaceByUI(workspaceName: string): Promise<void> {
        Logger.debug(`Dashboard.deleteStoppedWorkspaceByUI "${workspaceName}"`);

        await this.clickWorkspacesButton();
        await this.workspaces.waitPage();
        await this.workspaces.waitWorkspaceListItem(workspaceName);
        await this.workspaces.deleteWorkspaceByActionsButton(workspaceName);
        await this.workspaces.waitPage();
        await this.workspaces.waitWorkspaceListItemAbsence(workspaceName);
    }

    async stopAndRemoveWorkspaceByUI(workspaceName: string): Promise<void> {
        Logger.debug(`Dashboard.stopAndRemoveWorkspaceByUI "${workspaceName}"`);

        await this.stopWorkspaceByUI(workspaceName);
        await this.workspaces.deleteWorkspaceByActionsButton(workspaceName);
        await this.workspaces.waitWorkspaceListItemAbsence(workspaceName);
    }

    async openDashboard(): Promise<void> {
        Logger.debug('Dashboard.openDashboard');
        await this.driverHelper.getDriver().navigate().to(TestConstants.TS_SELENIUM_BASE_URL);
        await this.waitPage();

    }

    async waitPage(timeout: number = TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT): Promise<void> {
        Logger.debug('Dashboard.waitPage');

        await this.driverHelper.waitVisibility(By.xpath(Dashboard.WORKSPACES_BUTTON_XPATH), timeout);
        await this.driverHelper.waitVisibility(By.xpath(Dashboard.CREATE_WORKSPACE_BUTTON_XPATH), timeout);
    }

    async clickWorkspacesButton(timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
        Logger.debug('Dashboard.clickWorkspacesButton');

        await this.driverHelper.waitAndClick(By.xpath(Dashboard.WORKSPACES_BUTTON_XPATH), timeout);
    }

    async clickCreateWorkspaceButton(timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT): Promise<void> {
        Logger.debug('Dashboard.clickCreateWorkspaceButton');

        await this.driverHelper.waitAndClick(By.xpath(Dashboard.CREATE_WORKSPACE_BUTTON_XPATH), timeout);
    }

    async getLoaderAlert(timeout: number = TimeoutConstants.TS_WAIT_LOADER_PRESENCE_TIMEOUT): Promise<string> {
        Logger.debug('Dashboard.getLoaderAlert');

        return await this.driverHelper.waitAndGetText(By.xpath(Dashboard.LOADER_ALERT_XPATH), timeout);
    }

    async waitLoader(timeout: number = TimeoutConstants.TS_WAIT_LOADER_PRESENCE_TIMEOUT): Promise<void> {
        Logger.debug('Dashboard.waitLoader');

        await this.driverHelper.waitAllPresence(By.xpath(Dashboard.LOADER_PAGE_STEP_TITLES_XPATH), timeout);
    }

    async waitLoaderDisappearance(timeout: number = TimeoutConstants.TS_WAIT_LOADER_ABSENCE_TIMEOUT): Promise<void> {
        Logger.debug('Dashboard.waitLoaderDisappearance');

        await this.driverHelper.waitDisappearance(By.xpath(Dashboard.LOADER_PAGE_STEP_TITLES_XPATH), timeout);
    }

    async waitDisappearanceNavigationMenu(timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
        Logger.debug('Dashboard.waitDisappearanceNavigationMenu');

        await this.driverHelper.waitDisappearance(By.id('chenavmenu'), timeout);
    }

    async waitStartingPageLoaderDisappearance(timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
        Logger.debug(`Dashboard.waitStartingPageLoaderDisappearance`);

        await this.driverHelper.waitDisappearance(By.css(Dashboard.STARTING_PAGE_LOADER_CSS), timeout);
        await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
    }

    async getRecentWorkspaceName(timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<string> {
        Logger.debug(`Dashboard.getRecentWorkspaceName`);

        return await this.driverHelper.waitAndGetText(By.css('[data-testid="recent-workspace-item"]'), timeout);
    }

    async logout(timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT): Promise<void> {
        Logger.debug(`Dashboard.logout`);

        await this.openDashboard();
        await this.driverHelper.waitAndClick(Dashboard.getUserDropdownMenuButtonLocator(), timeout);
        await this.driverHelper.waitAndClick(By.xpath(Dashboard.LOGOUT_BUTTON_XPATH), timeout);
        await this.driverHelper.waitDisappearance(Dashboard.getUserDropdownMenuButtonLocator(), timeout);
    }
}
