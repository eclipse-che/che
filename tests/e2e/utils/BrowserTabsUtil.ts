/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { By, error, Key } from 'selenium-webdriver';
import { CLASSES } from '../inversify.types';
import { DriverHelper } from './DriverHelper';
import { Logger } from './Logger';
import { TimeoutConstants } from '../TimeoutConstants';

@injectable()
export class BrowserTabsUtil {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async switchToWindow(windowHandle: string) {
        Logger.debug('BrowserTabsUtil.switchToWindow');
        await this.driverHelper.getDriver().switchTo().window(windowHandle);
    }

    async getAllWindowHandles(): Promise<string[]> {
        Logger.debug('BrowserTabsUtil.getAllWindowHandles');

        return (await this.driverHelper.getDriver()).getAllWindowHandles();
    }

    async getCurrentWindowHandle(): Promise<string> {
        Logger.debug('BrowserTabsUtil.getCurrentWindowHandle');

        return await this.driverHelper.getDriver().getWindowHandle();
    }

    async navigateTo(url: string) {
        Logger.debug(`BrowserTabsUtil.navigateTo ${url}`);

        await this.driverHelper.getDriver().navigate().to(url);
    }

    async navigateAndWaitToUrl(url: string, timeout: number = TimeoutConstants.TS_SELENIUM_WAIT_FOR_URL) {
        Logger.trace(`BrowserTabsUtil.navigateAndWaitToUrl ${url}`);

        await this.navigateTo(url);
        await this.waitURL(url, timeout);
    }

    async waitAndSwitchToAnotherWindow(currentWindowHandle: string, timeout: number) {
        Logger.debug('BrowserTabsUtil.waitAndSwitchToAnotherWindow');

        await this.driverHelper.waitUntilTrue(async () => {
            const windowHandles: string[] = await this.getAllWindowHandles();

            return windowHandles.length > 1;
        }, timeout);

        const windowHandles: string[] = await this.getAllWindowHandles();

        windowHandles.forEach(async windowHandle => {
            if (windowHandle !== currentWindowHandle) {
                await this.switchToWindow(windowHandle);
                return;
            }
        });
    }

    async waitContentAvailableInTheNewTab(contentLocator: By, timeout: number) {
        Logger.debug('BrowserTabsUtil.waitContentAvailableInTheNewTab');

        await this.driverHelper.waitVisibility(contentLocator, timeout);
    }

    async refreshPage() {
        Logger.debug('BrowserTabsUtil.refreshPage');

        await (await this.driverHelper.getDriver()).navigate().refresh();
    }

    async refreshForDebug() {
        Logger.debug('BrowserTabsUtil.refreshForDebug');
        
        // If refresh triggers debug breakpoint test stucks of the refreshing
        // and fail with a timeout error.
        try {
            await (await this.driverHelper.getDriver()).navigate().refresh();
        } catch (err) {
            if (!(err instanceof error.TimeoutError)) {
                throw err
            }
        }
    }

    async getCurrentUrl(): Promise<string> {
        return await this.driverHelper.getDriver().getCurrentUrl();
    }

    async waitURL(expectedUrl: string, timeout: number) {
        Logger.trace(`BrowserTabsUtil.waitURL ${expectedUrl}`);

        await this.driverHelper.getDriver().wait(async () => {
            const currentUrl: string = await this.driverHelper.getDriver().getCurrentUrl();
            const urlEquals: boolean = currentUrl === expectedUrl;

            if (urlEquals) {
                return true;
            }
        }, timeout);
    }

    public async maximize() {
        Logger.trace(`BrowserTabsUtil.maximize`);

        await this.driverHelper.getDriver().manage().window().maximize();
    }

}
