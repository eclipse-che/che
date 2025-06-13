/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../configs/inversify.types';
import { By, WebElement } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';

@injectable()
export class NotificationHandler {
	private static readonly NOTIFICATION_SELECTORS: string[] = [
		'.notification-list-item-message',
		'[class*="notification"]',
		'.monaco-list-row'
	];

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	/**
	 * check for notifications containing specific text
	 */
	async checkForNotification(expectedText: string, timeoutSeconds: number = 20): Promise<boolean> {
		Logger.info(`Checking for notification containing: "${expectedText}"`);

		// check existing notifications first
		if (await this.findInExistingNotifications(expectedText)) {
			return true;
		}

		// wait for new notifications to appear
		const maxAttempts: number = timeoutSeconds / 2;
		for (let attempt: number = 0; attempt < maxAttempts; attempt++) {
			if (await this.findInExistingNotifications(expectedText)) {
				return true;
			}
			await this.driverHelper.wait(2000);
		}

		Logger.debug(`Notification containing "${expectedText}" not found`);
		return false;
	}

	/**
	 * check for any of multiple notification texts
	 */
	async checkForAnyNotification(texts: string[], timeoutSeconds: number = 20): Promise<boolean> {
		for (const text of texts) {
			if (await this.checkForNotification(text, timeoutSeconds)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * find notification text in existing notifications
	 */
	private async findInExistingNotifications(expectedText: string): Promise<boolean> {
		for (const selector of NotificationHandler.NOTIFICATION_SELECTORS) {
			try {
				const elements: WebElement[] = await this.driverHelper.getDriver().findElements(By.css(selector));

				for (const element of elements) {
					try {
						const text: string = await element.getText();
						if (text.trim() && text.toLowerCase().includes(expectedText.toLowerCase())) {
							Logger.debug(`Found notification: "${text}"`);
							return true;
						}
					} catch (err) {
						// continue to next element
					}
				}
			} catch (err) {
				// continue to next selector
			}
		}
		return false;
	}
}
