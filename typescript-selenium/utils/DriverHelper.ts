import { Driver } from "../driver/Driver";
import { inject, injectable } from "inversify";
import { TYPES } from "../types";
import 'selenium-webdriver';
import 'reflect-metadata';
import { WebElementPromise, ThenableWebDriver, By, promise, until, WebElement } from "selenium-webdriver";
import { TestConstants } from "../TestConstants";

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
export class DriverHelper {
    private readonly driver: ThenableWebDriver;

    constructor(
        @inject(TYPES.Driver) driver: Driver
    ) {
        this.driver = driver.get();
    }

    public findElement(locator: By): WebElementPromise {
        return this.driver.findElement(locator);
    }

    public isVisible(locator: By): promise.Promise<boolean> {
        return this.findElement(locator)
            .isDisplayed()
            .catch(err => {
                return false
            })
    }

    public wait(miliseconds: number): promise.Promise<void> {
        return new promise.Promise<void>(resolve => { setTimeout(resolve, miliseconds) })
    }

    public async waitVisibilityBoolean(locator: By, attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING): Promise<boolean> {
        for (let i = 0; i < attempts; i++) {
            const isVisible: boolean = await this.isVisible(locator);

            if (isVisible) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public async waitDisappearanceBoolean(locator: By, attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING): Promise<boolean> {
        for (let i = 0; i < attempts; i++) {
            const isVisible: boolean = await this.isVisible(locator)

            if (!isVisible) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public waitVisibility(elementLocator: By, timeout = TestConstants.DEFAULT_TIMEOUT): promise.Promise<WebElement> {
        return new promise.Promise<WebElement>((resolve, reject) => {
            this.driver
                .wait(until.elementLocated(elementLocator), timeout)
                .then(webElement => {
                    this.driver.wait(until.elementIsVisible(webElement), timeout)
                        .then(webElement => { resolve(webElement) })
                })
        })
    }

    public async waitAllVisibility(locators: Array<By>, timeout = TestConstants.DEFAULT_TIMEOUT) {
        for (const elementLocator of locators) {
            await this.waitVisibility(elementLocator, timeout)
        }
    }

    public async waitDisappearance(elementLocator: By, attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING) {
        const isDisappeared = await this.waitDisappearanceBoolean(elementLocator, attempts, polling)

        if (!isDisappeared) {
            throw new Error(`Waiting attempts exceeded, element '${elementLocator}' is still visible`)
        }
    }

    public async waitAllDisappearance(locators: Array<By>, attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING): Promise<void> {
        for (const elementLocator of locators) {
            await this.waitDisappearance(elementLocator, attempts, polling)
        }
    }

    public async waitAndClick(elementLocator: By, timeout = TestConstants.DEFAULT_TIMEOUT) {
        for (let i = 0; i < 5; i++) {
            const element: WebElement = await this.waitVisibility(elementLocator, timeout)

            try {
                await element.click();
                return;
            } catch (err) {
                continue;
            }
        }

        throw new Error(`Exceeded maximum clicking attempts, the '${elementLocator}' element is not clickable`)

    }





}