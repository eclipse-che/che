/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import "reflect-metadata";
import { LoginPage } from "./LoginPage";
import { injectable, inject } from "inversify";
import { ThenableWebDriver } from "selenium-webdriver";
import { TYPES } from "../../inversify.types";
import { Driver } from "../../driver/Driver";
import { TestConstants } from "../../TestConstants";

@injectable()
export class SingleUserLoginPage implements LoginPage {
    private readonly driver: ThenableWebDriver;

    constructor(
        @inject(TYPES.Driver) driver: Driver
    ) {
        this.driver = driver.get();
    }

    async login() {
        await this.driver
            .navigate()
            .to(TestConstants.TS_SELENIUM_BASE_URL)
    }

}
