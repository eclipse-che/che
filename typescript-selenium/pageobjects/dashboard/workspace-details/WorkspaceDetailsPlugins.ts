/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { DriverHelper } from "../../../utils/DriverHelper";
import { injectable, inject } from "inversify";
import 'reflect-metadata';
import { CLASSES } from "../../../types";
import { TestConstants } from "../../../TestConstants";
import { By } from "selenium-webdriver";


@injectable()
export class WorkspaceDetailsPlugins {
    private readonly driverHelper: DriverHelper;

    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper
    ) {
        this.driverHelper = driverHelper;
    }

    private getPluginListItemCssLocator(pluginName: string): string {
        return `.plugin-item div[plugin-item-name='${pluginName}']`
    }

    private getPluginListItemSwitcherCssLocator(pluginName: string): string {
        return `${this.getPluginListItemCssLocator(pluginName)} md-switch`
    }

    async waitPluginListItem(pluginName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const pluginListItemLocator: By = By.css(this.getPluginListItemCssLocator(pluginName))

        await this.driverHelper.waitVisibility(pluginListItemLocator, timeout)
    }

    async clickOnPluginListItemSwitcher(pluginName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const pluginListItemSwitcherLocator = By.css(this.getPluginListItemSwitcherCssLocator(pluginName))

        await this.driverHelper.waitAndClick(pluginListItemSwitcherLocator, timeout)
    }

    async waitPluginEnabling(pluginName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const enabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName)} md-switch[aria-checked='true']`)

        await this.driverHelper.waitVisibility(enabledPluginSwitcherLocator, timeout)
    }

    async waitPluginDisabling(pluginName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const disabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName)} md-switch[aria-checked='false']`)

        await this.driverHelper.waitVisibility(disabledPluginSwitcherLocator, timeout)
    }

}
