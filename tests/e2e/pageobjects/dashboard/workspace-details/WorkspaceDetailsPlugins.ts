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
import { CLASSES, TYPES } from '../../../inversify.types';
import { By } from 'selenium-webdriver';
import { WorkspaceDetails } from './WorkspaceDetails';
import { ITestWorkspaceUtil } from '../../../utils/workspace/ITestWorkspaceUtil';
import { WorkspaceStatus } from '../../../utils/workspace/WorkspaceStatus';
import { Logger } from '../../../utils/Logger';
import { TimeoutConstants } from '../../../TimeoutConstants';


@injectable()
export class WorkspaceDetailsPlugins {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.WorkspaceDetails) private readonly workspaceDetails: WorkspaceDetails,
        @inject(TYPES.WorkspaceUtil) private readonly testWorkspaceUtil: ITestWorkspaceUtil) { }

    async waitPluginListItem(pluginName: string) {
        Logger.debug(`WorkspaceDetailsPlugins.waitPluginListItem ${pluginName}`);

        const pluginListItemLocator: By = By.css(this.getPluginListItemCssLocator(pluginName));

        await this.driverHelper.waitVisibility(pluginListItemLocator, TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT);
    }

    async enablePlugin(pluginName: string, pluginVersion?: string) {
        Logger.debug(`WorkspaceDetailsPlugins.enablePlugin ${pluginName}:${pluginVersion}`);

        await this.waitPluginDisabling(pluginName, pluginVersion);
        await this.clickOnPluginListItemSwitcher(pluginName, pluginVersion);
        await this.waitPluginEnabling(pluginName, pluginVersion);
    }

    async disablePlugin(pluginName: string, pluginVersion?: string) {
        Logger.debug(`WorkspaceDetailsPlugins.disablePlugin ${pluginName}:${pluginVersion}`);

        await this.waitPluginEnabling(pluginName, pluginVersion);
        await this.clickOnPluginListItemSwitcher(pluginName, pluginVersion);
        await this.waitPluginDisabling(pluginName, pluginVersion);
    }

    async addPluginAndOpenWorkspace(namespace: string, workspaceName: string, pluginName: string, pluginId: string, pluginVersion?: string) {
        Logger.debug(`WorkspaceDetailsPlugins.addPluginAndOpenWorkspace ${namespace}/${workspaceName} plugin: ${pluginName}:${pluginVersion}`);

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
        timeout: number = TimeoutConstants.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) {

        const pluginListItemSwitcherLocator = By.css(this.getPluginListItemSwitcherCssLocator(pluginName, pluginVersion));

        await this.driverHelper.waitAndClick(pluginListItemSwitcherLocator, timeout);
    }

    private async waitPluginEnabling(pluginName: string, pluginVersion?: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        const enabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch[aria-checked='true']`);

        await this.driverHelper.waitVisibility(enabledPluginSwitcherLocator, timeout);
    }

    private async waitPluginDisabling(pluginName: string, pluginVersion?: string, timeout: number = TimeoutConstants.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) {
        const disabledPluginSwitcherLocator: By = By.css(`${this.getPluginListItemCssLocator(pluginName, pluginVersion)} md-switch[aria-checked='false']`);

        await this.driverHelper.waitVisibility(disabledPluginSwitcherLocator, timeout);
    }

}
