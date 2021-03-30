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
import { Key, By, error } from 'selenium-webdriver';
import { CLASSES } from '../inversify.types';
import { DriverHelper } from './DriverHelper';
import { Logger } from './Logger';

@injectable()
export class BrowserWindowsUtil {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async switchToWindow(windowHandle: string) {
        Logger.debug('BrowserWindowsUtil.switchToWindow')
        await this.driverHelper.getDriver().switchTo().window(windowHandle);
    }

    async getAllWindowHandles(): Promise<string[]> {
        Logger.debug('BrowserWindowsUtil.getAllWindowHandles')

        return (await this.driverHelper.getDriver()).getAllWindowHandles();
    }

    async getCurrentWindowHandle(): Promise<string> {
        Logger.debug('BrowserWindowsUtil.getCurrentWindowHandle')

        return await this.driverHelper.getDriver().getWindowHandle();
    }

    async navigateTo(url: string) {
        Logger.debug(`BrowserWindowsUtil.navigateTo ${url}`)

        await this.driverHelper.navigateAndWaitToUrl(url);
    }

    async waitAndSwitchToAnotherWindow(currentWindowHandle: string, timeout: number) {
        Logger.debug('BrowserWindowsUtil.waitAndSwitchToAnotherWindow')

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
        Logger.debug('BrowserWindowsUtil.waitContentAvailableInTheNewTab')

        await this.driverHelper.waitVisibility(contentLocator, timeout);
    }
}
