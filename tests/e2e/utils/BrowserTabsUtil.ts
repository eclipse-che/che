/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { inject, injectable } from 'inversify';
import { CLASSES } from '../configs/inversify.types';
import { DriverHelper } from './DriverHelper';
import { Logger } from './Logger';
import { TimeoutConstants } from '../constants/TimeoutConstants';
import { ChromeDriverConstants } from '../constants/ChromeDriverConstants';

@injectable()
export class BrowserTabsUtil {
  constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

  async switchToWindow(windowHandle: string): Promise<void> {
    Logger.debug();
    await this.driverHelper.getDriver().switchTo().window(windowHandle);
  }

  async getAllWindowHandles(): Promise<string[]> {
    Logger.debug();

    return (await this.driverHelper.getDriver()).getAllWindowHandles();
  }

  async getCurrentWindowHandle(): Promise<string> {
    Logger.debug();

    return await this.driverHelper.getDriver().getWindowHandle();
  }

  async navigateTo(url: string): Promise<void> {
    Logger.debug(`${url}`);

    await this.driverHelper.getDriver().navigate().to(url);
  }

  async navigateAndWaitToUrl(url: string, timeout: number = TimeoutConstants.TS_SELENIUM_WAIT_FOR_URL): Promise<void> {
    Logger.trace(`${url}`);

    await this.navigateTo(url);
    await this.waitURL(url, timeout);
  }

  async waitAndSwitchToAnotherWindow(currentWindowHandle: string, timeout: number): Promise<void> {
    Logger.debug();

    await this.driverHelper.waitUntilTrue(async () => {
      const windowHandles: string[] = await this.getAllWindowHandles();

      return windowHandles.length > 1;
    }, timeout);

    const windowHandles: string[] = await this.getAllWindowHandles();

    for (const windowHandle of windowHandles) {
      if (windowHandle !== currentWindowHandle) {
        await this.switchToWindow(windowHandle);
      }
    }
  }

  async refreshPage(): Promise<void> {
    Logger.debug();

    await (await this.driverHelper.getDriver()).navigate().refresh();
  }

  async getCurrentUrl(): Promise<string> {
    return await this.driverHelper.getDriver().getCurrentUrl();
  }

  async waitURL(expectedUrl: string, timeout: number): Promise<void> {
    Logger.trace(`${expectedUrl}`);

    await this.driverHelper.getDriver().wait(async () => {
      const currentUrl: string = await this.driverHelper.getDriver().getCurrentUrl();
      const urlEquals: boolean = currentUrl === expectedUrl;

      if (urlEquals) {
        return true;
      }
    }, timeout);
  }

  async maximize(): Promise<void> {
    Logger.trace();
    if (ChromeDriverConstants.TS_SELENIUM_LAUNCH_FULLSCREEN) {
      Logger.debug(`TS_SELENIUM_LAUNCH_FULLSCREEN is set to true, maximizing window.`);
      await this.driverHelper.getDriver().manage().window().maximize();
    }
  }

  async closeAllTabsExceptCurrent(): Promise<void> {
    Logger.trace();
    const allTabsHandles: string[] = await this.getAllWindowHandles();
    const currentTabHandle: string = await this.getCurrentWindowHandle();
    allTabsHandles.splice(allTabsHandles.indexOf(currentTabHandle), 1);

    for (const tabHandle of allTabsHandles) {
      await this.switchToWindow(tabHandle);
      await this.driverHelper.getDriver().close();
    }
    await this.switchToWindow(currentTabHandle);
  }
}
