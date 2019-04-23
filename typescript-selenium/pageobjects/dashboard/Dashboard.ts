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

@injectable()
export class Dashboard {
    private readonly driverHelper: DriverHelper;

    private static readonly DASHBOARD_BUTTON_CSS: string = "#dashboard-item";
    private static readonly WORKSPACES_BUTTON_CSS: string = "#workspaces-item";
    private static readonly STACKS_BUTTON_CSS: string = "#stacks-item";
    private static readonly FACTORIES_BUTTON_CSS: string = "#factories-item";
    private static readonly LOADER_PAGE_CSS: string = ".main-page-loader"

    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper
    ) {
        this.driverHelper = driverHelper;
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
