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
import { error, ActionSequence } from 'selenium-webdriver';
import 'reflect-metadata';
import { ThenableWebDriver, By, until, WebElement } from 'selenium-webdriver';
import { TestConstants } from '../TestConstants';


@injectable()
export class DriverHelper {
    private readonly driver: ThenableWebDriver;

    constructor(@inject(TYPES.Driver) driver: IDriver) {
        this.driver = driver.get();
    }

    public getAction(): ActionSequence {
        return this.driver.actions();
    }

    public async isVisible(locator: By): Promise<boolean> {
        try {
            const element: WebElement = await this.driver.findElement(locator);
            const isVisible: boolean = await element.isDisplayed();
            return isVisible;
        } catch {
            return false;
        }
    }

    public async wait(miliseconds: number) {
        await this.driver.sleep(miliseconds);
    }

    public async waitVisibilityBoolean(locator: By,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING): Promise<boolean> {

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

        for (let i = 0; i < attempts; i++) {
            const isVisible: boolean = await this.isVisible(locator);

            if (!isVisible) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public async waitVisibility(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<WebElement> {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const webElement: WebElement = await this.driver.wait(until.elementLocated(elementLocator), timeout);

            try {
                const visibleWebElement = await this.driver.wait(until.elementIsVisible(webElement), timeout);
                return visibleWebElement;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum visibility checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
    }

    public async waitPresence(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<WebElement> {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            try {
                const webElement: WebElement = await this.driver.wait(until.elementLocated(elementLocator), timeout);
                return webElement;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
    }

    public async waitAllPresence(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<Array<WebElement>> {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            try {
                const webElements: Array<WebElement> = await this.driver.wait(until.elementsLocated(elementLocator), timeout);
                return webElements;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum presence checkings attempts, problems with 'StaleElementReferenceError' of '${elementLocator}' element`);
    }

    public async waitAllVisibility(locators: Array<By>, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        for (const elementLocator of locators) {
            await this.waitVisibility(elementLocator, timeout);
        }
    }

    public async waitDisappearance(elementLocator: By,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        const isDisappeared = await this.waitDisappearanceBoolean(elementLocator, attempts, polling);

        if (!isDisappeared) {
            throw new Error(`Waiting attempts exceeded, element '${elementLocator}' is still visible`);
        }
    }

    public async waitDisappearanceWithTimeout(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.getDriver().wait(async () => {
            const isVisible: boolean = await this.isVisible(elementLocator);

            if (!isVisible) {
                return true;
            }
        }, timeout);
    }

    public async waitAllDisappearance(locators: Array<By>,
        attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING) {

        for (const elementLocator of locators) {
            await this.waitDisappearance(elementLocator, attempts, polling);
        }
    }

    public async waitAndClick(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, timeout);

            try {
                await element.click();
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                if (err instanceof error.WebDriverError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum clicking attempts, the '${elementLocator}' element is not clickable`);

    }

    public async waitAndGetElementAttribute(elementLocator: By,
        attribute: string,
        visibilityTimeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<string> {

        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, visibilityTimeout);

            try {
                const attributeValue = await element.getAttribute(attribute);
                return attributeValue;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum gettin of the '${attribute}' attribute attempts, from the '${elementLocator}' element`);
    }

    public async waitAndGetCssValue(elementLocator: By,
        cssAttribute: string,
        visibilityTimeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<string> {

        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, visibilityTimeout);

            try {
                const cssAttributeValue = await element.getCssValue(cssAttribute);
                return cssAttributeValue;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum gettin of the '${cssAttribute}' css attribute attempts, from the '${elementLocator}' element`);
    }

    public async waitAttributeValue(elementLocator: By,
        attribute: string,
        expectedValue: string,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {

        await this.driver.wait(async () => {
            const attributeValue: string = await this.waitAndGetElementAttribute(elementLocator, attribute, timeout);

            return expectedValue === attributeValue;
        },
            timeout,
            `The '${attribute}' attribute value doesn't match with expected value '${expectedValue}'`);
    }

    public async type(elementLocator: By, text: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, timeout);

            try {
                await element.sendKeys(text);
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
    }

    public async typeToInvisible(elementLocator: By, text: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitPresence(elementLocator, timeout);

            try {
                await element.sendKeys(text);
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum typing attempts, to the '${elementLocator}' element`);
    }

    public async clear(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, timeout);

            try {
                await element.clear();
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum clearing attempts, to the '${elementLocator}' element`);
    }

    public async enterValue(elementLocator: By, text: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.waitVisibility(elementLocator, timeout);
        await this.clear(elementLocator, timeout);
        await this.waitAttributeValue(elementLocator, 'value', '', timeout);
        await this.type(elementLocator, text, timeout);
        await this.waitAttributeValue(elementLocator, 'value', text, timeout);
    }

    public async waitAndSwitchToFrame(iframeLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driver.wait(until.ableToSwitchToFrame(iframeLocator), timeout);
    }

    public async waitAndGetText(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<string> {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, timeout);

            try {
                const innerText: string = await element.getText();
                return innerText;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum text obtaining attempts, from the '${elementLocator}' element`);
    }

    public async waitAndGetValue(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT): Promise<string> {
        const elementValue: string = await this.waitAndGetElementAttribute(elementLocator, 'value', timeout);
        return elementValue;
    }

    public async waitUntilTrue(callback: any, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driver.wait(callback(), timeout);
    }

    public async reloadPage() {
        await this.driver.navigate().refresh();
    }

    public async navigateTo(url: string) {
        await this.driver.navigate().to(url);
        await this.waitURL(url);
    }

    public async waitURL(expectedUrl: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.getDriver().wait(async () => {
            const currentUrl: string = await this.getDriver().getCurrentUrl();
            const urlEquals: boolean = currentUrl === expectedUrl;

            if (urlEquals) {
                return true;
            }
        });
    }

    public async scrollTo(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const attempts: number = TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const element: WebElement = await this.waitPresence(elementLocator, timeout);

            try {
                await this.getAction().mouseMove(element).perform();
                return;
            } catch (err) {
                if (err instanceof error.StaleElementReferenceError) {
                    await this.wait(polling);
                    continue;
                }

                throw err;
            }
        }

        throw new Error(`Exceeded maximum mouse move attempts, for the '${elementLocator}' element`);
    }

    getDriver(): ThenableWebDriver {
        return this.driver;
    }

}
