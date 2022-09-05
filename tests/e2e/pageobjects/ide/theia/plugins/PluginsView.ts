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
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../../../inversify.types';
import { DriverHelper } from '../../../../utils/DriverHelper';
import { Logger } from '../../../../utils/Logger';
import { By, error } from 'selenium-webdriver';
import { TimeoutConstants } from '../../../../TimeoutConstants';
import { LeftToolBar } from '../LeftToolBar';
import { TopMenu } from '../TopMenu';

@injectable()
export class PluginsView {
    private static readonly SEARCH_FIELD_LOCATOR: By = By.xpath(`//div[@class='che-plugin-control-panel']//input`);
    private static readonly PLUGINS_LOADER: By = By.xpath(`//div[@id='theia-left-side-panel']//div[@class='spinnerContainer']`);

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.LeftToolBar) private readonly leftToolbar: LeftToolBar,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu) { }

    async waitTitle(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitTitle');

        await this.driverHelper.waitVisibility(By.xpath(`//div[@title='Plugins']`), timeout);
    }

    async waitPluginsLoader(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitPluginsLoader');

        await this.driverHelper.waitVisibility(PluginsView.PLUGINS_LOADER, timeout);
    }

    async waitPluginsLoaderDisappearance(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitPluginsLoaderDisappearance');

        await this.driverHelper.waitDisappearance(PluginsView.PLUGINS_LOADER, timeout);
    }

    async openView(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.openView');

        await this.openViewByTopMenu();
        await this.waitPluginsLoader(timeout);
        await this.waitPluginsLoaderDisappearance(timeout);
        await this.waitView(timeout);
    }

    async clickPluginsViewButton(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.clickPluginsViewButton');

        await this.leftToolbar.clickOnToolIcon('Plugins', timeout);
    }

    async selectPlugins(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.selectPlugins');

        await this.leftToolbar.selectView('Plugins', timeout);
    }

    async waitSearchField(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitSearchField');

        await this.driverHelper.waitVisibility(PluginsView.SEARCH_FIELD_LOCATOR, timeout);
    }

    async typeTextToSearchField(text: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.typeTextToSearchField');

        // search field performs searching during typing
        // if try to write whole word at once
        // it will cause to the random result
        for (let i: number = 0; i < text.length; i++) {
            await this.driverHelper.type(PluginsView.SEARCH_FIELD_LOCATOR, text.charAt(i), timeout);
            await this.driverHelper.wait(2000);
        }
    }

    async waitView(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitView');

        await this.waitTitle(timeout);
        await this.waitSearchField(timeout);
    }

    async waitPlugin(pluginTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitPlugin');

        const pluginLocator: By = By.xpath(`${this.getPluginBaseLocator(pluginTitle)}`);

        await this.driverHelper.waitVisibility(pluginLocator, timeout);
    }

    async waitInstallButton(pluginTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitInstallButton');

        const installButtonLocator: By = this.getInstallButtonLocator(pluginTitle);

        await this.driverHelper.waitVisibility(installButtonLocator, timeout);
    }

    async clickInstallButton(pluginTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.clickInstallButton');

        const installButtonLocator: By = this.getInstallButtonLocator(pluginTitle);

        await this.driverHelper.waitAndClick(installButtonLocator, timeout);
    }

    async waitInstalledButton(pluginTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitInstalledButton');

        const installedButtonLocator: By = this.getInstalledButtonLocator(pluginTitle);

        await this.driverHelper.waitVisibility(installedButtonLocator, timeout);
    }

    async clickInstalledButton(pluginTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.clickInstalledButton');

        const installedButtonLocator: By = this.getInstalledButtonLocator(pluginTitle);

        await this.driverHelper.waitAndClick(installedButtonLocator, timeout);
    }

    async waitPluginNotification(notificationText: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.waitPluginNotification');

        const pluginNotificationLocator: By = this.getPluginNotificationLocator(notificationText);

        await this.driverHelper.waitVisibility(pluginNotificationLocator);
    }

    async clickPluginNotification(notificationText: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('PluginsView.clickPluginNotification');

        const pluginNotificationLocator: By = this.getPluginNotificationLocator(notificationText);

        await this.driverHelper.waitAndClick(pluginNotificationLocator);
    }

    private async openViewByTopMenu() {
        await this.topMenu.clickOnTopMenuButton('View');

        try {
            await this.topMenu.clickOnSubmenuItem('Plugins');
        } catch (err) {
            if (!(err instanceof error.TimeoutError)) {
                throw err;
            }

            Logger.debug('The "View" menu is not opened, try again');

            await this.topMenu.clickOnTopMenuButton('View');
            await this.topMenu.clickOnSubmenuItem('Plugins');
        }
    }

    private getPluginNotificationLocator(notificationText: string): By {
        const pluginNotificationXpath: string = `//div[@class='che-plugins-notification']//div[@class='notification-message-text' and text()='${notificationText}']`;

        return By.xpath(pluginNotificationXpath);
    }

    private getPluginBaseLocator(pluginTitle: string): string {
        return `//div[@class='che-plugin-content']//div[@class='che-plugin-name']/span[text()='${pluginTitle}']/parent::div/parent::div/parent::div/parent::div`;
    }

    private getInstallButtonLocator(pluginTitle: string): By {
        const basePluginXpath: string = this.getPluginBaseLocator(pluginTitle);
        const relativeInstallButtonXpath: string = `//div[@class='che-plugin-action-add' and text()='Install']`;

        return By.xpath(`${basePluginXpath}${relativeInstallButtonXpath}`);
    }

    private getInstalledButtonLocator(pluginTitle: string): By {
        const basePluginXpath: string = this.getPluginBaseLocator(pluginTitle);
        const relativeInstalledButtonXpath: string = `//div[@class='che-plugin-action-remove' and text()='Installed']`;

        return By.xpath(`${basePluginXpath}${relativeInstalledButtonXpath}`);
    }

}
