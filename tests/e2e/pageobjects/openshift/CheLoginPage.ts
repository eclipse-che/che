/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';

@injectable()
export class CheLoginPage {

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
    }

    async waitEclipseCheLoginFormPage() {
        Logger.debug('CheLoginPage.waitEclipseCheLoginFormPage');

        await this.driverHelper.waitVisibility(By.id('login'), TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async inputUserNameEclipseCheLoginPage(userName: string) {
        Logger.debug(`CheLoginPage.inputUserNameEclipseCheLoginPage username: "${userName}"`);

        await this.driverHelper.enterValue(By.id('login'), userName);
    }

    async clickEclipseCheLoginButton() {
        Logger.debug('CheLoginPage.clickEclipseCheLoginButton');

        await this.driverHelper.waitAndClick(By.id('submit-login'));
    }
}
