/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
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
import { CHROME_DRIVER_CONSTANTS } from '../constants/CHROME_DRIVER_CONSTANTS';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';

@injectable()
export class BrowserTabsUtil {
	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

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

		await this.driverHelper.navigateToUrl(url);
	}

	async waitAndSwitchToAnotherWindow(
		currentWindowHandle: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL
	): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitUntilTrue(async (): Promise<boolean> => {
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
		Logger.trace();

		const maxAttempts: number = 5;
		let attempts: number = 0;
		while (attempts < maxAttempts) {
			try {
				return await this.driverHelper.getDriver().getCurrentUrl();

			} catch (e) {
				Logger.trace(`Attempt ${attempts + 1} failed: cannot get current url`);

				attempts++;

				await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_WAIT_FOR_URL);
			}
		}

		throw new Error('Max attempts reached: cannot get current url');
	}

	async waitURL(expectedUrl: string, timeout: number): Promise<void> {
		Logger.trace(`${expectedUrl}`);

		try {
			await this.driverHelper.getDriver().wait(async (): Promise<boolean | undefined> => {
				const currentUrl: string = await this.driverHelper.getDriver().getCurrentUrl();
				const urlEquals: boolean = currentUrl === expectedUrl;

				if (urlEquals) {
					return true;
				}
			}, timeout);
		} catch (e) {
			throw e;
		}
	}

	async maximize(): Promise<void> {
		Logger.trace();

		if (CHROME_DRIVER_CONSTANTS.TS_SELENIUM_LAUNCH_FULLSCREEN) {
			Logger.debug('TS_SELENIUM_LAUNCH_FULLSCREEN is set to true, maximizing window.');
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
