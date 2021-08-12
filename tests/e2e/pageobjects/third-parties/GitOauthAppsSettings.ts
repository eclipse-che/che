/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Logger } from '../../utils/Logger';
import { By, error, Key } from 'selenium-webdriver';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

@injectable()
export class GitOauthAppsSettings {
    private static readonly GITHUB_OAUTH_APPS_SETTINGS_URL = 'https://github.com/settings/developers';
    private static readonly HOME_PAGE_FIELD_LOCATOR: By = By.xpath(`//input[@id='oauth_application_url']`);
    private static readonly CALLBACK_URL_FIELD_LOCATOR: By = By.xpath(`//input[@id='oauth_application_callback_url']`);
    private static readonly UPDATE_APPLICATION_BUTTON_LOCATOR: By = By.xpath(`//form[@class='edit_oauth_application']//button[@type='submit']`);

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil) { }

    async openPage(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.openPage');

        await this.browserTabsUtil.navigateTo(GitOauthAppsSettings.GITHUB_OAUTH_APPS_SETTINGS_URL);
    }

    async waitTitle(title: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.waitTitle');

        await this.driverHelper.waitVisibility(this.getTitleLocator(title), timeout);
    }

    async waitOauthApp(applicationTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.waitOauthApp');

        await this.driverHelper.waitVisibility(this.getOauthAppLocator(applicationTitle), timeout);
    }

    async clickOauthApp(applicationTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.clickOauthApp');

        await this.driverHelper.waitAndClick(this.getOauthAppLocator(applicationTitle), timeout);
    }

    async openOauthApp(title: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.openOauthApp');

        await this.driverHelper.waitUntilTrue(async () => {
            await this.clickOauthApp(title);

            try {
                await this.waitTitle(title, timeout / 3);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                Logger.debug(`The ${title} oAuth app is not opened, next try.`);
            }

        }, timeout);
    }

    async scrollToUpdateApplicationButton(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.scrollToUpdateApplicationButton');

        await this.driverHelper.scrollTo(GitOauthAppsSettings.UPDATE_APPLICATION_BUTTON_LOCATOR, timeout);
    }

    async typeHomePageUrl(homePageUrl: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.typeHomePageUrl');

        await this.driverHelper.type(GitOauthAppsSettings.HOME_PAGE_FIELD_LOCATOR, Key.chord(Key.CONTROL, 'a'), timeout);
        await this.driverHelper.type(GitOauthAppsSettings.HOME_PAGE_FIELD_LOCATOR, Key.DELETE, timeout);
        await this.driverHelper.type(GitOauthAppsSettings.HOME_PAGE_FIELD_LOCATOR, homePageUrl, timeout);
    }

    async typeCallbackUrl(callbackUrl: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.typeCallbackUrl');

        await this.driverHelper.type(GitOauthAppsSettings.CALLBACK_URL_FIELD_LOCATOR, Key.chord(Key.CONTROL, 'a'), timeout);
        await this.driverHelper.type(GitOauthAppsSettings.CALLBACK_URL_FIELD_LOCATOR, Key.DELETE, timeout);
        await this.driverHelper.type(GitOauthAppsSettings.CALLBACK_URL_FIELD_LOCATOR, callbackUrl, timeout);
    }

    async clickUpdateApplicationButton(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug('GitOauthAppsSettings.clickUpdateApplicationButton');

        await this.driverHelper.waitAndClick(GitOauthAppsSettings.UPDATE_APPLICATION_BUTTON_LOCATOR, timeout);
    }

    private getOauthAppLocator(applicationTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        return By.xpath(`//div[@class='TableObject']//a[text()='${applicationTitle}']`);
    }

    private getTitleLocator(title: string): By {
        return By.xpath(`//main[@id='js-pjax-container']//h2[text()='${title}']`);
    }

}
