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
import { By } from 'selenium-webdriver';
import { TestWorkspaceUtil, WorkspaceStatus } from '../../utils/workspace/TestWorkspaceUtil';


@injectable()
export class Ide {
    public static readonly EXPLORER_BUTTON_XPATH: string = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Explorer\'])[1]';
    public static readonly SELECTED_EXPLORER_BUTTON_XPATH: string = '(//ul[@class=\'p-TabBar-content\']//li[@title=\'Explorer\' and contains(@class, \'p-mod-current\')])[1]';
    public static readonly ACTIVATED_IDE_IFRAME_CSS: string = '#ide-iframe-window[aria-hidden=\'false\']';
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

    async waitNotification(notificationMessage: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const notificationLocator: By = By.css(`div[id='notification-container-3-${notificationMessage}-|']`);

        await this.driverHelper.waitVisibility(notificationLocator, timeout);
    }

    async waitNotificationDisappearance(notificationMessage: string, attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        const notificationLocator: By = By.css(`div[id='notification-container-3-${notificationMessage}-|']`);

        await this.driverHelper.waitDisappearance(notificationLocator, attempts, polling);
    }

    async waitWorkspaceAndIde(workspaceNamespace: string, workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
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

    async waitExplorerButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.xpath(Ide.EXPLORER_BUTTON_XPATH), timeout);
    }

    async clickOnExplorerButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.xpath(Ide.EXPLORER_BUTTON_XPATH), timeout);
    }

    async waitTopMenuPanel(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Ide.TOP_MENU_PANEL_CSS), timeout);
    }

    async waitLeftContentPanel(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Ide.LEFT_CONTENT_PANEL_CSS));
    }

    async waitPreloaderAbsent(attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS, polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {
        await this.driverHelper.waitDisappearance(By.css(Ide.PRELOADER_CSS), attempts, polling);
    }

    async waitStatusBarContains(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) {
        const statusBarLocator: By = By.css('div[id=\'theia-statusBar\']');

        await this.driverHelper.waitUntilTrue(async () => {
            const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout);

            return elementText.search(expectedText) > 0;

        }, timeout);
    }

    async waitStatusBarTextAbcence(expectedText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const statusBarLocator: By = By.css('div[id=\'theia-statusBar\']');

        await this.driverHelper.waitUntilTrue(async () => {
            const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout);

            return elementText.search(expectedText) === 0;

        }, timeout);

    }

    async waitIdeFrameAndSwitchOnIt(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout);
    }

}
