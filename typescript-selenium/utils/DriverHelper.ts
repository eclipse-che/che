import { Driver } from "../driver/Driver";
import { inject, injectable } from "inversify";
import { TYPES } from "../types";
import 'selenium-webdriver';
import 'reflect-metadata';
import { WebElementPromise, ThenableWebDriver, By, promise, until, WebElement } from "selenium-webdriver";

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
    public static readonly DEFAULT_TIMEOUT: number = 20000;
    public static readonly DEFAULT_ATTEMPTS: number = 20;
    public static readonly DEFAULT_POLLING: number = 1000;
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

    public async waitVisibilityBoolean(locator: By, attempts = DriverHelper.DEFAULT_ATTEMPTS, polling = DriverHelper.DEFAULT_POLLING): Promise<boolean> {
        for (let i = 0; i < attempts; i++) {
            if (await this.isVisible(locator)) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public async waitDisappearanceBoolean(locator: By, attempts = DriverHelper.DEFAULT_ATTEMPTS, polling = DriverHelper.DEFAULT_POLLING): Promise<boolean> {
        for (let i = 0; i < attempts; i++) {
            if (await !this.isVisible(locator)) {
                return true;
            }

            await this.wait(polling);
        }

        return false;
    }

    public waitVisibility(elementLocator: By, timeout = DriverHelper.DEFAULT_TIMEOUT): promise.Promise<WebElement> {
        return new promise.Promise<WebElement>((resolve, reject) => {
            this.driver
                .wait(until.elementLocated(elementLocator), timeout)
                .then(webElement => {
                    resolve(this.driver.wait(until.elementIsVisible(webElement), timeout))
                })
        })
    }

    public async waitAllVisibility(locators: Array<By>, timeout = DriverHelper.DEFAULT_TIMEOUT): Promise<void> {
        await locators.forEach(async elementLocator => {
            await this.waitVisibility(elementLocator, timeout)
        })
    }

    public async waitDisappearance(elementLocator: By, attempts = DriverHelper.DEFAULT_ATTEMPTS, polling = DriverHelper.DEFAULT_POLLING): Promise<void> {
        await this.waitDisappearanceBoolean(elementLocator, attempts, polling)
            .then(isVisible => {
                if (isVisible) {
                    throw new Error(`Waiting attempts exceeded, element '${elementLocator}' is still visible`)
                }
            })
    }

    public async waitAllDisappearance(locators: Array<By>, attempts = DriverHelper.DEFAULT_ATTEMPTS, polling = DriverHelper.DEFAULT_POLLING ): Promise<void> {
        await locators.forEach(async elementLocator => {
            await this.waitDisappearance(elementLocator, attempts, polling)
        })
    }

    public click(elementLocator: By, timeout = DriverHelper.DEFAULT_TIMEOUT): promise.Promise<void> {
        return this.waitVisibility(elementLocator, timeout).then(element => { element.click() })
    }






}