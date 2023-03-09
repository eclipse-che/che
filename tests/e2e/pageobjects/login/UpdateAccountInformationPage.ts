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
import { Logger } from '../../utils/Logger';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';

@injectable()
export class UpdateAccountInformationPage {

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async enterEmail(email: string, timeout: number) {
        Logger.debug('UpdateAccountInformationPage.enterEmail');

        await this.driverHelper.enterValue(By.id('email'), email, timeout);
    }

    async enterFirstName(firstName: string, timeout: number) {
        Logger.debug('UpdateAccountInformationPage.enterFirstName');

        await this.driverHelper.enterValue(By.id('firstName'), firstName, timeout);
    }

    async enterLastName(lastName: string, timeout: number) {
        Logger.debug('UpdateAccountInformationPage.enterLastName');

        await this.driverHelper.enterValue(By.id('lastName'), lastName, timeout);
    }

    async clickConfirmButton(timeout: number) {
        Logger.debug('UpdateAccountInformationPage.clickConfirmButton');

        await this.driverHelper.waitAndClick(By.xpath('//input[@type=\'submit\']'), timeout);
    }

    async clickAddToExistingAccountButton(timeout: number) {
        Logger.debug('UpdateAccountInformationPage.clickAddToExistingAccountButton');

        await this.driverHelper.waitAndClick(By.id('linkAccount'), timeout);
    }

    async enterPassword(password: string, timeout: number) {
        Logger.debug('UpdateAccountInformationPage.enterPassword');

        await this.driverHelper.enterValue(By.id('password'), password, timeout);
    }

    async clickLogInButton(timeout: number) {
        Logger.debug('UpdateAccountInformationPage.clickLogInButton');

        await this.driverHelper.waitAndClick(By.id('kc-login'), timeout);
    }

    async clickToAllowSelectedPermissionsButton(timeout: number) {
        Logger.debug('UpdateAccountInformationPage.clickToAllowSelectedPermissionsButton');

        await this.driverHelper.waitAndClick(By.xpath('//input[@name=\'approve\']'), timeout);
    }

}
