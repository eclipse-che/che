/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { IDriver } from '../driver/IDriver';
import { inject, injectable } from 'inversify';
import { TYPES } from '../inversify.types';
import { error, Actions } from 'selenium-webdriver';
import 'reflect-metadata';
import { ThenableWebDriver, By, until, WebElement } from 'selenium-webdriver';
import { TestConstants } from '../TestConstants';
import { Logger } from './Logger';
import { TimeoutConstants } from '../TimeoutConstants';


@injectable()
export class DriverHelper {
    private readonly driver: ThenableWebDriver;

    constructor(@inject(TYPES.Driver) driver: IDriver) {
        this.driver = driver.get();
    }

    public getAction(): Actions {
        Logger.trace('DriverHelper.getAction');

        return this.driver.actions();
    }

    /**
     * @deprecated Method deprecated. Use the next method instead.
     * @see BrowserTabsUtil.maximize()
     */
    public async maximize() {
        Logger.trace(`DriverHelper.maximize`);

        await this.driver.manage().window().maximize();
    }

    public async isVisible(locator: By): Promise<boolean> {
        Logger.trace(`DriverHelper.isVisible ${locator}`);

        try {
            const element: WebElement = await this.driver.findElement(locator);
            const isVisible: boolean = await element.isDisplayed();
            return isVisible;
        } catch {
            return false;
        }
    }

    public async wait(milliseconds: number) {
        Logger.trace(`DriverHelper.wait (${milliseconds} milliseconds)`);

        await this.driver.sleep(milliseconds);
    }

    public async waitVisibilityBoolean(locator: By,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING): Promise<boolean> {

        Logger.trace(`DriverHelper.waitVisibilityBoolean ${locator}`);

        for (let i = 0; i < attempts; i++) {
            const isVisible: boolean = await this.isVisible(locator);

            if (isVisible) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public async waitDisappearanceBoolean(locator: By,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING): Promise<boolean> {

        Logger.trace(`DriverHelper.waitDisappearanceBoolean ${locator}`);

        for (let i = 0; i < attempts; i++) {
            const isVisible: boolean = await this.isVisible(locator);

            if (!isVisible) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public async waitVisibility(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<WebElement> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitVisibility ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.driver.wait(until.elementLocated(elementLocator), polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.waitVisibility - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    if (attempts !== 1) { // waitVisibility was spamming other methods when the number of attempts was 1 - only show message if attempts > 1
                        Logger.trace(`DriverHelper.waitVisibility - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    }
                    continue;
                }

                if (err instanceof error.NoSuchWindowError) { // sometimes waitVisibility fails with NoSuchWindowError when the check is run too soon before the page loads
                    Logger.trace(`DriverHelper.waitVisibility - failed with NoSuchWindow exception. Attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.waitVisibility - failed with an unexpected exception - ${err}`);
                throw err;
            }

            try {
                const visibleWebElement = await this.driver.wait(until.elementIsVisible(element), polling);
                Logger.trace('DriverHelper.waitVisibility - Element is located and is visible.');
                return visibleWebElement;
            } catch (err) {
                if (err instanceof error.TimeoutError) {
                    if (attempts !== 1) { // waitVisibility was spamming other methods when the number of attempts was 1 - only show message if attempts > 1
                        Logger.trace(`DriverHelper.waitVisibility - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    }
                    continue;
                }

                if (err instanceof error.StaleElementReferenceError) {
                    Logger.debug(`DriverHelper.waitVisibility - Stale element error - ${err}`);
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitVisibility - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum visibility checkings attempts for '${elementLocator}' element, timeouted after ${timeout}`);
    }

    public async waitPresence(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<WebElement> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitPresence ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            try {
                const webElement: WebElement = await this.driver.wait(until.elementLocated(elementLocator), polling);
                return webElement;
            } catch (err) {
                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.waitPresence - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitPresence - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
    }

    public async waitAllPresence(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<Array<WebElement>> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitAllPresence ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            try {
                const webElements: Array<WebElement> = await this.driver.wait(until.elementsLocated(elementLocator), polling);
                return webElements;
            } catch (err) {
                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.waitAllPresence - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitAllPresence - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
    }

    public async waitAllVisibility(locators: Array<By>, timeout: number) {
        Logger.trace(`DriverHelper.waitAllVisibility ${locators}`);

        for (const elementLocator of locators) {
            await this.waitVisibility(elementLocator, timeout);
        }
    }

    public async waitDisappearance(elementLocator: By,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.trace(`DriverHelper.waitDisappearance ${elementLocator}`);

        const isDisappeared = await this.waitDisappearanceBoolean(elementLocator, attempts, polling);

        if (!isDisappeared) {
            throw new error.TimeoutError(`Waiting attempts exceeded, element '${elementLocator}' is still visible`);
        }
    }

    public async waitDisappearanceWithTimeout(elementLocator: By, timeout: number) {
        Logger.trace(`DriverHelper.waitDisappearanceWithTimeout ${elementLocator}`);

        await this.getDriver().wait(async () => {
            const isVisible: boolean = await this.isVisible(elementLocator);

            if (!isVisible) {
                return true;
            }
        }, timeout);
    }

    public async waitAllDisappearance(locators: Array<By>,
        attemptsPerLocator: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        pollingPerLocator: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        Logger.trace(`DriverHelper.waitAllDisappearance ${locators}`);

        for (const elementLocator of locators) {
            await this.waitDisappearance(elementLocator, attemptsPerLocator, pollingPerLocator);
        }
    }

    public async waitAndClick(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitAndClick ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitVisibility(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.waitAndClick - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.waitAndClick - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndClick - failed with an unexpected exception - ${err}`);
                throw err;
            }

            try {
                await element.click();
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    Logger.debug(`DriverHelper.waitAndClik - ${elementLocator} - StaleElementReferenceError - ${err}`);
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndClick - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum clicking attempts, the '${elementLocator}' element is not clickable`);

    }

    public async waitAndGetElementAttribute(elementLocator: By, attribute: string,
        timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<string> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitAndGetElementAttribute ${elementLocator} attribute: '${attribute}'`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitVisibility(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.waitAndGetElementAttribute - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.waitAndGetElementAttribute - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndGetElementAttribute - failed with an unexpected exception - ${err}`);
                throw err;
            }

            try {
                const attributeValue = await element.getAttribute(attribute);
                return attributeValue;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndGetElementAttribute - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum gettin of the '${attribute}' attribute attempts, from the '${elementLocator}' element`);
    }

    public async waitAndGetCssValue(elementLocator: By, cssAttribute: string,
        timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<string> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitAndGetCssValue ${elementLocator} cssAttribute: ${cssAttribute}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitVisibility(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.waitAndGetCssValue - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.waitAndGetCssValue - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndGetCssValue - failed with an unexpected exception - ${err}`);
                throw err;
            }

            try {
                const cssAttributeValue = await element.getCssValue(cssAttribute);
                return cssAttributeValue;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndGetCssValue - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum gettin of the '${cssAttribute}' css attribute attempts, from the '${elementLocator}' element`);
    }

    public async waitAttributeValue(elementLocator: By,
        attribute: string,
        expectedValue: string,
        timeout: number) {

        Logger.trace(`DriverHelper.waitAttributeValue ${elementLocator}`);

        await this.driver.wait(async () => {
            const attributeValue: string = await this.waitAndGetElementAttribute(elementLocator, attribute, timeout);

            return expectedValue === attributeValue;
        },
            timeout,
            `The '${attribute}' attribute value doesn't match with expected value '${expectedValue}'`);
    }

    public async type(elementLocator: By, text: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.type ${elementLocator} text: ${text}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitVisibility(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.type - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.type - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.type - failed with an unexpected exception - ${err}`);
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

                Logger.error(`DriverHelper.type - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
    }

    public async typeToInvisible(elementLocator: By, text: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.typeToInvisible ${elementLocator} text: ${text}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitPresence(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.typeToInvisible - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.typeToInvisible - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.typeToInvisible - failed with an unexpected exception - ${err}`);
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

                Logger.error(`DriverHelper.typeToInvisible - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
    }

    public async clear(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.clear ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitVisibility(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.clear - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.clear - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.clear - failed with an unexpected exception - ${err}`);
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

                Logger.error(`DriverHelper.clear - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
    }

    public async clearInvisible(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.clearInvisible ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitPresence(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.clearInvisible - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.clearInvisible - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.clearInvisible - failed with an unexpected exception - ${err}`);
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

                Logger.error(`DriverHelper.clearInvisible - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
    }

    public async enterValue(elementLocator: By, text: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.trace(`DriverHelper.enterValue ${elementLocator} text: ${text}`);

        await this.waitVisibility(elementLocator, timeout);
        await this.clear(elementLocator);
        await this.waitAttributeValue(elementLocator, 'value', '', timeout);
        await this.type(elementLocator, text, timeout);
        await this.waitAttributeValue(elementLocator, 'value', text, timeout);
    }

    public async waitAndSwitchToFrame(iframeLocator: By, timeout: number) {
        Logger.trace(`DriverHelper.waitAndSwitchToFrame ${iframeLocator}`);

        await this.driver.wait(until.ableToSwitchToFrame(iframeLocator), timeout);
    }

    public async waitAndGetText(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<string> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.waitAndGetText ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitVisibility(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.waitAndGetText - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.waitAndGetText - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndGetText - failed with an unexpected exception - ${err}`);
                throw err;
            }

            try {
                const innerText: string = await element.getText();
                return innerText;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.waitAndGetText - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum text obtaining attempts, from the '${elementLocator}' element`);
    }

    public async waitAndGetValue(elementLocator: By, timeout: number): Promise<string> {
        Logger.trace(`DriverHelper.waitAndGetValue ${elementLocator}`);

        const elementValue: string = await this.waitAndGetElementAttribute(elementLocator, 'value', timeout);
        return elementValue;
    }

    public async waitUntilTrue(callback: any, timeout: number) {
        Logger.trace('DriverHelper.waitUntilTrue');

        await this.driver.wait(callback, timeout);
    }

    /**
     * @deprecated Method deprecated. Use the next method instead.
     * @see BrowserTabsUtil.refreshPage()
     */
    public async reloadPage() {
        Logger.debug('DriverHelper.reloadPage');

        await this.driver.navigate().refresh();
    }

    /**
     * @deprecated Method deprecated. Use the next method instead.
     * @see BrowserTabsUtil.navigateAndWaitToUrl()
     */
    public async navigateAndWaitToUrl(url: string, timeout: number = TimeoutConstants.TS_SELENIUM_WAIT_FOR_URL) {
        Logger.trace(`DriverHelper.navigateAndWaitToUrl ${url}`);

        await this.navigateToUrl(url);
        await this.waitURL(url, timeout);
    }

    /**
     * @deprecated Method deprecated. Use the next method instead.
     * @see BrowserTabsUtil.navigateTo()
     */
    public async navigateToUrl(url: string) {
        Logger.debug(`DriverHelper.navigateToUrl ${url}`);

        await this.driver.navigate().to(url);
    }

    /**
     * @deprecated Method deprecated. Use the next method instead.
     * @see BrowserTabsUtil.waitURL()
     */
    public async waitURL(expectedUrl: string, timeout: number) {
        Logger.trace(`DriverHelper.waitURL ${expectedUrl}`);

        await this.getDriver().wait(async () => {
            const currentUrl: string = await this.getDriver().getCurrentUrl();
            const urlEquals: boolean = currentUrl === expectedUrl;

            if (urlEquals) {
                return true;
            }
        }, timeout);
    }

    public async scrollTo(elementLocator: By, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);

        Logger.trace(`DriverHelper.scrollTo ${elementLocator}`);

        for (let i = 0; i < attempts; i++) {
            let element: WebElement;
            try {
                element = await this.waitPresence(elementLocator, polling);
            } catch (err) {
                if (i >= attempts - 1) {
                    Logger.error(`DriverHelper.scrollTo - failed with exception, out of attempts - ${err}`);
                    throw err;
                }

                if (err instanceof error.TimeoutError) {
                    Logger.trace(`DriverHelper.scrollTo - Polling timed out attempt #${(i + 1)}, retrying with ${polling}ms timeout`);
                    continue;
                }

                Logger.error(`DriverHelper.scrollTo - failed with an unexpected exception - ${err}`);
                throw err;
            }

            try {
                await this.getAction().move({origin: element}).perform();
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                Logger.error(`DriverHelper.scrollTo - failed with an unexpected exception - ${err}`);
                throw err;
            }
        }

        throw new error.TimeoutError(`Exceeded maximum mouse move attempts, for the '${elementLocator}' element`);
    }

    /**
     * @deprecated Method deprecated. Use the next method instead.
     * @see BrowserTabsUtil.getCurrentUrl()
     */
    public async getCurrentUrl(): Promise<string> {
        return await this.driver.getCurrentUrl();
    }

    getDriver(): ThenableWebDriver {
        Logger.trace('DriverHelper.getDriver');

        return this.driver;
    }

    public sleep(time: number) {
        this.driver.sleep(time);
    }
}
