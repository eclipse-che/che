import { Driver } from "../driver/Driver";
import { inject, injectable } from "inversify";
import { TYPES } from "../types";
import 'selenium-webdriver';
import 'reflect-metadata';
import { WebElementPromise, ThenableWebDriver, By, promise } from "selenium-webdriver";

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
        // try{
        // return this.findElement(locator).isDisplayed();
        // }catch(err){
        //     return  new promise.Promise(resolve =>{resolve(false)})
        // }

        return this.findElement(locator).isDisplayed().catch(err => { return false})


         
            
        
    }

    public wait(miliseconds: number): promise.Promise<void> {
        return new promise.Promise<void>(resolve => { setTimeout(resolve, miliseconds) })
    }

    public async waitVisibility(locator: By): Promise<boolean> {
        for (let i = 0; i < 10; i++) {
            if (await this.isVisible(locator)) {
                return true;
            }

            await this.wait(5000);
        }

        return false;
    }




}