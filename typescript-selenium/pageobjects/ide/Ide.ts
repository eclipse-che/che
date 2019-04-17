import { DriverHelper } from "../../utils/DriverHelper";
import { injectable, inject } from "inversify";
import { CLASSES } from "../../types";
import { TestConstants } from "../../TestConstants";
import { By } from "selenium-webdriver";
import { TestWorkspaceUtil } from "../../utils/workspace/TestWorkspaceUtil";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

@injectable()
export class Ide {

    private readonly driverHelper: DriverHelper;
    private readonly testWorkspaceUtil: TestWorkspaceUtil;

    private static readonly TOP_MENU_PANEL_CSS: string = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    private static readonly LEFT_CONTENT_PANEL_CSS: string = "#theia-left-content-panel";
    public static readonly EXPLORER_BUTTON_XPATH: string = "(//ul[@class='p-TabBar-content']//li[@title='Explorer'])[1]";
    public static readonly SELECTED_EXPLORER_BUTTON_XPATH: string = "(//ul[@class='p-TabBar-content']//li[@title='Explorer' and contains(@class, 'p-mod-current')])[1]"
    private static readonly PRELOADER_CSS: string = ".theia-preload";
    private static readonly IDE_IFRAME_CSS: string = "iframe#ide-application-iframe";

    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper,
        @inject(CLASSES.TestWorkspaceUtil) testWorkspaceUtil: TestWorkspaceUtil
    ) {
        this.driverHelper = driverHelper;
        this.testWorkspaceUtil = testWorkspaceUtil;
    }

    async waitAndSwitchToIdeFrame(timeout = TestConstants.LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout)
    }

    async waitNotification(notificationMessage: string, timeout = TestConstants.DEFAULT_TIMEOUT) {
        const notificationLocator: By = By.css(`div[id='notification-container-3-${notificationMessage}-|']`)

        await this.driverHelper.waitVisibility(notificationLocator, timeout)
    }

    async waitNotificationDisappearance(notificationMessage: string, attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING) {
        const notificationLocator: By = By.css(`div[id='notification-container-3-${notificationMessage}-|']`)

        await this.driverHelper.waitDisappearance(notificationLocator, attempts, polling)
    }

    async waitWorkspaceAndIde(workspaceNamespace: string, workspaceName: string, timeout = TestConstants.LOAD_PAGE_TIMEOUT) {
        await this.testWorkspaceUtil.waitRunningStatus(workspaceNamespace, workspaceName)
        await this.waitIde(timeout)
    }

    async waitIde(timeout = TestConstants.LOAD_PAGE_TIMEOUT) {
        const mainIdeParts: Array<By> = [By.css(Ide.TOP_MENU_PANEL_CSS), By.css(Ide.LEFT_CONTENT_PANEL_CSS), By.xpath(Ide.EXPLORER_BUTTON_XPATH)]

        for (const idePartLocator of mainIdeParts) {
            await this.driverHelper.waitVisibility(idePartLocator, timeout)
        }
    }

    async waitExplorerButton(timeout = TestConstants.DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.xpath(Ide.EXPLORER_BUTTON_XPATH), timeout)
    }

    async clickOnExplorerButton(timeout = TestConstants.DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.xpath(Ide.EXPLORER_BUTTON_XPATH), timeout)
    }

    async waitTopMenuPanel(timeout = TestConstants.DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Ide.TOP_MENU_PANEL_CSS), timeout)
    }

    async waitLeftContentPanel(timeout = TestConstants.DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(Ide.LEFT_CONTENT_PANEL_CSS))
    }

    async waitPreloaderAbsent(attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING) {
        await this.driverHelper.waitDisappearance(By.css(Ide.PRELOADER_CSS), attempts, polling)
    }

    async waitStatusBarContains(expectedText: string, timeout = TestConstants.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT) {
        const statusBarLocator: By = By.css("div[id='theia-statusBar']")

        await this.driverHelper.waitUntilTrue(async () => {
            const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout)

            return elementText.search(expectedText) > 0

        }, timeout)
    }

    async waitStatusBarTextAbcence(expectedText: string, timeout = TestConstants.DEFAULT_TIMEOUT) {
        const statusBarLocator: By = By.css("div[id='theia-statusBar']")

        await this.driverHelper.waitUntilTrue(async () => {
            const elementText: string = await this.driverHelper.waitAndGetText(statusBarLocator, timeout)

            return elementText.search(expectedText) === 0

        }, timeout)

    }

    async waitIdeFrameAndSwitchOnIt(timeout = TestConstants.LOAD_PAGE_TIMEOUT){
        await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout) 
    }

}
