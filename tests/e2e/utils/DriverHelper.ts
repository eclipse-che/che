/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { IDriver } from '../driver/IDriver';
import { inject, injectable } from 'inversify';
import { TYPES } from '../configs/inversify.types';
import { Actions, By, error, ThenableWebDriver, until, WebElement } from 'selenium-webdriver';
import 'reflect-metadata';
import { Logger } from './Logger';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';

@injectable()
export class DriverHelper {
	private readonly driver: ThenableWebDriver;

	constructor(@inject(TYPES.Driver) driver: IDriver) {
		this.driver = driver.get();
	}

	getAction(): Actions {
		Logger.trace();

		return this.driver.actions();
	}

	async isVisible(locator: By): Promise<boolean> {
		Logger.trace(`${locator}`);

		try {
			const element: WebElement = await this.driver.findElement(locator);
			return await element.isDisplayed();
		} catch {
			return false;
		}
	}

	async wait(milliseconds: number): Promise<void> {
		Logger.trace(`(${milliseconds} milliseconds)`);

		await this.driver.sleep(milliseconds);
	}

	async refreshPage(): Promise<void> {
		Logger.trace();

		await this.driver.navigate().refresh();
	}

	async waitVisibilityBoolean(
		locator: By,
		attempts: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<boolean> {
		Logger.trace(`${locator}`);

		for (let i: number = 0; i < attempts; i++) {
			const isVisible: boolean = await this.isVisible(locator);

			if (isVisible) {
				return true;
			}

			await this.wait(polling);
		}

		return false;
	}

	async waitDisappearanceBoolean(
		locator: By,
		attempts: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<boolean> {
		Logger.trace(`${locator}`);

		for (let i: number = 0; i < attempts; i++) {
			const isVisible: boolean = await this.isVisible(locator);

			if (!isVisible) {
				return true;
			}

			await this.wait(polling);
		}

		return false;
	}

	async waitVisibility(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<WebElement> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.driver.wait(until.elementLocated(elementLocator), polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					if (attempts !== 1) {
						// waitVisibility was spamming other methods when the number of attempts was 1 - only show message if attempts > 1
						Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					}
					continue;
				}

				if (err instanceof error.NoSuchWindowError) {
					// sometimes waitVisibility fails with NoSuchWindowError when the check is run too soon before the page loads
					Logger.trace(`failed with NoSuchWindow exception. Attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				const visibleWebElement: WebElement = await this.driver.wait(until.elementIsVisible(element), polling);
				Logger.trace('element is located and is visible.');
				return visibleWebElement;
			} catch (err) {
				if (err instanceof error.TimeoutError) {
					if (attempts !== 1) {
						// waitVisibility was spamming other methods when the number of attempts was 1 - only show message if attempts > 1
						Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					}
					continue;
				}

				if (err instanceof error.StaleElementReferenceError) {
					Logger.debug(`stale element error - ${JSON.stringify(err)}`);
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(
			`Exceeded maximum visibility checking attempts for '${elementLocator}' element, timeouted after ${timeout}`
		);
	}

	async waitPresence(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<WebElement> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			try {
				return await this.driver.wait(until.elementLocated(elementLocator), polling);
			} catch (err) {
				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(
			`Exceeded maximum presence checking attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`
		);
	}

	async waitAllPresence(
		elementLocator: By,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
	): Promise<Array<WebElement>> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			try {
				return await this.driver.wait(until.elementsLocated(elementLocator), polling);
			} catch (err) {
				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(
			`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`
		);
	}

	async waitAllVisibility(locators: Array<By>, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		Logger.trace(`${locators}`);

		for (const elementLocator of locators) {
			await this.waitVisibility(elementLocator, timeout);
		}
	}

	async waitDisappearance(
		elementLocator: By,
		attempts: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<void> {
		Logger.trace(`${elementLocator}`);

		const isDisappeared: boolean = await this.waitDisappearanceBoolean(elementLocator, attempts, polling);

		if (!isDisappeared) {
			throw new error.TimeoutError(`Waiting attempts exceeded, element '${elementLocator}' is still visible`);
		}
	}

	async waitAllDisappearance(
		locators: Array<By>,
		attemptsPerLocator: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_ATTEMPTS,
		pollingPerLocator: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING
	): Promise<void> {
		Logger.trace(`${locators}`);

		try {
			for (const elementLocator of locators) {
				await this.waitDisappearance(elementLocator, attemptsPerLocator, pollingPerLocator);
			}
		} catch (e) {
			throw e;
		}
	}

	async waitAndClick(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitVisibility(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				await element.click();
				return;
			} catch (err) {
				function isElementClickInterceptedOnLastAttempt(err: any, i: number): boolean {
					return err instanceof error.ElementClickInterceptedError && i === attempts - 1;
				}

				if (err instanceof error.StaleElementReferenceError || err instanceof error.ElementClickInterceptedError) {
					Logger.debug(`${elementLocator} - ${JSON.stringify(err)}`);
					await this.wait(polling);
					continue;
				}

				if (isElementClickInterceptedOnLastAttempt(err, i)) {
					Logger.debug('element is not clickable, try to perform pointer click');
					await this.getAction()
						.move({
							origin: await this.waitPresence(elementLocator)
						})
						.click()
						.perform();
					return;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum clicking attempts, the '${elementLocator}' element is not clickable`);
	}

	async waitAndGetElementAttribute(
		elementLocator: By,
		attribute: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
	): Promise<string> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator} attribute: '${attribute}'`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitVisibility(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				return await element.getAttribute(attribute);
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(
			`Exceeded maximum gettin of the '${attribute}' attribute attempts, from the '${elementLocator}' element`
		);
	}

	async waitAndGetCssValue(
		elementLocator: By,
		cssAttribute: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
	): Promise<string> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator} cssAttribute: ${cssAttribute}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitVisibility(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				return await element.getCssValue(cssAttribute);
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(
			`Exceeded maximum getting of the '${cssAttribute}' css attribute attempts, from the '${elementLocator}' element`
		);
	}

	async waitAttributeValue(elementLocator: By, attribute: string, expectedValue: string, timeout: number): Promise<void> {
		Logger.trace(`${elementLocator}`);

		await this.driver.wait(
			async (): Promise<boolean> => {
				const attributeValue: string = await this.waitAndGetElementAttribute(elementLocator, attribute, timeout);

				return expectedValue === attributeValue;
			},
			timeout,
			`The '${attribute}' attribute value doesn't match with expected value '${expectedValue}'`
		);
	}

	async type(elementLocator: By, text: string, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);

		if (elementLocator.toString().toLocaleLowerCase().includes('password')) {
			Logger.trace(`${elementLocator} text: ***`);
		} else {
			Logger.trace(`${elementLocator} text: ${text}`);
		}

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitVisibility(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				await element.sendKeys(text);
				return;
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
	}

	async typeToInvisible(
		elementLocator: By,
		text: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
	): Promise<void> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator} text: ${text}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitPresence(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				await element.sendKeys(text);
				return;
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
	}

	async clear(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitVisibility(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				await element.clear();
				return;
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
	}

	async clearInvisible(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitPresence(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				await element.clear();
				return;
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
	}

	async enterValue(
		elementLocator: By,
		text: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
	): Promise<void> {
		if (elementLocator.toString().toLocaleLowerCase().includes('password')) {
			Logger.trace(`${elementLocator} text: ***`);
		} else {
			Logger.trace(`${elementLocator} text: ${text}`);
		}

		await this.waitVisibility(elementLocator, timeout);
		await this.clear(elementLocator);
		await this.waitAttributeValue(elementLocator, 'value', '', timeout);
		await this.type(elementLocator, text, timeout);
		await this.waitAttributeValue(elementLocator, 'value', text, timeout);
	}

	async waitAndSwitchToFrame(iframeLocator: By, timeout: number): Promise<void> {
		Logger.trace(`${iframeLocator}`);

		await this.driver.wait(until.ableToSwitchToFrame(iframeLocator), timeout);
	}

	async waitAndGetText(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<string> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitVisibility(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				return await element.getText();
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum text obtaining attempts, from the '${elementLocator}' element`);
	}

	async waitAndGetValue(elementLocator: By, timeout: number): Promise<string> {
		Logger.trace(`${elementLocator}`);

		return await this.waitAndGetElementAttribute(elementLocator, 'value', timeout);
	}

	async waitUntilTrue(callback: any, timeout: number): Promise<void> {
		Logger.trace();

		await this.driver.wait(callback, timeout);
	}

	async scrollTo(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		const polling: number = TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING;
		const attempts: number = Math.ceil(timeout / polling);
		Logger.trace(`${elementLocator}`);

		for (let i: number = 0; i < attempts; i++) {
			let element: WebElement;
			try {
				element = await this.waitPresence(elementLocator, polling);
			} catch (err) {
				if (i >= attempts - 1) {
					Logger.error(`failed with exception, out of attempts - ${err}`);
					throw err;
				}

				if (err instanceof error.TimeoutError) {
					Logger.trace(`polling timed out attempt #${i + 1}, retrying with ${polling}ms timeout`);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}

			try {
				await this.getDriver().executeScript('arguments[0].scrollIntoView(true);', element);
				return;
			} catch (err) {
				if (err instanceof error.StaleElementReferenceError) {
					await this.wait(polling);
					continue;
				}

				Logger.error(`failed with an unexpected exception - ${err}`);
				throw err;
			}
		}

		throw new error.TimeoutError(`Exceeded maximum mouse move attempts, for the '${elementLocator}' element`);
	}

	async scrollToAndClick(elementLocator: By, timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		Logger.trace();

		await this.scrollTo(elementLocator, timeout);
		await this.waitAndClick(elementLocator, timeout);
	}

	async scrollToAndEnterValue(
		elementLocator: By,
		value: string,
		timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
	): Promise<void> {
		Logger.trace();

		await this.scrollTo(elementLocator, timeout);
		await this.enterValue(elementLocator, value, timeout);
	}

	// method is useful to debug page object elements
	async highLightElement(element: WebElement): Promise<void> {
		Logger.trace();

		await this.getDriver().executeScript('arguments[0].style.border="2px solid red"', element);
	}

	getDriver(): ThenableWebDriver {
		Logger.trace();

		return this.driver;
	}

	async navigateToUrl(url: string): Promise<void> {
		Logger.trace();

		await this.getDriver().navigate().to(url);
	}
}
