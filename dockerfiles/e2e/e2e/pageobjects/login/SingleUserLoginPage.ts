/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { ICheLoginPage } from './ICheLoginPage';
import { injectable, inject } from 'inversify';
import { ThenableWebDriver } from 'selenium-webdriver';
import { TYPES, CLASSES } from '../../inversify.types';
import { IDriver } from '../../driver/IDriver';
import { TestConstants } from '../../TestConstants';
import { Dashboard } from '../dashboard/Dashboard';

@injectable()
export class SingleUserLoginPage implements ICheLoginPage {
    constructor(
        @inject(TYPES.Driver) private readonly driver: IDriver,
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard) { }

    async login(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        const webDriver: ThenableWebDriver = this.driver.get();
        await webDriver.navigate().to(TestConstants.TS_SELENIUM_BASE_URL);
        await this.dashboard.waitPage(timeout);
    }

}
