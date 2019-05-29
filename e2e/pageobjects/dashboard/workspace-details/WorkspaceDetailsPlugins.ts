/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { DriverHelper } from '../../../utils/DriverHelper';
import { injectable, inject } from 'inversify';
import 'reflect-metadata';
import { CLASSES } from '../../../inversify.types';
import { TestConstants } from '../../../TestConstants';
import { By } from 'selenium-webdriver';
import { WorkspaceDetails } from './WorkspaceDetails';


@injectable()
export class WorkspaceDetailsPlugins {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.WorkspaceDetails) private readonly workspaceDetails: WorkspaceDetails) { }

    async waitPluginListItem(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const pluginListItemLocator: By = By.css(this.getPluginListItemCssLocator(pluginName));

        await this.driverHelper.waitVisibility(pluginListItemLocator, timeout);
    }

    async enablePlugin(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitPluginDisabling(pluginName, timeout);
        await this.clickOnPluginListItemSwitcher(pluginName, timeout);
        await this.waitPluginEnabling(pluginName, timeout);
    }

    async disablePlugin(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitPluginEnabling(pluginName, timeout);
        await this.clickOnPluginListItemSwitcher(pluginName, timeout);
        await this.waitPluginDisabling(pluginName, timeout);
    }

    async addPluginAndOpenWorkspace(namespace: string, workspaceName: string, pluginName: string, pluginId: string) {
        await this.workspaceDetails.selectTab('Plugins');
        await this.enablePlugin(pluginName);
        await this.workspaceDetails.saveChanges();
        await this.workspaceDetails.openWorkspace(namespace, workspaceName);
    }

    private getPluginListItemCssLocator(pluginName: string): string {
        return `.plugin-item div[plugin-item-name='${pluginName}']`;
    }

    private getPluginListItemSwitcherCssLocator(pluginName: string): string {
        return `${this.getPluginListItemCssLocator(pluginName)} md-switch`;
    }

    private async clickOnPluginListItemSwitcher(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const pluginListItemSwitcherLocator = By.css(this.getPluginListItemSwitcherCssLocator(pluginName));

        await this.driverHelper.waitAndClick(pluginListItemSwitcherLocator, timeout);
    }

    private async waitPluginEnabling(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const enabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName)} md-switch[aria-checked='true']`);

        await this.driverHelper.waitVisibility(enabledPluginSwitcherLocator, timeout);
    }

    private async waitPluginDisabling(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const disabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName)} md-switch[aria-checked='false']`);

        await this.driverHelper.waitVisibility(disabledPluginSwitcherLocator, timeout);
    }

}
