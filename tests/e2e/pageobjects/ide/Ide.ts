/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import axios from 'axios';
import { DriverHelper } from '../../utils/DriverHelper';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By, error } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { NotificationCenter } from './NotificationCenter';

export enum RightToolbarButton {
    Explorer = 'Explorer',
    Git = 'Git',
    Debug = 'Debug'
}

@injectable()
export class Ide {
    public static readonly EXPLORER_BUTTON_ID: string = 'shell-tab-explorer-view-container';
    public static readonly SELECTED_EXPLORER_BUTTON_CSS: string = 'li#shell-tab-explorer-view-container.theia-mod-active';
    public static readonly ACTIVATED_IDE_IFRAME_CSS: string = '#ide-iframe-window[aria-hidden=\'false\']';
    public static readonly SELECTED_GIT_BUTTON_XPATH: string = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Git\' and contains(@class, \'p-mod-current\')])[1]';
    private static readonly TOP_MENU_PANEL_CSS: string = '#theia-app-shell #theia-top-panel .p-MenuBar-content';
    private static readonly LEFT_CONTENT_PANEL_CSS: string = '#theia-left-content-panel';
    private static readonly PRELOADER_CSS: string = '.theia-preload';
    private static readonly IDE_IFRAME_CSS: string = 'iframe#ide-application-iframe';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.NotificationCenter) private readonly notificationCenter: NotificationCenter
    ) { }

    async waitAndSwitchToIdeFrame(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        Logger.debug('Ide.waitAndSwitchToIdeFrame');
        try {
            await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout);
        } catch (err) {
            if (err instanceof error.StaleElementReferenceError) {
                Logger.warn('StaleElementException occured during waiting for IDE. Sleeping for 2 secs and retrying.');
                this.driverHelper.wait(2000);
                await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout);
            }
        }
    }

    async waitNotification(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Ide.waitNotification "${notificationText}"`);

        const notificationLocator: By = By.xpath(this.getNotificationXpathLocator(notificationText));
        await this.driverHelper.waitVisibility(notificationLocator, timeout);
    }

    async waitNotificationAndClickOnButton(notificationText: string,
        buttonText: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        Logger.debug(`Ide.waitNotificationAndClickOnButton "${notificationText}" buttonText: "${buttonText}"`);

        await this.driverHelper.getDriver().wait(async () => {
            await this.waitNotification(notificationText, timeout);
            await this.clickOnNotificationButton(notificationText, buttonText);

            try {
                await this.waitNotificationDisappearance(notificationText);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                console.log(`After clicking on "${buttonText}" button of the notification with text "${notificationText}" \n` +
                    'it is still visible (issue #14121), try again.');

                await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
            }
        }, timeout);
    }

    async waitNotificationAndConfirm(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Ide.waitNotificationAndConfirm "${notificationText}"`);

        await this.waitNotificationAndClickOnButton(notificationText, 'yes', timeout);
    }

    async waitNotificationAndOpenLink(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Ide.waitNotificationAndOpenLink "${notificationText}"`);

        await this.waitNotification(notificationText, timeout);
        await this.waitApllicationIsReady(await this.getApplicationUrlFromNotification(notificationText), timeout);
        await this.waitNotificationAndClickOnButton(notificationText, 'Open Link', timeout);
    }

    async isNotificationPresent(notificationText: string): Promise<boolean> {
        Logger.debug(`Ide.isNotificationPresent "${notificationText}"`);

        const notificationLocator: By = By.xpath(this.getNotificationXpathLocator(notificationText));
        return await this.driverHelper.waitVisibilityBoolean(notificationLocator);
    }

    async waitNotificationDisappearance(notificationText: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.debug(`Ide.waitNotificationDisappearance "${notificationText}"`);

        const notificationLocator: By = By.xpath(this.getNotificationXpathLocator(notificationText));
        await this.driverHelper.waitDisappearance(notificationLocator, attempts, polling);
    }

    async clickOnNotificationButton(notificationText: string, buttonText: string) {
        Logger.debug(`Ide.clickOnNotificationButton "${notificationText}" buttonText: "${buttonText}"`);

        const yesButtonLocator: string = `//div[@class='theia-notification-list']//span[contains(.,'${notificationText}')]/parent::div/parent::div/parent::div/div[@class='theia-notification-list-item-content-bottom']//div[@class='theia-notification-buttons']//button[text()='${buttonText}'] `;
        await this.driverHelper.waitAndClick(By.xpath(yesButtonLocator));
    }

    async waitWorkspaceAndIde(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {

        Logger.debug('Ide.waitWorkspaceAndIde');

        await this.waitAndSwitchToIdeFrame(timeout);
        await this.waitIde(timeout);
    }

    async waitIde(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        Logger.debug('Ide.waitIde');

        const mainIdeParts: Array<By> = [By.css(Ide.TOP_MENU_PANEL_CSS), By.css(Ide.LEFT_CONTENT_PANEL_CSS), By.id(Ide.EXPLORER_BUTTON_ID)];

        for (const idePartLocator of mainIdeParts) {
            await this.driverHelper.waitVisibility(idePartLocator, timeout);
        }
    }

    async waitRightToolbarButton(buttonTitle: RightToolbarButton, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Ide.waitRightToolbarButton');

        const buttonLocator: By = this.getRightToolbarButtonLocator(buttonTitle);
        await this.driverHelper.waitVisibility(buttonLocator, timeout);
    }

    async waitAndClickRightToolbarButton(buttonTitle: RightToolbarButton, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Ide.waitAndClickRightToolbarButton');

        const buttonLocator: By = this.getRightToolbarButtonLocator(buttonTitle);
        await this.driverHelper.waitAndClick(buttonLocator, timeout);
    }

    async waitTopMenuPanel(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Ide.waitTopMenuPanel');

        await this.driverHelper.waitVisibility(By.css(Ide.TOP_MENU_PANEL_CSS), timeout);
    }

    async waitLeftContentPanel(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Ide.waitLeftContentPanel');

        await this.driverHelper.waitVisibility(By.css(Ide.LEFT_CONTENT_PANEL_CSS));
    }

    async waitPreloaderAbsent(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = timeout / polling;
        Logger.debug('Ide.waitPreloaderAbsent');

        await this.driverHelper.waitDisappearance(By.css(Ide.PRELOADER_CSS), attempts, polling);
    }

    async waitPreloaderVisible(timeout: number = TestConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT) {
        Logger.debug('Ide.waitPreloaderVisible');

        await this.driverHelper.waitVisibility(By.css(Ide.PRELOADER_CSS), timeout);
    }

    async waitStatusBarContains(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) {
        const statusBarLocator: By = By.css('div[id=\'theia-statusBar\']');

        Logger.debug(`Ide.waitStatusBarContains "${expectedText}"`);

        await this.driverHelper.getDriver().wait(async () => {
            const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout);
            const isTextPresent: boolean = elementText.search(expectedText) > 0;

            if (isTextPresent) {
                return true;
            }

            await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING * 2);

        }, timeout);
    }

    async waitStatusBarTextAbsence(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const statusBarLocator: By = By.css('div[id=\'theia-statusBar\']');

        Logger.debug(`Ide.waitStatusBarTextAbsence "${expectedText}"`);

        // for ensuring that check is not invoked in the gap of status displaying
        for (let i: number = 0; i < 3; i++) {
            await this.driverHelper.getDriver().wait(async () => {
                const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout);

                const isTextAbsent: boolean = elementText.search(expectedText) === -1;

                if (isTextAbsent) {
                    return true;
                }

                await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING * 2);

            }, timeout);
        }
    }

    async checkLsInitializationStart(expectedTextInStatusBar: string) {
        Logger.debug('Ide.checkLsInitializationStart');

        await this.waitStatusBarContains(expectedTextInStatusBar, 20000);
    }

    async performKeyCombination(keyCombination: string) {
        Logger.debug(`Ide.performKeyCombination "${keyCombination}"`);

        const bodyLocator: By = By.tagName('body');
        await this.driverHelper.type(bodyLocator, keyCombination);
    }

    async waitRightToolbarButtonSelection(buttonTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('Ide.waitRightToolbarButtonSelection');

        const selectedRightToolbarButtonLocator: By = this.getSelectedRightToolbarButtonLocator(buttonTitle);
        await this.driverHelper.waitVisibility(selectedRightToolbarButtonLocator, timeout);
    }

    async getApplicationUrlFromNotification(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Ide.getApplicationUrlFromNotification ${notificationText}`);

        const notificationTextLocator: By = By.xpath(`//div[@class='theia-notification-message']/span[contains(.,'${notificationText}')]`);
        let notification = await this.driverHelper.waitAndGetText(notificationTextLocator, timeout);
        let regexp: RegExp = new RegExp('^.*(https?://.*)$');

        if (!regexp.test(notification)) {
            throw new Error('Cannot obtaine url from notification message');
        }

        return notification.split(regexp)[1];
    }

    async closeAllNotifications(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`Ide.closeAllNotifications`);

        await this.notificationCenter.open(timeout);
        await this.notificationCenter.closeAll(timeout);
    }

    async waitApllicationIsReady(url: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        Logger.debug(`Ide.waitApllicationIsReady ${url}`);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                const res = await axios.get(url);
                if (res.status === 200) {
                    return true;
                }
            } catch (error) {
                await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
            }

        }, timeout);
    }

    private getSelectedRightToolbarButtonLocator(buttonTitle: string): By {
        return By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title[contains(.,'${buttonTitle}')] and contains(@id, 'shell-tab')] and contains(@class, 'p-mod-current')`);
    }

    private getRightToolbarButtonLocator(buttonTitle: String): By {
        return By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title[contains(.,'${buttonTitle}')] and contains(@id, 'shell-tab')]`);
    }

    private getNotificationXpathLocator(notificationText: string): string {
        return `//div[@class='theia-notification-message']/span[contains(.,'${notificationText}')]`;
    }

}
