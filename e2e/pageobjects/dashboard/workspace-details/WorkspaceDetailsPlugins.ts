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
import { TestWorkspaceUtil, WorkspaceStatus } from '../../../utils/workspace/TestWorkspaceUtil';


@injectable()
export class WorkspaceDetailsPlugins {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.WorkspaceDetails) private readonly workspaceDetails: WorkspaceDetails,
        @inject(CLASSES.TestWorkspaceUtil) private readonly testWorkspaceUtil: TestWorkspaceUtil) { }

    async waitPluginListItem(pluginName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const pluginListItemLocator: By = By.css(this.getPluginListItemCssLocator(pluginName));

        await this.driverHelper.waitVisibility(pluginListItemLocator, timeout);
    }

    async enablePlugin(pluginName: string, pluginVersion?: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitPluginDisabling(pluginName, pluginVersion, timeout);
        await this.clickOnPluginListItemSwitcher(pluginName, pluginVersion, timeout);
        await this.waitPluginEnabling(pluginName, pluginVersion, timeout);
    }

    async disablePlugin(pluginName: string, pluginVersion?: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitPluginEnabling(pluginName, pluginVersion, timeout);
        await this.clickOnPluginListItemSwitcher(pluginName, pluginVersion, timeout);
        await this.waitPluginDisabling(pluginName, pluginVersion, timeout);
    }

    async addPluginAndOpenWorkspace(namespace: string, workspaceName: string, pluginName: string, pluginId: string, pluginVersion?: string) {
        await this.workspaceDetails.selectTab('Plugins');
        await this.enablePlugin(pluginName, pluginVersion);
        await this.workspaceDetails.saveChanges();
        await this.workspaceDetails.openWorkspace(namespace, workspaceName);
        await this.testWorkspaceUtil.waitWorkspaceStatus(namespace, workspaceName, WorkspaceStatus.RUNNING);
        await this.testWorkspaceUtil.waitPluginAdding(namespace, workspaceName, pluginId);
    }

    private getPluginListItemCssLocator(pluginName: string, pluginVersion?: string): string {
        if (pluginVersion) {
            return `.plugin-item div[plugin-item-name*='${pluginName}'][plugin-item-version='${pluginVersion}']`;
        }

        return `.plugin-item div[plugin-item-name*='${pluginName}']`;
    }

    private getPluginListItemSwitcherCssLocator(pluginName: string, pluginVersion?: string): string {
        return `${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch`;
    }

    private async clickOnPluginListItemSwitcher(pluginName: string,
        pluginVersion?: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        const pluginListItemSwitcherLocator = By.css(this.getPluginListItemSwitcherCssLocator(pluginName, pluginVersion));

        await this.driverHelper.waitAndClick(pluginListItemSwitcherLocator, timeout);
    }

    private async waitPluginEnabling(pluginName: string, pluginVersion?: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const enabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch[aria-checked='true']`);

        await this.driverHelper.waitVisibility(enabledPluginSwitcherLocator, timeout);
    }

    private async waitPluginDisabling(pluginName: string, pluginVersion?: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const disabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch[aria-checked='false']`);

        await this.driverHelper.waitVisibility(disabledPluginSwitcherLocator, timeout);
    }

}
