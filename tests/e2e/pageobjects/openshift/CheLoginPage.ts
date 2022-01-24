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
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class CheLoginPage {

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitEclipseCheLoginFormPage() {
            Logger.debug('CheLoginPage.waitEclipseCheLoginFormPage');

            await this.driverHelper.waitVisibility(By.id('login'), TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async inputUserNameEclipseCheLoginPage(userName: string) {
            Logger.debug(`CheLoginPage.inputUserNameEclipseCheLoginPage username: "${userName}"`);

            await this.driverHelper.enterValue(By.id('login'), userName);
    }

    async inputPaswordEclipseCheLoginPage(passw: string) {
            Logger.debug(`CheLoginPage.inputPaswordEclipseCheLoginPage password: "${passw}"`);

            await this.driverHelper.enterValue(By.id('password'), passw);
    }

    async clickEclipseCheLoginButton() {
        Logger.debug('CheLoginPage.clickEclipseCheLoginButton');

        await this.driverHelper.waitAndClick(By.id('submit-login'));
    }

    async isFirstBrokerLoginPageVisible(): Promise<boolean> {
        Logger.debug('CheLoginPage.waitFirstBrokerLoginPage');

        return await this.driverHelper.isVisible(By.id('kc-update-profile-form'));
    }

    async waitFirstBrokerLoginPage() {
        Logger.debug('CheLoginPage.waitFirstBrokerLoginPage');

        await this.driverHelper.waitVisibility(By.id('kc-update-profile-form'), TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async enterEmailFirstBrokerLoginPage(email: string) {
        Logger.debug(`CheLoginPage.enterEmailFirstBrokerLoginPage "${email}"`);

        await this.driverHelper.enterValue(By.id('email'), email);
    }

    async enterFirstNameBrokerLoginPage(firstName: string) {
        Logger.debug(`CheLoginPage.enterFirstNameBrokerLoginPage "${firstName}"`);

        await this.driverHelper.enterValue(By.id('firstName'), firstName);
    }

    async enterLastNameBrokerLoginPage(lastName: string) {
        Logger.debug(`CheLoginPage.enterLastNameBrokerLoginPage "${lastName}"`);

        await this.driverHelper.enterValue(By.id('lastName'), lastName);
    }

    async clickOnSubmitButton() {
        Logger.debug('CheLoginPage.clickOnSubmitButton');

        const submitButtonlocator: By = By.css('input[type=submit]');
        await this.driverHelper.waitAndClick(submitButtonlocator);
    }

    async waitDisappearanceBrokerLoginPage() {
        Logger.debug('CheLoginPage.waitDisappearanceBrokerLoginPage');

        await this.driverHelper.waitDisappearance(By.id('kc-update-profile-form'));
    }

}
