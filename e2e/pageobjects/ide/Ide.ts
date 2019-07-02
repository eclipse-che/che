/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { DriverHelper } from '../../utils/DriverHelper';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By, WebElement, error } from 'selenium-webdriver';
import { TestWorkspaceUtil, WorkspaceStatus } from '../../utils/workspace/TestWorkspaceUtil';

export enum RightToolbarButton {
    Explorer = 'Explorer',
    Git = 'Git',
    Debug = 'Debug'
}

@injectable()
export class Ide {
    public static readonly EXPLORER_BUTTON_XPATH: string = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Explorer\'])[1]';
    public static readonly SELECTED_EXPLORER_BUTTON_XPATH: string = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Explorer\' and contains(@class, \'p-mod-current\')])[1]';
    public static readonly ACTIVATED_IDE_IFRAME_CSS: string = '#ide-iframe-window[aria-hidden=\'false\']';
    public static readonly SELECTED_GIT_BUTTON_XPATH: string = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Git\' and contains(@class, \'p-mod-current\')])[1]';
    private static readonly TOP_MENU_PANEL_CSS: string = '#theia-app-shell #theia-top-panel .p-MenuBar-content';
    private static readonly LEFT_CONTENT_PANEL_CSS: string = '#theia-left-content-panel';
    private static readonly PRELOADER_CSS: string = '.theia-preload';
    private static readonly IDE_IFRAME_CSS: string = 'iframe#ide-application-iframe';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.TestWorkspaceUtil) private readonly testWorkspaceUtil: TestWorkspaceUtil) { }

    async waitAndSwitchToIdeFrame(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout);
    }

    async waitNotification(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const notificationLocator: By = By.xpath(this.getNotificationXpathLocator(notificationText));

        await this.driverHelper.waitVisibility(notificationLocator, timeout);
    }

    async waitNotificationAndConfirm(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitNotification(notificationText, timeout);
        await this.clickOnNotificationButton(notificationText, 'yes');
    }

    async waitNotificationAndOpenLink(notificationText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitNotification(notificationText, timeout);
        await this.clickOnNotificationButton(notificationText, 'Open Link');
    }

    async isNotificationPresent(notificationText: string): Promise<boolean> {
        const notificationLocator: By = By.xpath(this.getNotificationXpathLocator(notificationText));

        return await this.driverHelper.waitVisibilityBoolean(notificationLocator);
    }

    async waitNotificationDisappearance(notificationText: string,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        const notificationLocator: By = By.xpath(this.getNotificationXpathLocator(notificationText));

        await this.driverHelper.waitDisappearance(notificationLocator, attempts, polling);
    }

    async clickOnNotificationButton(notificationText: string, buttonText: string) {
        const notificationLocator: string = this.getNotificationXpathLocator(notificationText);
        const yesButtonLocator: string = notificationLocator + `//button[text()=\'${buttonText}\']`;

        await this.driverHelper.waitAndClick(By.xpath(yesButtonLocator));
    }

    async waitWorkspaceAndIde(workspaceNamespace: string,
        workspaceName: string,
        timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {

        await this.waitAndSwitchToIdeFrame(timeout);
        await this.testWorkspaceUtil.waitWorkspaceStatus(workspaceNamespace, workspaceName, WorkspaceStatus.RUNNING);
        await this.waitIde(timeout);
    }

    async waitIde(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        const mainIdeParts: Array<By> = [By.css(Ide.TOP_MENU_PANEL_CSS), By.css(Ide.LEFT_CONTENT_PANEL_CSS), By.xpath(Ide.EXPLORER_BUTTON_XPATH)];

        for (const idePartLocator of mainIdeParts) {
            await this.driverHelper.waitVisibility(idePartLocator, timeout);
        }
    }

    async waitRightToolbarButton(buttonTitle: RightToolbarButton, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const buttonLocator: By = this.getRightToolbarButtonLocator(buttonTitle);

        await this.driverHelper.waitVisibility(buttonLocator, timeout);
    }

    async waitAndClickRightToolbarButton(buttonTitle: RightToolbarButton, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const buttonLocator: By = this.getRightToolbarButtonLocator(buttonTitle);

        await this.driverHelper.waitAndClick(buttonLocator, timeout);
    }

    async waitTopMenuPanel(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Ide.TOP_MENU_PANEL_CSS), timeout);
    }

    async waitLeftContentPanel(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Ide.LEFT_CONTENT_PANEL_CSS));
    }

    async waitPreloaderAbsent(attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        await this.driverHelper.waitDisappearance(By.css(Ide.PRELOADER_CSS), attempts, polling);
    }

    async waitStatusBarContains(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) {
        const statusBarLocator: By = By.css('div[id=\'theia-statusBar\']');

        await this.driverHelper.getDriver().wait(async () => {
            const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout);
            const isTextPresent: boolean = elementText.search(expectedText) > 0;

            if (isTextPresent) {
                return true;
            }

        }, timeout);
    }

    async waitStatusBarTextAbcence(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const statusBarLocator: By = By.css('div[id=\'theia-statusBar\']');

        // for ensuring that check is not invoked in the gap of status displaying
        for (let i: number = 0; i < 3; i++) {
            await this.driverHelper.wait(2000);

            await this.driverHelper.getDriver().wait(async () => {
                const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout);

                const isTextAbsent: boolean = elementText.search(expectedText) === -1;

                if (isTextAbsent) {
                    return true;
                }

            }, timeout);
        }
    }

    async waitIdeFrameAndSwitchOnIt(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout);
    }

    async checkLsInitializationStart(expectedTextInStatusBar: string) {
        try {
            await this.waitStatusBarContains(expectedTextInStatusBar, 20000);
        } catch (err) {
            if (!(err instanceof error.TimeoutError)) {
                throw err;
            }

            await this.driverHelper.getDriver().navigate().refresh();
            await this.waitAndSwitchToIdeFrame();
            await this.waitStatusBarContains(expectedTextInStatusBar);
        }

    }

    async closeAllNotifications() {
        const notificationLocator: By = By.css('.theia-Notification');

        if (! await this.driverHelper.isVisible(notificationLocator)) {
            return;
        }

        const notifications: WebElement[] = await this.driverHelper.waitAllPresence(notificationLocator);
        const notificationsCapacity: number = notifications.length;

        for (let i = 1; i <= notificationsCapacity; i++) {
            const notificationLocator: By = By.xpath('//div[@class=\'theia-Notification\']//button[text()=\'Close\']');

            const isElementVisible: boolean = await this.driverHelper.isVisible(notificationLocator);

            if (!isElementVisible) {
                continue;
            }

            await this.driverHelper.waitAndClick(notificationLocator);
        }
    }

    async performKeyCombination(keyCombination: string) {
        const bodyLocator: By = By.tagName('body');

        await this.driverHelper.type(bodyLocator, keyCombination);
    }

    async waitRightToolbarButtonSelection(buttonTitle: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const selectedRightToolbarButtonLocator: By = this.getSelectedRightToolbarButtonLocator(buttonTitle);

        await this.driverHelper.waitVisibility(selectedRightToolbarButtonLocator, timeout);
    }

    private getSelectedRightToolbarButtonLocator(buttonTitle: string): By {
        return By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title='${buttonTitle}' and contains(@id, 'shell-tab')] and contains(@class, 'p-mod-current')`);
    }

    private getRightToolbarButtonLocator(buttonTitle: String): By {
        return By.xpath(`//div[@id='theia-left-content-panel']//ul[@class='p-TabBar-content']` +
            `//li[@title='${buttonTitle}' and contains(@id, 'shell-tab')]`);
    }

    private getNotificationXpathLocator(notificationText: string): string {
        return `//div[@class='theia-Notification' and contains(@id,'${notificationText}')]`;
    }
}
