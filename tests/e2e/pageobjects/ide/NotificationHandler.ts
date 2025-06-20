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
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { WebElement } from 'monaco-page-objects';

@injectable()
export class NotificationHandler {
	private static readonly NOTIFICATION_MESSAGE: By = By.css('.notification-list-item-message');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	/**
	 * check for notifications containing specific text
	 *
	 * @param expectedText Text to search for in notifications
	 * @param timeoutSeconds Maximum time to wait for notification
	 * @returns True if notification with text was found
	 */
	async checkForNotification(
		expectedText: string,
		timeoutSeconds: number = TIMEOUT_CONSTANTS.TS_NOTIFICATION_WAIT_TIMEOUT
	): Promise<boolean> {
		Logger.debug(`Checking for notification containing: "${expectedText}"`);

		// check existing notifications first
		if (await this.findInExistingNotifications(expectedText)) {
			return true;
		}

		// wait for new notifications to appear
		const maxAttempts: number = Math.ceil((timeoutSeconds * 1000) / TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		for (let attempt: number = 0; attempt < maxAttempts; attempt++) {
			if (await this.findInExistingNotifications(expectedText)) {
				return true;
			}
			await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		}

		Logger.debug(`Notification containing "${expectedText}" not found after ${timeoutSeconds} seconds`);
		return false;
	}

	/**
	 * check for any of multiple notification texts
	 *
	 * @param texts Array of text strings to search for
	 * @param timeoutSeconds Maximum time to wait for notification
	 * @returns True if any notification with text was found
	 */
	async checkForAnyNotification(
		texts: string[],
		timeoutSeconds: number = TIMEOUT_CONSTANTS.TS_NOTIFICATION_WAIT_TIMEOUT
	): Promise<boolean> {
		for (const text of texts) {
			if (await this.checkForNotification(text, timeoutSeconds)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * find notification text in existing notifications
	 *
	 * @param expectedText Text to search for
	 * @returns True if text was found in any notification
	 */
	private async findInExistingNotifications(expectedText: string): Promise<boolean> {
		try {
			const elements: WebElement[] = await this.driverHelper.getDriver().findElements(NotificationHandler.NOTIFICATION_MESSAGE);

			for (const element of elements) {
				try {
					const text: string = await element.getText();
					if (text && text.toLowerCase().includes(expectedText.toLowerCase())) {
						Logger.debug(`Found notification: "${text}"`);
						return true;
					}
				} catch (err) {
					// continue to next element
				}
			}
		} catch (err) {
			// no notifications found, or other error occurred
		}

		return false;
	}
}
