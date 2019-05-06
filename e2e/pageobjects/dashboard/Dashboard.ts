/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from "inversify";
import "reflect-metadata";
import { TYPES, CLASSES } from "../../inversify.types";
import { Driver } from "../../driver/Driver";
import { WebElementCondition, By } from "selenium-webdriver";
import { DriverHelper } from "../../utils/DriverHelper";
import { TestConstants } from "../../TestConstants";
import { Workspaces } from "./Workspaces";

@injectable()
export class Dashboard {
    private static readonly DASHBOARD_BUTTON_CSS: string = "#dashboard-item";
    private static readonly WORKSPACES_BUTTON_CSS: string = "#workspaces-item";
    private static readonly STACKS_BUTTON_CSS: string = "#stacks-item";
    private static readonly FACTORIES_BUTTON_CSS: string = "#factories-item";
    private static readonly LOADER_PAGE_CSS: string = ".main-page-loader"

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces) { }

    async stopWorkspaceByUI(workspaceName: string) {
        await this.openDashboard()
        await this.clickWorkspacesButton()
        await this.workspaces.waitPage()
        await this.workspaces.waitWorkspaceListItem(workspaceName)
        await this.workspaces.waitWorkspaceWithRunningStatus(workspaceName)
        await this.workspaces.clickOnStopWorkspaceButton(workspaceName)
        await this.workspaces.waitWorkspaceWithStoppedStatus(workspaceName)
    }

    async deleteWorkspaceByUI(workspaceName: string) {
        await this.openDashboard()
        await this.clickWorkspacesButton()
        await this.workspaces.waitPage()
        await this.workspaces.waitWorkspaceListItem(workspaceName)
        await this.workspaces.clickWorkspaceListItem(workspaceName);
        await this.workspaces.clickDeleteButtonOnWorkspaceDetails();
        await this.workspaces.clickConfirmDeletionButton();
        await this.workspaces.waitPage()
        await this.workspaces.waitWorkspaceListItemAbcence(workspaceName);
    }

    async openDashboard(timeout = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.navigateTo(TestConstants.TS_SELENIUM_BASE_URL)
        await this.waitPage(timeout)
    }

    async waitPage(timeout = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Dashboard.DASHBOARD_BUTTON_CSS), timeout)
        await this.driverHelper.waitVisibility(By.css(Dashboard.WORKSPACES_BUTTON_CSS), timeout)
        await this.driverHelper.waitVisibility(By.css(Dashboard.STACKS_BUTTON_CSS), timeout)
        await this.driverHelper.waitVisibility(By.css(Dashboard.FACTORIES_BUTTON_CSS), timeout)
    }

    async clickDashboardButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(Dashboard.DASHBOARD_BUTTON_CSS), timeout)
    }

    async clickWorkspacesButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(Dashboard.WORKSPACES_BUTTON_CSS), timeout)
    }

    async clickStacksdButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(Dashboard.STACKS_BUTTON_CSS), timeout)
    }

    async clickFactoriesButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(Dashboard.FACTORIES_BUTTON_CSS), timeout)
    }

    async waitLoader(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Dashboard.LOADER_PAGE_CSS), timeout)
    }

    async waitLoaderDisappearance(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitDisappearance(By.css(Dashboard.LOADER_PAGE_CSS), timeout)
    }

}
