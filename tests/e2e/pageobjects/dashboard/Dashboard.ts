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
import 'reflect-metadata';
import { CLASSES } from '../../inversify.types';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../TestConstants';
import { Workspaces } from './Workspaces';
import { Logger } from '../../utils/Logger';

@injectable()
export class Dashboard {
    private static readonly DASHBOARD_BUTTON_CSS: string = '#dashboard-item';
    private static readonly WORKSPACES_BUTTON_CSS: string = '#workspaces-item';
    private static readonly STACKS_BUTTON_CSS: string = '#stacks-item';
    private static readonly FACTORIES_BUTTON_CSS: string = '#factories-item';
    private static readonly GET_STARTED_BUTTON_XPATH: string = '//md-list-item//span[text()=\'Get Started\']';
    private static readonly LOADER_PAGE_CSS: string = '.main-page-loader';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces) { }

    async stopWorkspaceByUI(workspaceName: string) {
        Logger.debug(`Dashboard.stopWorkspaceByUI "${workspaceName}"`);

        await this.openDashboard();
        await this.clickWorkspacesButton();
        await this.workspaces.waitPage();
        await this.workspaces.waitWorkspaceListItem(workspaceName);
        await this.workspaces.waitWorkspaceWithRunningStatus(workspaceName);
        await this.workspaces.clickOnStopWorkspaceButton(workspaceName);
        await this.workspaces.waitWorkspaceWithStoppedStatus(workspaceName);
    }

    async deleteWorkspaceByUI(workspaceName: string) {
        Logger.debug(`Dashboard.deleteWorkspaceByUI "${workspaceName}"`);

        await this.openDashboard();
        await this.clickWorkspacesButton();
        await this.workspaces.waitPage();
        await this.workspaces.waitWorkspaceListItem(workspaceName);
        await this.workspaces.clickWorkspaceListItem(workspaceName);
        await this.workspaces.clickDeleteButtonOnWorkspaceDetails();
        await this.workspaces.confirmWorkspaceDeletion();
        await this.workspaces.waitPage();
        await this.workspaces.waitWorkspaceListItemAbcence(workspaceName);
    }

    async openDashboard() {
        Logger.debug('Dashboard.openDashboard');

        await this.driverHelper.getDriver().navigate().to(TestConstants.TS_SELENIUM_BASE_URL);
        await this.waitPage();

    }

    async waitPage(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        Logger.debug('Dashboard.waitPage');

        await this.driverHelper.waitVisibility(By.css(Dashboard.WORKSPACES_BUTTON_CSS), timeout);
        await this.driverHelper.waitVisibility(By.css(Dashboard.STACKS_BUTTON_CSS), timeout);
        await this.driverHelper.waitVisibility(By.xpath(Dashboard.GET_STARTED_BUTTON_XPATH), timeout);
    }

    async clickDashboardButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.clickDashboardButton');

        await this.driverHelper.waitAndClick(By.css(Dashboard.DASHBOARD_BUTTON_CSS), timeout);
    }

    async clickWorkspacesButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.clickWorkspacesButton');

        await this.driverHelper.waitAndClick(By.css(Dashboard.WORKSPACES_BUTTON_CSS), timeout);
    }

    async clickStacksdButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.clickStacksdButton');

        await this.driverHelper.waitAndClick(By.css(Dashboard.STACKS_BUTTON_CSS), timeout);
    }

    async clickGetStartedButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.clickGetStartedButton');

        await this.driverHelper.waitAndClick(By.xpath(Dashboard.GET_STARTED_BUTTON_XPATH), timeout);
    }

    async clickFactoriesButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.clickFactoriesButton');

        await this.driverHelper.waitAndClick(By.css(Dashboard.FACTORIES_BUTTON_CSS), timeout);
    }

    async waitLoader(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.waitLoader');

        await this.driverHelper.waitVisibility(By.css(Dashboard.LOADER_PAGE_CSS), timeout);
    }

    async waitLoaderDisappearance(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.waitLoaderDisappearance');

        await this.driverHelper.waitDisappearance(By.css(Dashboard.LOADER_PAGE_CSS), timeout);
    }

    async waitDisappearanceNavigationMenu(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Dashboard.waitDisappearanceNavigationMenu');

        await this.driverHelper.waitDisappearance(By.id('chenavmenu'), timeout);
    }

}
